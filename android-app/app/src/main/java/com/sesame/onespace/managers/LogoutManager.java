package com.sesame.onespace.managers;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.sesame.onespace.activities.LoginActivity;
import com.sesame.onespace.databases.Database;
import com.sesame.onespace.service.MessageService;

/**
 * Created by chongos on 11/13/15 AD.
 */
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
