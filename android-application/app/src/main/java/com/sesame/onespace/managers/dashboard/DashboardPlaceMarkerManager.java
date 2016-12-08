package com.sesame.onespace.managers.dashboard;

import android.location.Location;

import com.sesame.onespace.models.map.Place;

/**
 * Created by Thian on 7/12/2559.
 */

public abstract class DashboardPlaceMarkerManager {

    //===========================================================================================================//
    //  ATTRIBUTE                                                                                   ATTRIBUTE
    //===========================================================================================================//

    private static boolean isReady = false;

    private static Place place;

    //===========================================================================================================//
    //  METHOD                                                                                      METHOD
    //===========================================================================================================//

    public static boolean isPlaceMarker(){

        //init
        boolean isPlaceMarker = false;

        //before
        if (isReady == false){

            isReady = true;

        }

        //main
        if (place != null){

            isPlaceMarker = true;

        }

        //after


        return isPlaceMarker;

    }

    public static void setPlace(Place place1){

        place = place1;

    }

    public static Place getPlace(){

        return place;

    }

}
