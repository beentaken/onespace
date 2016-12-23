package com.sesame.onespace.managers;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.sesame.onespace.activities.LoginActivity;
import com.sesame.onespace.databases.Database;
import com.sesame.onespace.managers.service.GPSServiceManager;
import com.sesame.onespace.network.OneSpaceApi;
import com.sesame.onespace.service.MessageService;

import retrofit.GsonConverterFactory;
import retrofit.RxJavaCallAdapterFactory;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by chongos on 11/13/15 AD.
 */

// Modified code by Thianchai on 16/10/16

public class LogoutManager {

    private static LogoutManager instance;
    private Context context;

    private LogoutManager(Context context) {
        this.context = context;
    }

    public static LogoutManager getInstance(Context context) {
        if (instance == null)
            instance = new LogoutManager(context);
        return instance;
    }

    public void logout() {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                // Stop MessageService
                context.stopService(new Intent(context, MessageService.class));

                //Thianchai (I add this)
                if (GPSServiceManager.isGPSServiceRunning(context)){

                    GPSServiceManager.stopGPSService(context);

                }
                //**

                //Thianchai (I add this)
                //Latitude, Longitude for user that status marker in map = don't show me in map
                double latitude = 1000;
                double longitude = 1000;

                Observable<String> observable = new OneSpaceApi.Builder(context)
                        .addConverterFactory(GsonConverterFactory.create())
                        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                        .build()
                        .updateGeoLocationRx(UserAccountManager.getInstance(context).getUserID(),
                                latitude,
                                longitude);

                observable.subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                        com.sesame.onespace.utils.Log.i("PutLocation completed");
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(String s) {

                    }
                });
                //**

            }

            @Override
            protected Void doInBackground(Void... params) {
                SettingsManager.getSettingsManager(context).destroy();
                Database.Cleaner.getCleaner(context).cleanAll();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                // After logout redirect user to Loing Activity
                Intent i = new Intent(context, LoginActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                context.startActivity(i);
            }

        }.execute();


    }

}
