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

    /** Adds an answer to the poll and adds the view to the linear layout
     *
     * @param v view element
     */
    public void addItem(View v) {
        if(pollElements.size() < getResources().getInteger(R.integer.max_poll_answers)) {
            String name = pollElementEditText.getText().toString();
            if (!TextUtils.isEmpty(name)) {
                // check for duplicate answer
                for(String pollName : pollElements) {
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
            }
        } else {
            Toast.makeText(this,
                    R.string.create_element_limit,
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Removes an answer from the poll and also removes it from the linear layout
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
    }

    public void createAndSave() {
        // convert the answers into a json array
        JSONArray jsonItems = new JSONArray(pollElements);
        String itemsString =  jsonItems.toString();

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
            if(response == null)
                return;

            if(response.equals("true")) {
                finish();
            }
        }
    }
}
