package com.sesame.onespace.managers.location;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by Thian on 12/11/2559.
 */

public abstract class UserLocationManager {

    //===========================================================================================================//
    //  ATTRIBUTE                                                                                   ATTRIBUTE
    //===========================================================================================================//

    private static Boolean isReady = false;

    private static double latitude = 0;
    private static double longitude = 0;

    //===========================================================================================================//
    //  METHOD                                                                                      METHOD
    //===========================================================================================================//

    public static Boolean isReady(){

        return isReady;

    }

    public static void setLatitude(double latitude1){

        isReady = true;

        latitude = latitude1;

    }

    public static void setLongitude(double longitude1){

        isReady = true;

        longitude = longitude1;

    }

    public static double getLatitude(){

        return latitude;

    }

    public static double getLongitude(){

        return longitude;

    }

    public static Location getLocation(){

        Location location = new Location("MyLocation");
        location.setLatitude(latitude);
        location.setLongitude(longitude);

        return location;

    }

    public static Address getAddress(final Context context){

        Address address = null;

        Geocoder geocoder;
        List<Address> addresses;

        geocoder = new Geocoder(context, Locale.getDefault());

        try {

            addresses = geocoder.getFromLocation(latitude, longitude, 1);
            address = addresses.get(0);

        } catch (IOException e) {

            e.printStackTrace();

        }

        return address;

    }

}
