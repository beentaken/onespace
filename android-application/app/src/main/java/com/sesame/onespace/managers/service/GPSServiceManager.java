package com.sesame.onespace.managers.service;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;

import com.sesame.onespace.service.gps.GPSTrackerService;

/**
 * Created by Thian on 16/11/2559.
 */

public abstract class GPSServiceManager {

    //===========================================================================================================//
    //  ATTRIBUTE                                                                                   ATTRIBUTE
    //===========================================================================================================//

    private final static Class<?> serviceClass = GPSTrackerService.class;

    //===========================================================================================================//
    //  METHOD                                                                                      METHOD
    //===========================================================================================================//

    public static void startGPSService(Context context, String userid){

        //always run in background
        Intent intent = new Intent(context, serviceClass);
        intent.putExtra("userid", userid);
        context.startService(intent);

    }

    public static void stopGPSService(Context context){

        Intent intent = new Intent(context, serviceClass);
        context.stopService(intent);

    }

    public static boolean isGPSServiceRunning(Context context) {

        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;

    }

}
