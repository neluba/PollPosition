package com.example.android.pollposition;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.example.android.pollposition.StorageClasses.Poll;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    public static final String BEACONS_PARAMETER = "beacons";

    public static final String EXTRAS_POLL_ID = "id";
    public static final String EXTRAS_NAME = "name";
    public static final String EXTRAS_DATE = "date";
    public static final String EXTRAS_BEACON = "beacon";

    private String closestBeacon;
    ArrayList<Poll> pollsList = new ArrayList<>();

    // Recyclerview variables
    private RecyclerView mRecyclerView;
    private ConstraintLayout mNoBeacons;
    private MainRecyclerViewAdapter mAdapter;
    private int mPosition = RecyclerView.NO_POSITION;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = (RecyclerView) findViewById(R.id.main_recycler_view);
        mNoBeacons = (ConstraintLayout) findViewById(R.id.no_beacons);

        // initialize recyclerview
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);

        mAdapter = new MainRecyclerViewAdapter(this);
        mRecyclerView.setAdapter(mAdapter);

        // fab initialize
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO später wieder zu create ändern und EXTRAS_BEACON übergeben mit closestBeacon
                Intent createPollIntent;
                createPollIntent = new Intent(MainActivity.this, CreatePoll.class);
                createPollIntent.putExtra(EXTRAS_POLL_ID, new Long(17));
                createPollIntent.putExtra(EXTRAS_NAME, "test umfrage");
                createPollIntent.putExtra(EXTRAS_BEACON, "BEACON 1");
                createPollIntent.putExtra(EXTRAS_DATE, System.currentTimeMillis());
                startActivity(createPollIntent);
            }
        });

        // TODO TEST
        new GetPollsTask().execute("[ \"1\", \"2\" ]");
    }

    /**
     * Setting up the menu for the MainActivity
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    /**
     * On menu item select
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.reload) {
            // TODO TEST
            new GetPollsTask().execute("[ \"1\", \"2\" ]");
        }

        return super.onOptionsItemSelected(item);
    }

    private void showRecyclerView() {
        mNoBeacons.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showNoBeaconsFoundError() {
        mRecyclerView.setVisibility(View.GONE);
        mNoBeacons.setVisibility(View.VISIBLE);
    }

    /**
     * Downloads all polls from the server and fills the recyclerview with them
     * Requires a json array with all available beacons
     */
    public class GetPollsTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            // build url
            Uri pollsUri = Uri.parse(getString(R.string.server_url)).buildUpon()
                    .appendPath(getString(R.string.server_polls))
                    .appendQueryParameter(BEACONS_PARAMETER, params[0])
                    .build();
            URL pollsUrl;
            try {
                pollsUrl = new URL(pollsUri.toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return null;
            }

            // open connection
            String response;
            try {
                HttpURLConnection urlConnection = (HttpURLConnection) pollsUrl.openConnection();
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
            if(response == null) {
                showNoBeaconsFoundError();
                return;
            }

            // first element is always the id, second one is the name, third one the beacon name and forth the date
            // convert json array to an arraylist
            try {
                JSONArray jsonItems = new JSONArray(response);
                pollsList.clear();
                for (int i = 0; i < jsonItems.length(); i = i+4) {
                    Poll poll = new Poll();
                    poll.setId(jsonItems.getLong(i));
                    poll.setName(jsonItems.getString(i+1));
                    poll.setBeaconName(jsonItems.getString(i+2));
                    poll.setDate(jsonItems.getLong(i+3));
                    pollsList.add(poll);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                showNoBeaconsFoundError();
            }

            if(pollsList.size() > 0) {
                mAdapter.swapList(pollsList);
                if (mPosition == RecyclerView.NO_POSITION)
                    mPosition = 0;
                mRecyclerView.smoothScrollToPosition(mPosition);
                showRecyclerView();
            } else
                showNoBeaconsFoundError();

        }
    }
}
