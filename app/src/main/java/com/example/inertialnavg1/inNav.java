package com.example.inertialnavg1;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;
import java.lang.Math;

import static java.lang.Math.PI;

public class inNav extends Activity implements SensorEventListener {
    private Context vCtx;
    private Activity vAct;
    int ExecTracker = 0; //TODO test this later to see if it does its job

    String provider = LocationManager.GPS_PROVIDER;

    inNav(Context _Ctx, Activity _Act){
        vCtx = _Ctx;
        vAct = _Act;
        AlignmentManager();
    }

    private void AlertHandler(){
        AlertDialog.Builder GPSAlert = new AlertDialog.Builder(vCtx);
        GPSAlert.setMessage(R.string.AlertString);
        GPSAlert.setCancelable(true);

        GPSAlert.setPositiveButton(
                R.string.EnableGPS,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //automate later
                        dialog.cancel();
                        //Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        //startActivity(settingsIntent);
                    }
                });

        GPSAlert.setNegativeButton(
                R.string.CancelGPS,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        CharSequence CancelText = "WARNING: app cannot function without GPS initially";
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(vCtx, CancelText, duration);
                        toast.show();
                        AlertHandler();
                    }
                });

        AlertDialog Alert = GPSAlert.create();
        Alert.show();
    }

    public void AlignmentManager(){

        LocationManager lm = (LocationManager) vCtx.getSystemService(Context.LOCATION_SERVICE);
        boolean gpsEnabled;
        try{
            gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }
        catch (NullPointerException ex){
            Log.d("AlignmentManager", "AlignmentManager gps enabled null", ex);
            throw ex;
        }

        if(!gpsEnabled){
            AlertHandler();
        }
        else{
            if (ActivityCompat.checkSelfPermission(vCtx, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(vCtx, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling (HANDLED in MAIN ACT)
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
            }

            LocationListener ll = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) { //TODO to null checking on these before using
                    Log.d("onLocationChanged", "onLocationChanged:");
                    //String lon = String.valueOf(location.getLongitude());
                    //String FullStr = lon; //for testing will pass them to the inertial calculator later
                    //Toast.makeText(vCtx, FullStr, Toast.LENGTH_LONG).show();
                    AlignmentAdapter(location,0);
                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {
                    Log.d("onStatusChanged", "onStatusChanged:");
                    //add later
                }

                @Override
                public void onProviderEnabled(String s) {
                    //add later
                }

                @Override
                public void onProviderDisabled(String s) {
                    //add later
                }
            };

            lm.requestSingleUpdate(provider,ll,null);
        }
    }

    Location GLocation;
    public void AlignmentAdapter(Location location, float acceleration){ //TODO split in half maybe ??
        int radius;
        double dTime = 0.5; //not connected to sample (for later prototype)

        GLocation = location;

        double lat = location.getLatitude();
        double lon = location.getLongitude();
        double heading = location.getBearing();

        //TODO map marker update here


        if (lat > 45){
            radius = 6356000;
        }
        else{
            radius = 6378000;
        }

        double deviation  = (90 * acceleration * (dTime * dTime)) / (PI * radius); //displacement is here
        lat = lat + deviation;
        Toast.makeText(vCtx, String.valueOf(lat), Toast.LENGTH_LONG).show();

        //TODO coordinate update here


        if(ExecTracker == 0){ //this is here to make sure the sensor listener is only registered once
            ExecTracker++;
            getAcceleration(500000, GLocation);

        }
        else{
            //listener is already up wait until sensor change triggers
            try{
                wait(600);
            }
            catch(InterruptedException ex){
                Log.d("wait","interrupt exception triggered");
                getAcceleration(500000, GLocation);
            }
        }
        // TODO acc call is here

        //------------------------------------------------------------------------------------------
        //TODO the pitch roll heading needs to be fused to acceleration using -
        //TODO snapshot sin(x) method
        //TODO !!!!!!!!!! the sensors seem to be designed for when the phone is flat only and switch around when device is vertical
        //TODO !!!!!!!!!! this can be overcome by putting the phone flat and then using ADVANCED -
        //TODO !!!!!!!!!! math to continually calculate rotations but that's very cpu intensive and battery wasting
        //TODO !!!!!!!!!!! maybe there's a way to calculate rotation from a snapshot but its VERY unclear at this time


        //TODO sensors:
        //TODO orientation --------------------------------------------------------------------
        //TODO pitch: is working as expected (except it needs to be multiplied by -1)
        //TODO roll: is working when screen rotation is disabled (check the negative val further)
        //TODO azimuth: unreliable, changes with roll and unresponsive with pitch
        //TODO since azimuth is unreliable for snapshots need to calculate using gyros -
        //TODO until phone is resting on flat surface then can take a snapshot
        //TODO ---------------------------------------------------------------------------------
        //TODO Linear acceleration -------------------------------------------------------------
        //TODO !!! it is from sides of the phone the screen basis !!! (no matter the orientation)
        //TODO description in brackets are as if there was a force pushing/pulling perpendicular to/from given sides
        //TODO left-right will be x-axis (long side of phone)
        //TODO towards-away will be y-axis (short side of phone)
        //TODO up-down will be z-axis (into and from screen)
        //TODO ---------------------------------------------------------------------------------
    }

    public void getAcceleration(int interval, Location location){
        //location needs to be passed for calling adapter

        //acc readout is here
        //call to alignment adapter is here
        //-------------------------------------------------
        int sampleDelay = 500000;

        if (interval >= 200000 && interval <= 900000){
            sampleDelay = interval;
        }


        SensorManager mSensorManager;
        Sensor mAccelerometer;
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

        try{
            mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
        catch(NullPointerException ex){
            Log.d("getAcceleration","null at mAcc assignment");
            throw ex;
        }

        mSensorManager.registerListener(this, mAccelerometer, sampleDelay);
        AlignmentAdapter(location,0); // 0 passed to calibrate measurements
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float Xacc = sensorEvent.values[0];
        float Yacc = sensorEvent.values[1];
        float Zacc = sensorEvent.values[2];
        //TODO !!!!!!!! FINAL find a way to put this into alignment adt to calculate deviation

        AlignmentAdapter(GLocation, Xacc);
        //global variable switching maybe ???
    }
}
