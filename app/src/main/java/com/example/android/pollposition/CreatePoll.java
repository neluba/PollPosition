package com.example.android.pollposition;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

public class CreatePoll extends AppCompatActivity {

    public static final String POLL_NAME = "name";
    public static final String POLL_DATE = "date";
    public static final String POLL_BEACON = "beacon";

    ArrayList<String> pollElements = new ArrayList<String>();

    EditText pollNameEditText;
    EditText pollElementEditText;
    LinearLayout pollElementsView;
    LinearLayout createAnswerLayout;

    private String beaconName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_poll);

        if (getIntent().hasExtra(MainActivity.EXTRAS_BEACON)) {
            beaconName = getIntent().getStringExtra(MainActivity.EXTRAS_BEACON);
        }

        pollNameEditText = findViewById(R.id.create_name);
        pollElementEditText = findViewById(R.id.poll_element_name);
        pollElementsView = findViewById(R.id.create_answers);
        createAnswerLayout = findViewById(R.id.create_answer_layout);
    }

    // create menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.create_poll_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.save_poll) {
            createAndSave();
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Adds an answer to the poll and adds the view to the linear layout
     *
     * @param v view element
     */
    public void addItem(View v) {
        int maxPollAnswers = getResources().getInteger(R.integer.max_poll_answers);

        if (pollElements.size() < maxPollAnswers) {
            String name = pollElementEditText.getText().toString();
            if (!TextUtils.isEmpty(name)) {
                // check for duplicate answer
                for (String pollName : pollElements) {
                    if (pollName.equals(name)) {
                        Toast.makeText(this,
                                R.string.create_poll_duplicate,
                                Toast.LENGTH_LONG);
                        return;
                    }
                }
                // create the view
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View pollElementView = inflater.inflate(R.layout.create_poll_answer_list_item, pollElementsView, false);

                TextView itemTextView = (TextView) pollElementView.findViewById(R.id.answer_name);
                itemTextView.setText(name);
                // add the view directly above the input field
                pollElementsView.addView(pollElementView, pollElements.size());
                pollElements.add(name);
                pollElementEditText.setText("");
                // remove the add answer layout, if max answer count has been reached
                if(pollElements.size() >= maxPollAnswers)
                    createAnswerLayout.setVisibility(View.GONE);
            }
        } else {
            String elementLimitString = String.format(getString(R.string.create_element_limit),
                    getResources().getInteger(R.integer.max_poll_answers));
            Toast.makeText(this,
                    elementLimitString,
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Removes an answer from the poll and also removes it from the linear layout
     *
     * @param v view element
     */
    public void removeItem(View v) {
        View parent = (View) v.getParent();
        TextView itemTextView = (TextView) parent.findViewById(R.id.answer_name);
        String name = itemTextView.getText().toString();

        for (int i = 0; i < pollElements.size(); i++) {
            if (pollElements.get(i).equals(name)) {
                pollElements.remove(i);
                break;
            }
        }

        pollElementsView.removeView((View) v.getParent().getParent());

        // show the create answers layout again, if there is room
        int maxPollAnswers = getResources().getInteger(R.integer.max_poll_answers);
        if(pollElements.size() == maxPollAnswers-1)
            createAnswerLayout.setVisibility(View.VISIBLE);
    }

    public void createAndSave() {
        // check if everything is ok
        String name = pollNameEditText.getText().toString();
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, getString(R.string.create_name_missing), Toast.LENGTH_LONG).show();
            return;
        }
        if (name.length() < getResources().getInteger(R.integer.min_poll_name_characters)) {
            String minCharsText = String.format(getString(R.string.create_name_min_characters),
                    getResources().getInteger(R.integer.min_poll_name_characters));
            Toast.makeText(this, minCharsText, Toast.LENGTH_LONG).show();
            return;
        }
        if(pollElements.size() < 2) {
            Toast.makeText(this, getString(R.string.create_min_answers), Toast.LENGTH_LONG).show();
            return;
        }


        // convert the answers into a json array
        JSONArray jsonItems = new JSONArray(pollElements);
        String itemsString = jsonItems.toString();

        String nameString = pollNameEditText.getText().toString();
        String dateString = String.valueOf(System.currentTimeMillis());

        new SavePollTask().execute(nameString, dateString, beaconName, itemsString);

    }

    /**
     * Sends the poll to the server, which saves it to the database. The order for the parameter is:
     * name, date, beacon, answers(as json array)
     */
    public class SavePollTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            // build url
            Uri answerUri = Uri.parse(getString(R.string.server_url)).buildUpon()
                    .appendPath(getString(R.string.server_poll_save_answer))
                    .appendQueryParameter(POLL_NAME, params[0])
                    .appendQueryParameter(POLL_DATE, params[1])
                    .appendQueryParameter(POLL_BEACON, params[2])
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
            if (response == null)
                return;

            if (response.equals("true")) {
                finish();
            }
        }
    }
}
