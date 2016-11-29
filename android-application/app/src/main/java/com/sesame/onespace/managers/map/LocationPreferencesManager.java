package com.sesame.onespace.managers.map;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.maps.model.LatLng;
import com.sesame.onespace.models.map.FilterMarkerNode;

/**
 * Created by chongos on 11/13/15 AD.
 */
public class LocationPreferencesManager {

    private static final String PREF_NAME = "location_preferences";
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGTITUDE = "longtitude";

    private static LocationPreferencesManager instance;

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    private LocationPreferencesManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public static LocationPreferencesManager getPreferencesManager(Context context) {
        if(instance == null) {
            instance = new LocationPreferencesManager(context);
        }
        return instance;
    }

    public void saveFilterCategoryStates(FilterMarkerNode filterMarkerNode) {
        for(FilterMarkerNode cat: filterMarkerNode.getAllSubCategory()) {
            editor.putBoolean(cat.getName(), cat.isSelected());
            for(FilterMarkerNode sub: cat.getAllSubCategory()) {
                editor.putBoolean(sub.getName(), sub.isSelected());
            }
        }
        editor.commit();
    }

    public void loadFilterCategoryStates(FilterMarkerNode filterMarkerNode) {
        for(FilterMarkerNode cat: filterMarkerNode.getAllSubCategory()) {
            cat.setSelected(prefs.getBoolean(cat.getName(), false));
            for(FilterMarkerNode sub: cat.getAllSubCategory()) {
                sub.setSelected(prefs.getBoolean(sub.getName(), false));
            }
        }
    }

    public void saveLocation(LatLng latLng) {
        editor.putString(KEY_LATITUDE, String.valueOf(latLng.latitude));
        editor.putString(KEY_LONGTITUDE, String.valueOf(latLng.longitude));
        editor.commit();
    }

    public LatLng getLocation() {
        double lat = Double.parseDouble(prefs.getString(KEY_LATITUDE, "1.290270"));
        double lng = Double.parseDouble(prefs.getString(KEY_LONGTITUDE, "103.851959"));

        return new LatLng(lat, lng);
    }


}
