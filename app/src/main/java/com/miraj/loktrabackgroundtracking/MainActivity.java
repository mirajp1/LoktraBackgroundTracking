package com.miraj.loktrabackgroundtracking;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.miraj.loktrabackgroundtracking.data.SQLiteDBHelper;
import com.miraj.loktrabackgroundtracking.model.Shift;
import com.miraj.loktrabackgroundtracking.model.ShiftLocation;
import com.miraj.loktrabackgroundtracking.service.LocationTrackService;
import com.miraj.loktrabackgroundtracking.util.Constants;
import com.miraj.loktrabackgroundtracking.util.Utils;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private GoogleMap map;
    private static final int PERMISSIONS_REQUEST_LOCATION_MAP = 1111;
    private static final int PERMISSIONS_REQUEST_LOCATION_SWITCH = 1112;

    private Switch shiftSwitch;
    private TextView durationTV;
    private ImageButton historyButton;

    private boolean serviceRunning;
    private Shift currentShift;

    private SQLiteDBHelper sqLiteDBHelper;
    private BroadcastReceiver receiver;
    private IntentFilter intentFilter;

    private List<Polyline> mapPolyLines;
    private List<Marker> mapMarkers;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        shiftSwitch = (Switch) findViewById(R.id.shiftSwitch);
        durationTV = (TextView) findViewById(R.id.durationTV);
        historyButton = (ImageButton) findViewById(R.id.historyButton);

        initMap();
        initBroadcastReceiver();

        mapMarkers = new ArrayList<>();
        mapPolyLines = new ArrayList<>();

        sqLiteDBHelper = new SQLiteDBHelper(this);
        sqLiteDBHelper.open();

        serviceRunning=isServiceRunning(LocationTrackService.class);
        currentShift=sqLiteDBHelper.getCurrentShift();

        if(serviceRunning || currentShift!=null){
            shiftSwitch.setChecked(true);
            durationTV.setText(R.string.started_label);

        }

        shiftSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {

                if(checked){

                    if(Utils.checkLocationPermission(MainActivity.this)){
                        shiftSwitch.setText(getString(R.string.end_shift));
                    }
                    else {

                        shiftSwitch.toggle();
                        Utils.requestLocationPermission(MainActivity.this,PERMISSIONS_REQUEST_LOCATION_SWITCH);
                        return;
                    }

                }
                else {
                    shiftSwitch.setText(getString(R.string.start_shift));
                }

                if(checked && !isServiceRunning(LocationTrackService.class)){

                    Log.e(LOG_TAG,"starting location serivce");

                    removeMapPolyLines();
                    removeMapMarkers();

                    serviceRunning=true;

                    Shift temp = new Shift();
                    temp.setStartTime(System.currentTimeMillis());
                    sqLiteDBHelper.startShift(temp);

                    currentShift = sqLiteDBHelper.getCurrentShift();

                    startLocationTrackService();

                    durationTV.setText(R.string.started_label);
                }
                else if(!checked){

                    Log.e(LOG_TAG,"stopping location serivce");

                    currentShift.setEndTime(System.currentTimeMillis());
                    sqLiteDBHelper.endShift(currentShift);
                    currentShift = sqLiteDBHelper.getCurrentShift();

                    stopLocationTrackService();
                    serviceRunning=false;
                }

            }
        });


        historyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this,ListActivity.class);
                startActivity(intent);

            }
        });

        Toast.makeText(this,R.string.update_frequency_message,Toast.LENGTH_SHORT).show();

    }

    private void initBroadcastReceiver() {

        intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.SEND_BROADCAST_LOCATION_UPDATE);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                Log.e(LOG_TAG,"received broadcast");


                if(intent.getAction().equals(Constants.SEND_BROADCAST_LOCATION_UPDATE)){

                    removeMapPolyLines();
                    removeMapMarkers();

                    currentShift = sqLiteDBHelper.getCurrentShift();

                    addMapPolyLines();

                    Location currentLocation = intent.getParcelableExtra(Constants.SEND_BROADCAST_LOCATION_UPDATE_EXTRA);
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude()),
                            15.0f
                    ));

                    if(currentShift.getLocations().size()>0){
                       addStartMarker(currentShift.getLocations().get(0).getLatLng(), currentShift.getLocations().get(0).getTimeStamp());
                    }

                    if(currentShift.getLocations().size()>1){
                        addEndMarker(currentShift.getLocations().get(currentShift.getLocations().size()-1).getLatLng(),currentShift.getLocations().get(currentShift.getLocations().size()-1).getTimeStamp());
                    }

                    durationTV.setText(Utils.convertMillistoDuration(System.currentTimeMillis() - currentShift.getStartTime()));

                }

            }
        };

    }

    private void addMapPolyLines() {

        PolylineOptions polyLineOptions = new PolylineOptions();

        for(ShiftLocation shiftLocation : currentShift.getLocations()){
            polyLineOptions.add(shiftLocation.getLatLng());
        }

        polyLineOptions.width(12);
        polyLineOptions.color(Color.BLUE);

        Polyline polyline = map.addPolyline(polyLineOptions);
        mapPolyLines.add(polyline);

    }

    private void addStartMarker(LatLng latLng, long timeStamp){

        Marker marker = map.addMarker(new MarkerOptions()
                .position(latLng)
                .title("Start")
                .snippet(Utils.convertMillisToDateString(timeStamp))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

        mapMarkers.add(marker);

    }

    private void addEndMarker(LatLng latLng, long timeStamp){

        Marker marker = map.addMarker(new MarkerOptions()
                .position(latLng)
                .title("End")
                .snippet(Utils.convertMillisToDateString(timeStamp))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));

        mapMarkers.add(marker);

    }

    private void removeMapPolyLines() {

        if(mapPolyLines!=null){

            for(Polyline polyline : mapPolyLines){
                polyline.remove();
            }
            mapPolyLines.clear();
        }
    }

    private void removeMapMarkers(){

        if(mapMarkers!=null){

            for(Marker marker : mapMarkers){
                marker.remove();
            }
            mapMarkers.clear();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        LocalBroadcastManager.getInstance(this).registerReceiver(receiver,intentFilter);

    }

    @Override
    protected void onStop() {
        super.onStop();

        if(receiver!=null){
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(sqLiteDBHelper!=null)
            sqLiteDBHelper.close();

    }

    private void stopLocationTrackService() {

        stopService(new Intent(this, LocationTrackService.class));

    }

    private void startLocationTrackService() {

        startService(new Intent(this,LocationTrackService.class));

    }

    private void initMap() {

        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                map = googleMap;
                if (!Utils.checkLocationPermission(MainActivity.this)) {
                    Utils.requestLocationPermission(MainActivity.this,PERMISSIONS_REQUEST_LOCATION_MAP);
                } else {
                    setupMap();
                }
            }
        });

    }

    private void setupMap() {

//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//
//        }
//        else{
//            map.setMyLocationEnabled(true);
//        }
        map.getUiSettings().setZoomControlsEnabled(true);
//        map.moveCamera(CameraUpdateFactory.newLatLngZoom(PUNE_LOCATION, 12.0f));

    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_LOCATION_MAP:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setupMap();
                }
                else{
                    Toast.makeText(getApplicationContext(),getString(R.string.no_location_permssion_message),Toast.LENGTH_LONG).show();
                }
                break;

            case PERMISSIONS_REQUEST_LOCATION_SWITCH:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    shiftSwitch.setChecked(true);
                }
                else{
                    Toast.makeText(getApplicationContext(),getString(R.string.no_location_permssion_message),Toast.LENGTH_LONG).show();
                }
                break;

        }
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
