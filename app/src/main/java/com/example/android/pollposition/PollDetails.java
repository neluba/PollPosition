package com.example.android.pollposition;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

public class PollDetails extends AppCompatActivity {

    long pollId;

    TextView pollName;
    TextView pollDate;
    TextView beaconName;

    LinearLayout optionsLinearLayout;
    LinearLayout answersLinearLayout;

    ArrayList<String> optionNames = new ArrayList<>();
    String[][] answersArray;

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

        optionNames.add("test1");
        optionNames.add("test2");
        optionNames.add("test3");

        answersArray = new String[3][2];
        answersArray[0][0] = "test1";
        answersArray[0][1] = "44";
        answersArray[1][0] = "test2";
        answersArray[1][1] = "23";
        answersArray[2][0] = "test3";
        answersArray[2][1] = "33";

        // show options or answers
        if (answered == 1) {
            fillAnswers();
        } else {
            fillOptions();
        }


    }

    private void fillOptions() {
        // start async task


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
        for (int i = 0; i < answersArray.length; i++) {
            voteCount = voteCount + Integer.parseInt(answersArray[i][1]);
        }

        // create the views
        int counter = 0;
        for (String[] answer : answersArray) {
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View answerView = inflater.inflate(R.layout.details_answers_list_item, answersLinearLayout, false);

            TextView answerNameView = (TextView) answerView.findViewById(R.id.details_answers_name);
            answerNameView.setText(answer[0]);

            TextView voteCountView = (TextView) answerView.findViewById(R.id.details_answers_participants);
            String voteText = answer[1] + "/" + String.valueOf(voteCount);
            voteCountView.setText(voteText);

            ProgressBar progressBar = (ProgressBar) answerView.findViewById(R.id.details_answers_progressBar);
            progressBar.setProgress((Integer.parseInt(answer[1]) * 100) / voteCount);

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

        fillAnswers();
    }

}
