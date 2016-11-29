package com.sesame.onespace.network;

import android.content.Context;

import com.google.android.gms.maps.model.LatLngBounds;
import com.sesame.onespace.models.map.FilterMarkerNode;
import com.sesame.onespace.models.map.FilterMarkerNode.OnFilterChangeListener;
import com.sesame.onespace.network.OneSpaceApi;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by chongos on 8/19/15 AD.
 */
public abstract class MapMarkerLoader implements OnFilterChangeListener {

    protected Context context;
    protected OneSpaceApi.Service api;
    protected com.squareup.otto.Bus bus;
    protected HashMap<String, Object> markerHashMap;
    protected FilterMarkerNode filter;

    public MapMarkerLoader(Context context, OneSpaceApi.Service api, com.squareup.otto.Bus bus) {
        this.context = context;
        this.api = api;
        this.bus = bus;
        this.markerHashMap = new HashMap<>();
    }

    public ArrayList<Object> getAll() {
        ArrayList<Object> arr = new ArrayList<>();
        arr.addAll(markerHashMap.values());
        return arr;
    }

    public void setFilter(FilterMarkerNode filter) {
        this.filter = filter;
        this.filter.setOnFilterChageListener(this);
    }

    protected void publishAddResult(ArrayList<Object> result) {
        bus.post(new AddMarkerResultEvent(result));
    }

    protected void publishRemoveResult(ArrayList<Object> result) {
        bus.post(new RemoveMarkerResultEvent(result));
    }

    @Override
    public void onFilterChange(FilterMarkerNode filterMarkerNode) {
        ArrayList<Object> markers = new ArrayList<>();
        markers.addAll(markerHashMap.values());
        if(filter.isSelected())
            publishAddResult(markers);
        else
            publishRemoveResult(markers);
    }

    public abstract void fetch(LatLngBounds bounds, int limit);

    public class AddMarkerResultEvent {

        ArrayList<Object> result;

        public AddMarkerResultEvent(ArrayList<Object> result) {
            this.result = result;
        }

        public ArrayList<Object> getResult() {
            return result;
        }

    }

    public class RemoveMarkerResultEvent {

        ArrayList<Object> result;

        public RemoveMarkerResultEvent(ArrayList<Object> result) {
            this.result = result;
        }

        public ArrayList<Object> getResult() {
            return result;
        }


    }

    public static class Bus {

        private static final com.squareup.otto.Bus BUS = new com.squareup.otto.Bus();

        public static com.squareup.otto.Bus getInstance() {
            return BUS;
        }

    }

}
