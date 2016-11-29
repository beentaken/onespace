package com.sesame.onespace.network;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLngBounds;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.sesame.onespace.R;
import com.sesame.onespace.models.map.FilterMarkerNode;
import com.sesame.onespace.models.map.Place;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;

/**
 * Created by chongos on 8/19/15 AD.
 */
public class PlaceMarkerLoader extends MapMarkerLoader {

    private Call call;
    private Multimap<String, String> mapFilterAndID;
    private List<String> listVisibleCategories;
    private LatLngBounds lastBounds;
    private int lastLimit;

    public PlaceMarkerLoader(Context context, OneSpaceApi.Service api, com.squareup.otto.Bus bus) {
        super(context, api, bus);
        mapFilterAndID = ArrayListMultimap.create();
        listVisibleCategories = new ArrayList<>();
    }

    @Override
    public void setFilter(FilterMarkerNode filter) {
        super.setFilter(filter);

        for(int i=0; i<filter.getSubCategorySize(); i++) {
            FilterMarkerNode sub = filter.getSubCategory(i);
            if (sub.isSelected()) {
                listVisibleCategories.add(sub.getName());
            }
        }
    }

    @Override
    public void fetch(LatLngBounds bounds, int limit) {
        this.lastBounds = bounds;
        this.lastLimit = limit;

        if(!filter.isSelected())
            return;

        if(call != null)
            call.cancel();

        call = api.getPlaces(bounds.northeast.latitude,
                bounds.northeast.longitude,
                bounds.southwest.latitude,
                bounds.southwest.longitude,
                limit);

        call.enqueue(new Callback<ArrayList<Place>>() {
            @Override
            public void onResponse(final Response<ArrayList<Place>> response) {
                if (response.isSuccess()) {
                    new AsyncTask<Void, Void, ArrayList<Object>>() {

                        @Override
                        protected ArrayList<Object> doInBackground(Void... params) {
                            ArrayList<Object> places = new ArrayList<>();
                            for (Place place : response.body()) {
                                String id = String.valueOf(place.getId());
                                String cat = getCategoryClassName(place.getCategoryClass());
                                place.setCategoryClass(cat);

                                if (!markerHashMap.containsKey(id)) {
                                    markerHashMap.put(id, place);
                                    mapFilterAndID.put(cat, id);

                                    if (listVisibleCategories.contains(cat))
                                        places.add(place);
                                }
                            }
                            return places;
                        }

                        @Override
                        protected void onPostExecute(ArrayList<Object> objects) {
                            super.onPostExecute(objects);
                            publishAddResult(objects);
                        }

                    }.execute();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e("GetPlace", t.toString());
            }
        });
    }

    @Override
    public void onFilterChange(final FilterMarkerNode filterMarkerNode) {
        if(filterMarkerNode.equals(filter)) {
            if(filterMarkerNode.isSelected()) {
                new AsyncTask<Void, Void, ArrayList<Object>>() {

                    @Override
                    protected ArrayList<Object> doInBackground(Void... params) {
                        ArrayList<Object> places = new ArrayList<>();

                        for(String visibleCategory : listVisibleCategories) {
                            Collection<String> addPlaces = mapFilterAndID.get(visibleCategory);

                            for (String addPlaceID : addPlaces) {
                                Object aPlace = markerHashMap.get(addPlaceID);
                                places.add(aPlace);
                            }
                        }
                        return places;
                    }

                    @Override
                    protected void onPostExecute(ArrayList<Object> objects) {
                        super.onPostExecute(objects);
                        publishAddResult(objects);
//                        fetch(lastBounds, lastLimit);
                    }

                }.execute();
            } else {
                super.onFilterChange(filterMarkerNode);
            }
        } else {
            new AsyncTask<Void, Void, ArrayList<Object>>() {

                @Override
                protected ArrayList<Object> doInBackground(Void... params) {
                    ArrayList<Object> places = new ArrayList<>();
                    Collection<String> addPlaces = mapFilterAndID.get(filterMarkerNode.getName());

                    for (String addPlaceID : addPlaces) {
                        Object aPlace = markerHashMap.get(addPlaceID);
                        places.add(aPlace);
                    }
                    return places;
                }

                @Override
                protected void onPostExecute(ArrayList<Object> objects) {
                    super.onPostExecute(objects);
                    if (filterMarkerNode.isSelected()) {
                        listVisibleCategories.add(filterMarkerNode.getName());
                        publishAddResult(objects);
                        fetch(lastBounds, lastLimit);
                    } else {
                        listVisibleCategories.remove(filterMarkerNode.getName());
                        publishRemoveResult(objects);
                    }
                }

            }.execute();
        }
    }

    private String getCategoryClassName(String str) {
        if(str.equals("transport"))
            return context.getString(R.string.display_places_transport);
        else if(str.equals("accommodation"))
            return context.getString(R.string.display_places_accommodation);
        else if(str.equals("amusement"))
            return context.getString(R.string.display_places_amusement);
        else if(str.equals("education"))
            return context.getString(R.string.display_places_education);
        else if(str.equals("establishment"))
            return context.getString(R.string.display_places_establishment);
        else if(str.equals("finance"))
            return context.getString(R.string.display_places_finance);
        else if(str.equals("food"))
            return context.getString(R.string.display_places_food);
        else if(str.equals("government"))
            return context.getString(R.string.display_places_government);
        else if(str.equals("health_and_wellness"))
            return context.getString(R.string.display_places_health_and_wellness);
        else if(str.equals("place_of_worship"))
            return context.getString(R.string.display_places_place_of_worship);
        else if(str.equals("recreation"))
            return context.getString(R.string.display_places_recreation);
        else if(str.equals("service"))
            return context.getString(R.string.display_places_service);
        else if(str.equals("shopping"))
            return context.getString(R.string.display_places_shopping);
        else if(str.equals("establishment"))
            return context.getString(R.string.display_places_establishment);

        return null;
    }

}
