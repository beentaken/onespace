package com.sesame.onespace.service.fcm;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.sesame.onespace.managers.SettingsManager;

/**
 * Created by christian on 1/2/17.
 */

public class FCMInitializationService extends FirebaseInstanceIdService {
    private static final String TAG = "FCMInitService";

    @Override
    public void onTokenRefresh() {
        String fcmToken = FirebaseInstanceId.getInstance().getToken();

        Log.d(TAG, "FCM Device Token = " + fcmToken);

        //Save or send FCM registration token
        SettingsManager.getSettingsManager(getApplicationContext()).saveStringSetting("fcm_token", fcmToken);

    }

}