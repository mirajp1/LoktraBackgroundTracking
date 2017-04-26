package com.miraj.loktrabackgroundtracking.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.miraj.loktrabackgroundtracking.MainActivity;
import com.miraj.loktrabackgroundtracking.R;
import com.miraj.loktrabackgroundtracking.data.SQLiteDBHelper;
import com.miraj.loktrabackgroundtracking.model.Shift;
import com.miraj.loktrabackgroundtracking.model.ShiftLocation;
import com.miraj.loktrabackgroundtracking.util.Constants;
import com.miraj.loktrabackgroundtracking.util.Utils;

public class LocationTrackService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String LOG_TAG = LocationTrackService.class.getSimpleName();
    private GoogleApiClient googleApiClient;

    private static final long LOCATION_UPDATE_INTERVAL = 20 * 1000;
    private static final long LOCATION_UPDATE_FASTEST_INTERVAL = 10 * 1000;
    private static final float LOCATION_UPDATE_SMALLEST_DISPLACEMENT_METRES = 5;

    private SQLiteDBHelper sqLiteDBHelper;
    private int NOTIFICATION_ID = 1111;

    private Shift currentShift;
    private NotificationManager notificationManager;

    public LocationTrackService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        sqLiteDBHelper = new SQLiteDBHelper(this);
        sqLiteDBHelper.open();
        notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        initGoogleApiClient();
        startGoogleApiClient();

        currentShift = sqLiteDBHelper.getCurrentShift();

        startServiceAsForeground();

        Log.e(LOG_TAG,"Service started");


        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        stopLocationUpdates();

        if(sqLiteDBHelper != null)
            sqLiteDBHelper.close();

        if(notificationManager!=null)
            notificationManager.cancel(NOTIFICATION_ID);

        Log.e(LOG_TAG,"onDestroy");

    }

    private void startGoogleApiClient() {
        if (googleApiClient != null) {
            if (!(googleApiClient.isConnected() || googleApiClient.isConnecting())) {
                googleApiClient.connect();
            }
        }
    }

    private void initGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)

                .build();
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = createLocationRequest();

        if (!Utils.checkLocationPermission(this)) {
            return;
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient, locationRequest, this);
    }

    private void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                googleApiClient, this);
    }


    public LocationRequest createLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();

        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
//        locationRequest.setInterval(LOCATION_UPDATE_INTERVAL);
        locationRequest.setFastestInterval(LOCATION_UPDATE_FASTEST_INTERVAL);
        locationRequest.setSmallestDisplacement(LOCATION_UPDATE_SMALLEST_DISPLACEMENT_METRES);

        return locationRequest;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        startLocationUpdates();
        Log.e(LOG_TAG,"onConnected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(LOG_TAG,"onConnectionSuspended");

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(LOG_TAG,"onConnectionFailed");

    }

    @Override
    public void onLocationChanged(Location location) {

        if(currentShift!=null){

            ShiftLocation shiftLocation = new ShiftLocation();
            shiftLocation.setShiftId(currentShift.get_ID());
            shiftLocation.setLatLng(new LatLng(location.getLatitude(),location.getLongitude()));
            shiftLocation.setTimeStamp(System.currentTimeMillis());

            sqLiteDBHelper.addShiftLocation(shiftLocation);

            Intent intent = new Intent();
            intent.setAction(Constants.SEND_BROADCAST_LOCATION_UPDATE);
            intent.putExtra(Constants.SEND_BROADCAST_LOCATION_UPDATE_EXTRA,location);

            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            Log.e(LOG_TAG,"[Location Update]"+location.toString());

        }


    }

    private void startServiceAsForeground(){

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(android.R.drawable.ic_menu_share)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Tracking Location Updates..")
                .setContentIntent(pendingIntent)
                .build();

        startForeground(NOTIFICATION_ID, notification);

    }
}
