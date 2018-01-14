package com.example.android.pollposition;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.pollposition.StorageClasses.Poll;

import java.util.ArrayList;

/**
 * Created by oliver on 14.01.2018.
 */

public class MainRecyclerViewAdapter extends RecyclerView.Adapter<MainRecyclerViewAdapter.MainAdapterViewHolder> {

    private final Context mContext;
    private ArrayList<Poll> pollList;


    public MainRecyclerViewAdapter(@NonNull Context context) {
        mContext = context;
    }

    /**
     * Creates view holders
     *
     * @param viewGroup
     * @param viewType
     * @return
     */
    @Override
    public MainAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(
                R.layout.poll_list_item, viewGroup, false);
        view.setFocusable(true);
        return new MainAdapterViewHolder(view);
    }

    /**
     * Fills the view holders automatically with the data for that position
     *
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(MainAdapterViewHolder holder, int position) {
       Poll poll = pollList.get(0);

       // name
        holder.nameView.setText(poll.getName());
        // beacon
        holder.beaconNameView.setText(poll.getBeaconName());
        // date
        long date = poll.getDate();
        int flags = DateUtils.FORMAT_SHOW_DATE
                | DateUtils.FORMAT_NUMERIC_DATE
                | DateUtils.FORMAT_SHOW_YEAR
                | DateUtils.FORMAT_SHOW_TIME;
        String dateString = DateUtils.formatDateTime(mContext, date, flags);
        holder.date = date;
        holder.dateView.setText(dateString);
        // id
        holder.id = poll.getId();

    }

    @Override
    public int getItemCount() {
        if (pollList == null)
            return 0;
        return pollList.size();
    }

    /**
     * Swaps and sets a new list for the recyclerview to load data from.
     *
     * @param list
     */
    void swapList(ArrayList<Poll> list) {
        pollList = list;
        notifyDataSetChanged();
    }

    class MainAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final TextView dateView;
        final TextView nameView;
        final TextView beaconNameView;
        Long id;
        Long date;

        MainAdapterViewHolder(View view) {
            super(view);

            dateView = (TextView) view.findViewById(R.id.poll_date);
            nameView = (TextView) view.findViewById(R.id.poll_name);
            beaconNameView = (TextView) view.findViewById(R.id.poll_beacon);

            view.setOnClickListener(this);
        }


        @Override
        public void onClick(View view) {
            Intent pollDetails = new Intent(mContext, PollDetails.class);
            pollDetails.putExtra(MainActivity.EXTRAS_POLL_ID, id);
            pollDetails.putExtra(MainActivity.EXTRAS_NAME, nameView.getText().toString());
            pollDetails.putExtra(MainActivity.EXTRAS_BEACON, beaconNameView.getText().toString());
            pollDetails.putExtra(MainActivity.EXTRAS_DATE, date);
            mContext.startActivity(pollDetails);

        }
    }
}
