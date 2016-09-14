package com.sesame.onespace.network;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLngBounds;
import com.sesame.onespace.models.map.Surfer;

import java.util.ArrayList;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;

/**
 * Created by chongos on 8/19/15 AD.
 */
public class SurferMarkerLoader extends MapMarkerLoader {

    private Call call;

    public SurferMarkerLoader(Context context, OneSpaceApi.Service api, com.squareup.otto.Bus bus) {
        super(context, api, bus);
    }

    @Override
    public void fetch(LatLngBounds bounds, int limit) {
        removeSurferMarker();

        if (!filter.isSelected())
            return;

        if (call != null)
            call.cancel();

        call = api.getSurfers(bounds.northeast.latitude,
                bounds.northeast.longitude,
                bounds.southwest.latitude,
                bounds.southwest.longitude,
                limit);

        call.enqueue(new Callback<ArrayList<Surfer>>() {
            @Override
            public void onResponse(final Response<ArrayList<Surfer>> response) {
                if (response.isSuccess())
                    new AsyncTask<Void, Void, ArrayList<Object>>() {

                        @Override
                        protected ArrayList<Object> doInBackground(Void... params) {
                            ArrayList<Object> result = new ArrayList<>();
                            //surferHashMap.clear();
                            for (Surfer surfer : response.body()) {
                                String name = surfer.getUserName();
                                String vloc = surfer.getVloc();
                                String surferMarkerId = name+"-"+vloc;

                                surferHashMap.put(surferMarkerId, surfer);
                                //if (!markerHashMap.containsKey(surferMarkerId)) {
                                markerHashMap.put(surferMarkerId, surfer);
                                result.add(surfer);
                                //}
                            }


                            return result;
                        }

                        @Override
                        protected void onPostExecute(ArrayList<Object> objects) {
                            super.onPostExecute(objects);
                            //ArrayList<Object> surferMarkers = new ArrayList<>();
                            //surferMarkers.addAll(surferHashMap.values());
                            publishAddResult(objects);
                        }
                    }.execute();
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e("GetSurfer", t.toString());
            }
        });

    }

}