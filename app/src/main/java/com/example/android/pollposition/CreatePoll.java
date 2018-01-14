package com.example.android.pollposition;

import android.content.Context;
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

import java.net.URL;
import java.util.ArrayList;

public class CreatePoll extends AppCompatActivity {

    ArrayList<String> pollElements = new ArrayList<String>();

    EditText pollNameEditText;
    EditText pollElementEditText;
    LinearLayout pollElementsView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_poll);

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

    }

    public class SavePollTask extends AsyncTask<URL, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(getApplicationContext(),
                    R.string.create_poll_save_toast_text,
                    Toast.LENGTH_LONG).show();
        }

        @Override
        protected String doInBackground(URL... params) {
            /*
            URL searchUrl = params[0];
            String createResult = null;
            try {
                createResult = NetworkUtils.getResponseFromHttpUrl(searchUrl);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return createResult;
             */
            return null;
        }

        @Override
        protected void onPostExecute(String githubSearchResults) {
            /*
            mLoadingIndicator.setVisibility(View.INVISIBLE);
            if (githubSearchResults != null && !githubSearchResults.equals("")) {
                // COMPLETED (17) Call showJsonDataView if we have valid, non-null results
                showJsonDataView();
                mSearchResultsTextView.setText(githubSearchResults);
            } else {
                // COMPLETED (16) Call showErrorMessage if the result is null in onPostExecute
                showErrorMessage();
            }
             */
        }
    }
}
