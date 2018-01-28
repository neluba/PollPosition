package com.example.android.pollposition;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.estimote.coresdk.observation.region.beacon.BeaconRegion;
import com.estimote.coresdk.recognition.packets.Beacon;
import com.estimote.coresdk.service.BeaconManager;
import com.example.android.pollposition.StorageClasses.Poll;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    public static final String BEACONS_PARAMETER = "beacons";
    public static final String TIME_PARAMETER = "time";

    public static final String EXTRAS_POLL_ID = "id";
    public static final String EXTRAS_NAME = "name";
    public static final String EXTRAS_DATE = "date";
    public static final String EXTRAS_BEACON = "beacon";

    private static String closestBeacon;
    ArrayList<Poll> pollsList = new ArrayList<>();

    // Recyclerview variables
    private RecyclerView mRecyclerView;
    private ConstraintLayout mNoBeacons;
    private ConstraintLayout mLoading;
    private MainRecyclerViewAdapter mAdapter;
    private int mPosition = RecyclerView.NO_POSITION;
    FloatingActionButton fab;


    private BeaconManager beaconManager;
    private BeaconRegion region;
    private static List<String> beaconList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // request permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        }

        mRecyclerView = (RecyclerView) findViewById(R.id.main_recycler_view);
        mNoBeacons = (ConstraintLayout) findViewById(R.id.no_beacons);
        mLoading = (ConstraintLayout) findViewById(R.id.loading_constraint_layout);

        beaconManager = new BeaconManager(this);
        region = new BeaconRegion("all", null, null, null);

        // initialize recyclerview
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);

        mAdapter = new MainRecyclerViewAdapter(this);
        mRecyclerView.setAdapter(mAdapter);

        // fab initialize
        fab = findViewById(R.id.fab);
        fab.setVisibility(View.GONE);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent createPollIntent;
                createPollIntent = new Intent(MainActivity.this, CreatePoll.class);
                createPollIntent.putExtra(EXTRAS_BEACON, closestBeacon);
                startActivity(createPollIntent);
            }
        });

        // refill the recyclerview
        if(beaconList.size() > 0) {
            fillRecyclerView();
        }
        // show the floating action button again, if a nearby beacon was already found
        if(closestBeacon != null) {
            setClosestBeacon(closestBeacon);
        }

        // start the ranging listener
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startRanging(region);
            }
        });

        // search for nearby beacons with the ranging listener
        beaconManager.setRangingListener(new BeaconManager.BeaconRangingListener() {
            @Override
            public void onBeaconsDiscovered(BeaconRegion beaconRegion, List<Beacon> beacons) {
                if(!beacons.isEmpty()){
                    beaconList = new ArrayList<>();

                    // get all nearby beacon an put the identifier in the beaconList
                    for(Beacon beacon : beacons){
                        String uuidBeacon = beacon.getProximityUUID().toString();
                        int majorBeacon = beacon.getMajor();
                        int minorBeacon = beacon.getMinor();
                        String identifierBeacon = buildIdentifier(uuidBeacon, majorBeacon, minorBeacon);
                        beaconList.add(identifierBeacon);
                    }

                    fillRecyclerView();

                    // get the first element of the beaconList to find the nearest beacon
                    Beacon nearestBeacon = beacons.get(0);
                    String uuid = nearestBeacon.getProximityUUID().toString();
                    int major = nearestBeacon.getMajor();
                    int minor = nearestBeacon.getMinor();
                    String identifier = buildIdentifier(uuid, major, minor);
                    setClosestBeacon(identifier);
                }
            }
        });
    }

    private void fillRecyclerView() {
        // build a JSONArray with the identifier from the beaconList
        JSONArray jsonItems = new JSONArray(beaconList);
        String itemsString =  jsonItems.toString();
        new GetPollsTask().execute(itemsString);
    }

    

    /**
     *Build the identifier string with uuid major and minor
     */
    private String buildIdentifier(String uuid, int minor, int major){
        String identifier = uuid + ":" + String.valueOf(major) + ":" + String.valueOf(minor);
        return identifier;
    }

    /**
     *Set the closest beacon
     */
    private void setClosestBeacon(String identifier){
        closestBeacon = identifier;
        fab.setVisibility(View.VISIBLE);
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
            fillRecyclerView();
        }

        return super.onOptionsItemSelected(item);
    }

    private void showRecyclerView() {
        mNoBeacons.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
        mLoading.setVisibility(View.GONE);
    }

    private void showNoBeaconsFoundError() {
        mRecyclerView.setVisibility(View.GONE);
        mNoBeacons.setVisibility(View.VISIBLE);
        mLoading.setVisibility(View.GONE);
    }

    /**
     * Downloads all polls from the server and fills the recyclerview with them
     * Requires a json array with all available beacons
     */
    public class GetPollsTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            // get the time window to look for polls
            long time = System.currentTimeMillis();
            time = time- TimeUnit.DAYS.toMillis(getResources().getInteger(R.integer.poll_time_window));
            String timeString = String.valueOf(time);
            // build url
            Uri pollsUri = Uri.parse(getString(R.string.server_url)).buildUpon()
                    .appendPath(getString(R.string.server_polls))
                    .appendQueryParameter(BEACONS_PARAMETER, params[0])
                    .appendQueryParameter(TIME_PARAMETER, timeString)
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
