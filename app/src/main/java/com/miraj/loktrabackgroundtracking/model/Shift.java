package com.miraj.loktrabackgroundtracking.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by miraj on 26/4/17.
 */

public class Shift implements Serializable {

    private long _ID;
    private long startTime;
    private long endTime;
    private List<ShiftLocation> locations;

    public long get_ID() {
        return _ID;
    }

    public void set_ID(long _ID) {
        this._ID = _ID;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public List<ShiftLocation> getLocations() {
        return locations;
    }

    public void setLocations(List<ShiftLocation> locations) {
        this.locations = locations;
    }

    @Override
    public String toString() {
        return "Shift{" +
                "_ID=" + _ID +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", locations=" + locations +
                '}';
    }
}
