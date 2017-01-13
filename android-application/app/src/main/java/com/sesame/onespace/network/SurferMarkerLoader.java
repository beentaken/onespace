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

// Modified code by Thianchai on 11/10/16
public class SurferMarkerLoader extends MapMarkerLoader {

    //===========================================================================================================//
    //  ATTRIBUTE                                                                                   ATTRIBUTE
    //===========================================================================================================//

    private Call call;

    //Thianchai (I add this)
    private Thread thread;
    private boolean shouldContinue;
    private LatLngBounds myBounds;
    private int myLimit;
    private boolean isRunning;
    private static Boolean isFilter = false;
    //**

    //===========================================================================================================//
    //  INIT                                                                                        INIT
    //===========================================================================================================//

    public SurferMarkerLoader(Context context, OneSpaceApi.Service api, com.squareup.otto.Bus bus) {
        super(context, api, bus);

        this.isRunning = false;
    }

    //===========================================================================================================//
    //  METHOD BY chongos                                                                           METHOD BY chongos
    //===========================================================================================================//

    @Override
    public void fetch(LatLngBounds bounds, int limit) {
        if (!filter.isSelected())
            return;

        //Thianchai (I add this.)
        this.myBounds = bounds;
        this.myLimit = limit;
        //**

        //Thianchai (I add this)
        Thread thread = new Thread(){

            @Override
            public void run(){

                if (myBounds != null && isRunning == false){

                    isRunning = true;

                    if (call != null){

                        call.cancel();

                    }

                    call = api.getSurfers(myBounds.northeast.latitude,
                            myBounds.northeast.longitude,
                            myBounds.southwest.latitude,
                            myBounds.southwest.longitude,
                            myLimit);

                    //clone()
                    call.enqueue(new Callback<ArrayList<Surfer>>() {
                        @Override
                        public void onResponse(final Response<ArrayList<Surfer>> response) {
                            if (response.isSuccess()) {

                                updateSurferMarker(response);

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

        };

        thread.start();
        //**

        //Thianchai (I delete this)
//        if (call != null)
//            call.cancel();
//
//
//        call = api.getSurfers(bounds.northeast.latitude,
//                bounds.northeast.longitude,
//                bounds.southwest.latitude,
//                bounds.southwest.longitude,
//                limit);
//
//        call.enqueue(new Callback<ArrayList<Surfer>>() {
//            @Override
//            public void onResponse(final Response<ArrayList<Surfer>> response) {
//                if (response.isSuccess())
//                    new AsyncTask<Void, Void, ArrayList<Object>>() {
//
//                        @Override
//                        protected ArrayList<Object> doInBackground(Void... params) {
//                            ArrayList<Object> result = new ArrayList<>();
//                            for (Surfer surfer : response.body()) {
//                                String name = surfer.getUserName();
//
//                                if (!markerHashMap.containsKey(name)) {
//                                    markerHashMap.put(name, surfer);
//                                    result.add(surfer);
//                                }
//                            }
//                            return result;
//                        }
//
//                        @Override
//                        protected void onPostExecute(ArrayList<Object> objects) {
//                            super.onPostExecute(objects);
//                            publishAddResult(objects);
//                        }
//                    }.execute();
//            }
//
//            @Override
//            public void onFailure(Throwable t) {
//                Log.e("GetSurfer", t.toString());
//            }
//        });
        //**

    }

    //===========================================================================================================//
    //  METHOD BY Thianchai                                                                         METHOD BY Thianchai
    //===========================================================================================================//

    public static Boolean getIsFilter(){

        return isFilter;

    }

    public static void setIsFilter(Boolean b){

        isFilter = b;

    }

    public void startThread(){

        this.shouldContinue = true;

        this.thread = new Thread(){

            @Override
            public void run(){

                while(shouldContinue){

                    if (isFilter == true){

                        if (myBounds != null && isRunning == false){

                            isRunning = true;

                            if (call != null){

                                call.cancel();

                            }

                            call = api.getSurfers(myBounds.northeast.latitude,
                                    myBounds.northeast.longitude,
                                    myBounds.southwest.latitude,
                                    myBounds.southwest.longitude,
                                    myLimit);

                            //clone()
                            call.enqueue(new Callback<ArrayList<Surfer>>() {
                                @Override
                                public void onResponse(final Response<ArrayList<Surfer>> response) {
                                    if (response.isSuccess()) {

                                        updateSurferMarker(response);

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

                    try {

                        Thread.sleep(5000);

                    } catch (InterruptedException e) {

                        e.printStackTrace();

                    }

                }

            }

        };

        this.thread.start();

    }

    public void stopThread(){

        this.shouldContinue = false;

        //Thianchai (Note if wait thread finish, it's slow.)
//        try {
//            this.thread.join();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        //**

    }

    //-------------------------------------------------------------------------------------------------------//

    private void updateSurferMarker(final Response<ArrayList<Surfer>> response){

        new AsyncTask<Void, Void, ArrayList<Object>>() {

            @Override
            protected ArrayList<Object> doInBackground(Void... params) {
                ArrayList<Object> result = new ArrayList<>();
                ArrayList<Object> oldSurferWalker = new ArrayList<>();
                ArrayList<Object> newSurferWalker = new ArrayList<>();

                result.add(oldSurferWalker);
                result.add(newSurferWalker);

                for (Object marker : markerHashMap.values()){

                    if (marker instanceof Surfer){

                        oldSurferWalker.add(marker);

                    }

                }

                markerHashMap.clear();

                for (Surfer surfer : response.body()) {
                    String name = surfer.getUserName();

                    markerHashMap.put(name, surfer);
                    newSurferWalker.add(surfer);

                }

                return result;
            }

            @Override
            protected void onPostExecute(final ArrayList<Object> objects) {
                super.onPostExecute(objects);
                publishRemoveResult((ArrayList<Object>) objects.get(0));
                publishAddResult((ArrayList<Object>) objects.get(1));
                isRunning = false;
            }
        }.execute();

    }

}