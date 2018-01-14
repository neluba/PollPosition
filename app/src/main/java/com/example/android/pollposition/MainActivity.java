package com.example.android.pollposition;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    public static final String EXTRAS_POLL_ID = "id";
    public static final String EXTRAS_NAME = "name";
    public static final String EXTRAS_DATE = "date";
    public static final String EXTRAS_BEACON = "beacon";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // fab initialize
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent createPollIntent;
                createPollIntent = new Intent(MainActivity.this, PollDetails.class);
                createPollIntent.putExtra(EXTRAS_NAME, "test umfrage");
                createPollIntent.putExtra(EXTRAS_POLL_ID, 13);
                createPollIntent.putExtra(EXTRAS_BEACON, "BEACON 1");
                createPollIntent.putExtra(EXTRAS_DATE, System.currentTimeMillis());
                startActivity(createPollIntent);
            }
        });
    }
}
