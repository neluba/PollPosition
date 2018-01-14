package com.example.android.pollposition.StorageClasses;

/**
 * Created by oliver on 14.01.2018.
 */

public class Poll {
    private long id;
    private String name;
    private long date;
    private String beaconName;

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getDate() {
        return date;
    }

    public String getBeaconName() {
        return beaconName;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public void setBeaconName(String beaconName) {
        this.beaconName = beaconName;
    }
}
