package com.example.android.pollposition;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.pollposition.StorageClasses.Answer;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

public class PollDetails extends AppCompatActivity {

    public static final String POLL_ID_PARAMETER = "id";
    public static final String ANSWER_NAME_PARAMETER = "name";

    // values for the answers array
    public static final int NAME_VALUE = 0;
    public static final int VOTE_VALUE = 1;

    long pollId;

    TextView pollName;
    TextView pollDate;
    TextView beaconName;

    LinearLayout optionsLinearLayout;
    LinearLayout answersLinearLayout;

    ArrayList<String> optionNames = new ArrayList<>();
    ArrayList<Answer> answersList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poll_details);

        pollName = findViewById(R.id.details_name);
        pollDate = findViewById(R.id.details_date);
        beaconName = findViewById(R.id.details_beacon);

        optionsLinearLayout = findViewById(R.id.details_options_layout);
        answersLinearLayout = findViewById(R.id.details_answers_layout);

        // fill ui with the data from the intent
        if(getIntent().hasExtra(MainActivity.EXTRAS_POLL_ID)) {
            pollId = getIntent().getLongExtra(MainActivity.EXTRAS_POLL_ID, -1);
        }
        if (getIntent().hasExtra(MainActivity.EXTRAS_NAME)) {
            pollName.setText(getIntent().getStringExtra(MainActivity.EXTRAS_NAME));
        }
        if (getIntent().hasExtra(MainActivity.EXTRAS_DATE)) {
            Long dateExtra = getIntent().getLongExtra(MainActivity.EXTRAS_DATE, System.currentTimeMillis());
            int flags = DateUtils.FORMAT_SHOW_DATE
                    | DateUtils.FORMAT_NUMERIC_DATE
                    | DateUtils.FORMAT_SHOW_YEAR
                    | DateUtils.FORMAT_SHOW_TIME;
            String dateString = DateUtils.formatDateTime(this, dateExtra, flags);
            pollDate.setText(dateString);
        }
        if (getIntent().hasExtra(MainActivity.EXTRAS_BEACON)) {
            beaconName.setText(getIntent().getStringExtra(MainActivity.EXTRAS_BEACON));
        }

        // check if the poll was already answered
        SharedPreferences sharedPreferences = this.getSharedPreferences(
                getString(R.string.shared_preferences_name),
                Context.MODE_PRIVATE);
        int answered = sharedPreferences.getInt(String.valueOf(pollId), -1);
        if (answered == -1) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(String.valueOf(pollId), 0);
            editor.apply();
        }

        // show options or answers
        if (answered == 1) {
            new GetAnswersTask().execute();
        } else {
            new GetOptionsTask().execute();
        }


    }

    /**
     * This method gets started by the GetOptionsTask AsyncTask and fills the UI with poll options
     */
    private void fillOptions() {
        // create the views
        int counter = 0;
        for (final String optionName : optionNames) {
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View optionView = inflater.inflate(R.layout.details_option_list_item, optionsLinearLayout, false);

            optionView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    chooseAnswer(optionName);
                }
            });

            TextView optionNameView = (TextView) optionView.findViewById(R.id.details_options_item_name);
            optionNameView.setText(optionName);

            optionsLinearLayout.addView(optionView, counter);
            counter++;
        }
        // set visibility
        answersLinearLayout.setVisibility(View.GONE);
        optionsLinearLayout.setVisibility(View.VISIBLE);
    }

    private void fillAnswers() {
        // start async task

        // count votes
        int voteCount = 0;
        for (int i = 0; i < answersList.size(); i++) {
            voteCount = voteCount + answersList.get(i).getVotes();
        }

        // create the views
        int counter = 0;
        for (Answer answer : answersList) {
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View answerView = inflater.inflate(R.layout.details_answers_list_item, answersLinearLayout, false);

            TextView answerNameView = (TextView) answerView.findViewById(R.id.details_answers_name);
            answerNameView.setText(answer.getName());

            TextView voteCountView = (TextView) answerView.findViewById(R.id.details_answers_participants);
            String voteText = answer.getVotes() + "/" + String.valueOf(voteCount);
            voteCountView.setText(voteText);

            ProgressBar progressBar = (ProgressBar) answerView.findViewById(R.id.details_answers_progressBar);
            progressBar.setProgress((answer.getVotes() * 100) / voteCount);

            answersLinearLayout.addView(answerView, counter);
            counter++;
        }


        // set visibility
        optionsLinearLayout.setVisibility(View.GONE);
        answersLinearLayout.setVisibility(View.VISIBLE);
    }

    private void chooseAnswer(String name) {
        // save answer
        SharedPreferences sharedPreferences = this.getSharedPreferences(
                getString(R.string.shared_preferences_name),
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(String.valueOf(pollId), 1);
        editor.apply();

        // start the async task
        new SaveAnswerTask().execute(name);
    }


    /**
     * Downloads all poll options from the server and opens the fillOptions() method
     */
    public class GetOptionsTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            // build url
            Uri optionsUri = Uri.parse(getString(R.string.server_url)).buildUpon()
                    .appendPath(getString(R.string.server_poll_options))
                    .appendQueryParameter(POLL_ID_PARAMETER, String.valueOf(pollId))
                    .build();
            URL optionsUrl;
            try {
                optionsUrl = new URL(optionsUri.toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return null;
            }

            // open connection
            String response;
            try {
                HttpURLConnection urlConnection = (HttpURLConnection) optionsUrl.openConnection();
                try {
                    InputStream in = urlConnection.getInputStream();

                    Scanner scanner = new Scanner(in);
                    scanner.useDelimiter("\\A");

                    boolean hasInput = scanner.hasNext();
                    response = null;
                    if (hasInput) {
                        response = scanner.next();
                    }
                    scanner.close();
                } finally {
                    urlConnection.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            if(response == null)
                return;

            // convert json array to an arraylist
            try {
                JSONArray jsonItems = new JSONArray(response);
                for (int i = 0; i < jsonItems.length(); i++) {
                    optionNames.add(jsonItems.get(i).toString());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            fillOptions();
        }
    }

    /**
     * Downloads all poll answers from the server and opens the fillAnswers() method
     */
    public class GetAnswersTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            // build url
            Uri answersUri = Uri.parse(getString(R.string.server_url)).buildUpon()
                    .appendPath(getString(R.string.server_poll_answers))
                    .appendQueryParameter(POLL_ID_PARAMETER, String.valueOf(pollId))
                    .build();
            URL answersUrl;
            try {
                answersUrl = new URL(answersUri.toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return null;
            }

            // open connection
            String response;
            try {
                HttpURLConnection urlConnection = (HttpURLConnection) answersUrl.openConnection();
                try {
                    InputStream in = urlConnection.getInputStream();

                    Scanner scanner = new Scanner(in);
                    scanner.useDelimiter("\\A");

                    boolean hasInput = scanner.hasNext();
                    response = null;
                    if (hasInput) {
                        response = scanner.next();
                    }
                    scanner.close();
                } finally {
                    urlConnection.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            if(response == null)
                return;


            // first element is always the name, second one is the vote count
            // convert json array to an arraylist
            try {
                JSONArray jsonItems = new JSONArray(response);
                answersList.clear();
                for (int i = 0; i < jsonItems.length(); i = i+2) {
                    Answer answer = new Answer();
                    answer.setName(jsonItems.get(i).toString());
                    answer.setVotes(jsonItems.getInt(i+1));
                    answersList.add(answer);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            fillAnswers();
        }
    }

    /**
     * Sends the answer to the server, which saves it to the database. Afterwards executes a new
     * GetAnswersTask
     */
    public class SaveAnswerTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            // build url
            Uri answerUri = Uri.parse(getString(R.string.server_url)).buildUpon()
                    .appendPath(getString(R.string.server_poll_save_answer))
                    .appendQueryParameter(POLL_ID_PARAMETER, String.valueOf(pollId))
                    .appendQueryParameter(ANSWER_NAME_PARAMETER, params[0])
                    .build();
            URL answerUrl;
            try {
                answerUrl = new URL(answerUri.toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return null;
            }

            // open connection
            String response;
            try {
                HttpURLConnection urlConnection = (HttpURLConnection) answerUrl.openConnection();
                try {
                    InputStream in = urlConnection.getInputStream();

                    Scanner scanner = new Scanner(in);
                    scanner.useDelimiter("\\A");

                    boolean hasInput = scanner.hasNext();
                    response = null;
                    if (hasInput) {
                        response = scanner.next();
                    }
                    scanner.close();
                } finally {
                    urlConnection.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            if(response == null)
                return;

            if(response.equals("true")) {
                new GetAnswersTask().execute();
            }
        }
    }

}
