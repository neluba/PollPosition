package com.example.android.pollposition;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

public class CreatePoll extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_poll);
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
            //createAndSave();
        }

        return super.onOptionsItemSelected(item);
    }

    /** Adds an answer to the poll and adds the view to the linear layout
     *
     * @param v view element
     */
    public void addItem(View v) {

    }

    /**
     * Removes an answer from the poll and also removes it from the linear layout
     * @param v view element
     */
    public void removeItem(View v) {

    }
}
