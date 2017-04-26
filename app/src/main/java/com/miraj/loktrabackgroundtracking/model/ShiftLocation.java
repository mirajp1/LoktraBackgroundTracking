package com.miraj.loktrabackgroundtracking.model;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

/**
 * Created by miraj on 26/4/17.
 */

public class ShiftLocation implements Serializable {

    private long _ID;
    private long ShiftId;
    private LatLng latLng;
    private long timeStamp;

    public long get_ID() {
        return _ID;
    }

    public void set_ID(long _ID) {
        this._ID = _ID;
    }

    public long getShiftId() {
        return ShiftId;
    }

    public void setShiftId(long shiftId) {
        ShiftId = shiftId;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    @Override
    public String toString() {
        return "ShiftLocation{" +
                "_ID=" + _ID +
                ", ShiftId=" + ShiftId +
                ", latLng=" + latLng +
                ", timeStamp=" + timeStamp +
                '}';
    }
}
