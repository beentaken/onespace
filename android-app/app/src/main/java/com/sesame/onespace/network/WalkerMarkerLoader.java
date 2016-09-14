package com.sesame.onespace.network;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLngBounds;
import com.sesame.onespace.models.map.Walker;

import java.util.ArrayList;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;

/**
 * Created by chongos on 8/19/15 AD.
 */
public class WalkerMarkerLoader extends MapMarkerLoader {

    private Call call;
    private String userid = "";

    public WalkerMarkerLoader(Context context, OneSpaceApi.Service api, com.squareup.otto.Bus bus) {
        super(context, api, bus);
    }

    public void setUsername(String id) {
        this.userid = id;
    }

    @Override
    public void fetch(LatLngBounds bounds, int limit) {
        if(!filter.isSelected())
            return;

        if(call != null)
            call.cancel();

        call = api.getWalkers(bounds.northeast.latitude,
                bounds.northeast.longitude,
                bounds.southwest.latitude,
                bounds.southwest.longitude,
                limit);

        call.enqueue(new Callback<ArrayList<Walker>>() {
            @Override
            public void onResponse(final Response<ArrayList<Walker>> response) {
                if (response.isSuccess()) {
                    new AsyncTask<Void, Void, ArrayList<Object>>() {

                        @Override
                        protected ArrayList<Object> doInBackground(Void... params) {
                            ArrayList<Object> result = new ArrayList<>();
                            for (Walker walker : response.body()) {
                                String id = walker.getUserName();

                                if (!id.equals(userid) && !markerHashMap.containsKey(id)) {
                                    markerHashMap.put(id, walker);
                                    result.add(walker);
                                }
                            }
                            return result;
                        }

                        @Override
                        protected void onPostExecute(ArrayList<Object> objects) {
                            super.onPostExecute(objects);
                            publishAddResult(objects);
                        }
                    }.execute();
                } else {
                    Log.i("GET Walker", response.message());
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e("GetWalker", t.toString());
            }
        });

    }

}
