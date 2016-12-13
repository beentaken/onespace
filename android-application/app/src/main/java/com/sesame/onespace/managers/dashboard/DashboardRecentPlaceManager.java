package com.sesame.onespace.managers.dashboard;

import android.location.Location;
import android.support.annotation.NonNull;

import com.sesame.onespace.models.map.Place;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;

/**
 * Created by Thian on 7/12/2559.
 */

public abstract class DashboardRecentPlaceManager {

    //===========================================================================================================//
    //  ATTRIBUTE                                                                                   ATTRIBUTE
    //===========================================================================================================//

    private static ArrayList<Place> placeArrayList = new ArrayList<Place>();

    //===========================================================================================================//
    //  METHOD                                                                                      METHOD
    //===========================================================================================================//

    public static void addPlace(Place place){

        boolean check = false;
        int index2 = 0;

        int index = 0;

        while(index < placeArrayList.size()){

            if (placeArrayList.get(index).getName().equals(place.getName()) ){

                check = true;
                index2 = index;

                break;

            }

            index = index + 1;

        }

        if (check == true){

            ArrayList<Place> list = new ArrayList<Place>();

            index = 0;

            while(index < placeArrayList.size()){

                if (index != index2){

                    list.add(placeArrayList.get(index));

                }

                index = index + 1;

            }

            list.add(place);

            placeArrayList = list;

        }
        else{

            if (placeArrayList.size() < 10){

                placeArrayList.add(place);

            }
            else{

                ArrayList<Place> list = new ArrayList<Place>();

                index = 1;

                while (index < 10){

                    list.add(placeArrayList.get(index));

                    index = index + 1;

                }

                list.add(place);

                placeArrayList = list;

            }

        }

    }

    public static ArrayList<Place> getPlaceArrayList(){

        return placeArrayList;

    }

}
