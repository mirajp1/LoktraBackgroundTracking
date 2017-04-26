package com.miraj.loktrabackgroundtracking.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.provider.BaseColumns;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.miraj.loktrabackgroundtracking.model.Shift;
import com.miraj.loktrabackgroundtracking.model.ShiftLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by miraj on 26/4/17.
 */

public class SQLiteDBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME= "loktra_background_tracking.db";
    private static final String LOG_TAG = SQLiteDBHelper.class.getSimpleName();

    private SQLiteDatabase db;

    public SQLiteDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(ShiftTable.TABLE_CREATE_QUERY);
        sqLiteDatabase.execSQL(ShiftLocationTable.TABLE_CREATE_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }


    public long startShift(Shift shift){

        ContentValues contentValues = new ContentValues();
        contentValues.put(ShiftTable.COLUMN_NAME_START_TIME, shift.getStartTime());

        return db.insert(ShiftTable.TABLE_NAME, null, contentValues);

    }

    public long endShift(Shift shift){

        ContentValues contentValues = new ContentValues();
        contentValues.put(ShiftTable.COLUMN_NAME_END_TIME, shift.getEndTime());

        return db.update(ShiftTable.TABLE_NAME, contentValues, ShiftTable._ID +" = ?", new String[]{shift.get_ID()+""});

    }

    public Shift getCurrentShift(){

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        String [] projection = {"*"};

        String selection = ShiftTable.COLUMN_NAME_END_TIME + " is NULL";
//        String[] selectionArgs = { "" };
        qb.setTables(ShiftTable.TABLE_NAME);

        Cursor c = qb.query(
                db,
                projection,
                selection,
                null,
                null,
                null,
                ShiftTable._ID +" DESC LIMIT 1");

        Shift shift =null;
        if(c.moveToNext()){

            shift = new Shift();
            shift.set_ID(c.getLong(c.getColumnIndex(ShiftTable._ID)));
            shift.setStartTime(c.getLong(c.getColumnIndex(ShiftTable.COLUMN_NAME_START_TIME)));
            shift.setEndTime(c.getLong(c.getColumnIndex(ShiftTable.COLUMN_NAME_END_TIME)));
            shift.setLocations(getShiftLocations(shift.get_ID()));

        }

        return shift;
    }

    public Shift getShift(long _ID){

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        String [] projection = {"*"};

        String selection = ShiftTable._ID + " = ?";
        String[] selectionArgs = { _ID+"" };
        qb.setTables(ShiftTable.TABLE_NAME);

        Cursor c = qb.query(
                db,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null);

        Shift shift =null;
        if(c.moveToNext()){

            shift = new Shift();
            shift.set_ID(c.getLong(c.getColumnIndex(ShiftTable._ID)));
            shift.setStartTime(c.getLong(c.getColumnIndex(ShiftTable.COLUMN_NAME_START_TIME)));
            shift.setEndTime(c.getLong(c.getColumnIndex(ShiftTable.COLUMN_NAME_END_TIME)));
            shift.setLocations(getShiftLocations(shift.get_ID()));

        }
        c.close();

        return shift;
    }

    public List<Shift> getAllShifts(){

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        String [] projection = {"*"};
        String selection  = ShiftTable.COLUMN_NAME_END_TIME +" IS NOT NULL";
        String sortOrder = ShiftTable._ID +" ASC";
        qb.setTables(ShiftTable.TABLE_NAME);

        Cursor c = qb.query(
                db,
                projection,
                selection,
                null,
                null,
                null,
                sortOrder);

        List<Shift> shifts = new ArrayList<>();
        while(c.moveToNext()){

            Shift shift = new Shift();
            shift.set_ID(c.getLong(c.getColumnIndex(ShiftTable._ID)));
            shift.setStartTime(c.getLong(c.getColumnIndex(ShiftTable.COLUMN_NAME_START_TIME)));
            shift.setEndTime(c.getLong(c.getColumnIndex(ShiftTable.COLUMN_NAME_END_TIME)));
            shift.setLocations(getShiftLocations(shift.get_ID()));

            shifts.add(shift);

        }
        c.close();


        Log.e(LOG_TAG,"Shifts size: "+ shifts.size());
        return shifts;
    }


    public long addShiftLocation(ShiftLocation shiftLocation){

        ContentValues contentValues = new ContentValues();
        contentValues.put(ShiftLocationTable.COLUMN_NAME_SHIFT_ID, shiftLocation.getShiftId());
        contentValues.put(ShiftLocationTable.COLUMN_NAME_LATITUDE, shiftLocation.getLatLng().latitude);
        contentValues.put(ShiftLocationTable.COLUMN_NAME_LONGITUDE, shiftLocation.getLatLng().longitude);
        contentValues.put(ShiftLocationTable.COLUMN_NAME_TIMESTAMP, shiftLocation.getTimeStamp());

        return db.insert(ShiftLocationTable.TABLE_NAME, null, contentValues);

    }

    public List<ShiftLocation> getShiftLocations(long _ID){

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        String [] projection = {"*"};

        String selection = ShiftLocationTable.COLUMN_NAME_SHIFT_ID + " = ?";
        String[] selectionArgs = { _ID+"" };
        qb.setTables(ShiftLocationTable.TABLE_NAME);

        Cursor c = qb.query(
                db,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                ShiftLocationTable.COLUMN_NAME_TIMESTAMP +" ASC");


        List<ShiftLocation> shiftLocations = new ArrayList<>();

        while(c.moveToNext()){

            ShiftLocation shiftLocation = new ShiftLocation();

            shiftLocation.set_ID(c.getLong(c.getColumnIndex(ShiftLocationTable._ID)));
            shiftLocation.setShiftId(c.getLong(c.getColumnIndex(ShiftLocationTable.COLUMN_NAME_SHIFT_ID)));
            shiftLocation.setTimeStamp(c.getLong(c.getColumnIndex(ShiftLocationTable.COLUMN_NAME_TIMESTAMP)));

            LatLng latLng = new LatLng(
                    c.getDouble(c.getColumnIndex(ShiftLocationTable.COLUMN_NAME_LATITUDE)),
                    c.getDouble(c.getColumnIndex(ShiftLocationTable.COLUMN_NAME_LONGITUDE))
            );

            shiftLocation.setLatLng(latLng);

            shiftLocations.add(shiftLocation);
        }

        c.close();

        return shiftLocations;

    }

    public synchronized void open(){

        db =getWritableDatabase();

    }

    @Override
    public synchronized void close() {
        super.close();

        if(db!=null)
            db.close();

    }

    @Override
    public void onConfigure(SQLiteDatabase db){
        db.setForeignKeyConstraintsEnabled(true);
    }


    public static class ShiftTable implements BaseColumns {

        public static final String TABLE_NAME = "shift";

        public static final String COLUMN_NAME_START_TIME = "start_time";
        public static final String COLUMN_NAME_END_TIME = "end_time";

        public static final String TABLE_CREATE_QUERY="create table " + TABLE_NAME + " ( " + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_NAME_START_TIME + " INTEGER, " + COLUMN_NAME_END_TIME + " INTEGER);";


    }

    public static class ShiftLocationTable implements BaseColumns {
        public static final String TABLE_NAME = "shift_location";

        public static final String COLUMN_NAME_SHIFT_ID = "shift_id";
        public static final String COLUMN_NAME_LATITUDE = "latitude";
        public static final String COLUMN_NAME_LONGITUDE = "longitude";
        public static final String COLUMN_NAME_TIMESTAMP = "time_utc";

        public static final String TABLE_CREATE_QUERY="create table " + TABLE_NAME + " ( " + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_NAME_SHIFT_ID +" INTEGER, "+ COLUMN_NAME_LATITUDE + " REAL, " + COLUMN_NAME_LONGITUDE + " REAL, " +COLUMN_NAME_TIMESTAMP +" INTEGER, FOREIGN KEY(" +COLUMN_NAME_SHIFT_ID+") REFERENCES "+ ShiftTable.TABLE_NAME +"("+ShiftTable._ID+"));";


    }
}
