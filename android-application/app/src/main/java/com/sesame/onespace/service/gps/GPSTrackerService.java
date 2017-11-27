package com.sesame.onespace.service.gps;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.sesame.onespace.managers.UserAccountManager;
import com.sesame.onespace.network.OneSpaceApi;

import retrofit.GsonConverterFactory;
import retrofit.RxJavaCallAdapterFactory;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by Thian on 12/11/2559.
 */

public final class GPSTrackerService
        extends Service {

    //===========================================================================================================//
    //  ATTRIBUTE                                                                                   ATTRIBUTE
    //===========================================================================================================//

    private static final String TAG = "GPSTrackerService";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 500;
    private static final float LOCATION_DISTANCE = 10f;

    LocationListener[] mLocationListeners = new LocationListener[] {
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    private String userid;

    private MyReceiver receiver;
    private double latitude;
    private double longitude;

    //===========================================================================================================//
    //  ON ACTION                                                                                   ON ACTION
    //===========================================================================================================//

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if (intent == null) {
            stopSelf();
        }
        else{

            this.userid = intent.getStringExtra("userid");

        }

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        initializeLocationManager();

        try {
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE, mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }

        try {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE, mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }

        this.receiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("GPSTrackerService2");
        registerReceiver(this.receiver, intentFilter);

        Location location = null;
        try {
            location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        } catch (SecurityException e) {
            Log.d(TAG, "GPSTrackerService.onCreate() No permission the use LocationManager.GPS_PROVIDER " + e.getMessage());
        }

        if (location == null) {
            try {
                location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            } catch (SecurityException e) {
                Log.d(TAG, "GPSTrackerService.onCreate() No permission the use LocationManager.NETWORK_PROVIDER " + e.getMessage());
            }

        }

        if (location != null){

            latitude = location.getLatitude();
            longitude = location.getLongitude();

            this.userid = UserAccountManager.getInstance(getApplicationContext()).getUserID();

            Observable<String> observable = new OneSpaceApi.Builder(getApplicationContext())
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .build()
                    .updateGeoLocationRx(userid,
                            location.getLatitude(),
                            location.getLongitude());

            observable.subscribe(new Subscriber<String>() {
                @Override
                public void onCompleted() {
                    com.sesame.onespace.utils.Log.i("PutLocation completed");
                }

                @Override
                public void onError(Throwable e) {
                    e.printStackTrace();
                }

                @Override
                public void onNext(String s) {

                }
            });

            Intent intent = new Intent();
            intent.setAction("GPSTrackerService");
            intent.putExtra("latitude", latitude);
            intent.putExtra("longitude", longitude);
            sendBroadcast(intent);

        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver(this.receiver);

        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {

                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listners, ignore", ex);
                }

            }
        }

    }

    //===========================================================================================================//
    //  INIT                                                                                        INIT
    //===========================================================================================================//

    private void initializeLocationManager() {

        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }

    }

    //===========================================================================================================//
    //  PRIVATE CLASS                                                                               PRIVATE CLASS
    //===========================================================================================================//

    private final class LocationListener
            implements android.location.LocationListener {

        Location mLastLocation;

        public LocationListener(String provider) {
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {

            mLastLocation.set(location);

            latitude = location.getLatitude();
            longitude = location.getLongitude();

            Observable<String> observable = new OneSpaceApi.Builder(getApplicationContext())
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .build()
                    .updateGeoLocationRx(userid,
                            location.getLatitude(),
                            location.getLongitude());

            observable.subscribe(new Subscriber<String>() {
                @Override
                public void onCompleted() {
                    com.sesame.onespace.utils.Log.i("PutLocation completed");
                }

                @Override
                public void onError(Throwable e) {
                    e.printStackTrace();
                }

                @Override
                public void onNext(String s) {

                }
            });

            Intent intent = new Intent();
            intent.setAction("GPSTrackerService");
            intent.putExtra("latitude", latitude);
            intent.putExtra("longitude", longitude);
            sendBroadcast(intent);

        }

        @Override
        public void onProviderDisabled(String provider) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

    }

    //------------------------------------------------------------------------------------------------------------//

    private final class MyReceiver
            extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent arg1) {

            if (arg1.getStringExtra("want location again!!!").equals(new String("yes"))){

                //Toast.makeText(arg0, "Your Message", Toast.LENGTH_LONG).show();

                Intent intent = new Intent("GPSTrackerService");
                intent.putExtra("latitude", latitude);
                intent.putExtra("longitude", longitude);
                arg0.sendBroadcast(intent);

            }

        }

    }

}
