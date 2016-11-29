package com.sesame.onespace.constant;

import android.content.Context;

import com.sesame.onespace.R;

/**
 * Created by chongos on 10/15/15 AD.
 */
public class MapMarkerIconSelector {

    private static MapMarkerIconSelector instance;

    private Context context;

    public static synchronized MapMarkerIconSelector getInstance(Context context) {
        if (instance == null)
            instance = new MapMarkerIconSelector(context);
        return instance;
    }

    private MapMarkerIconSelector(Context context) {
        this.context = context;
    }

    public int getPlaceMarker(String str) {
        if(str.equals(context.getString(R.string.display_places_transport)))
            return R.drawable.ic_place_transport;
        else if(str.equals(context.getString(R.string.display_places_accommodation)))
            return R.drawable.ic_place_accommodation;
        else if(str.equals(context.getString(R.string.display_places_amusement)))
            return R.drawable.ic_place_amusement;
        else if(str.equals(context.getString(R.string.display_places_education)))
            return R.drawable.ic_place_education;
        else if(str.equals(context.getString(R.string.display_places_establishment)))
            return R.drawable.ic_place_establishment;
        else if(str.equals(context.getString(R.string.display_places_finance)))
            return R.drawable.ic_place_finance;
        else if(str.equals(context.getString(R.string.display_places_food)))
            return R.drawable.ic_place_food;
        else if(str.equals(context.getString(R.string.display_places_government)))
            return R.drawable.ic_place_government;
        else if(str.equals(context.getString(R.string.display_places_health_and_wellness)))
            return R.drawable.ic_place_hospital;
        else if(str.equals(context.getString(R.string.display_places_place_of_worship)))
            return R.drawable.ic_place_worship;
        else if(str.equals(context.getString(R.string.display_places_recreation)))
            return R.drawable.ic_place_recreation;
        else if(str.equals(context.getString(R.string.display_places_service)))
            return R.drawable.ic_place_service;
        else if(str.equals(context.getString(R.string.display_places_shopping)))
            return R.drawable.ic_place_shopping;

        return R.drawable.ic_place_establishment;
    }

    public int getSurferMarker() {
        return R.drawable.ic_pin_surfer;
    }

    public int getWalkerMarker() {
        return R.drawable.ic_pin_walker;
    }

    public int getCornerMarker(boolean isMine) {
        if(isMine)
            return R.drawable.ic_pin_corner_red_dot;
        return R.drawable.ic_pin_corner;
    }

}
