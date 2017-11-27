package com.sesame.onespace.service.fcm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;

import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.JsonObject;
import com.sesame.onespace.R;
import com.sesame.onespace.activities.MainActivity;
import com.sesame.onespace.managers.SettingsManager;
import com.sesame.onespace.models.chat.ChatMessage;
import com.sesame.onespace.service.xmpp.QAMessageManager;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by christian on 1/2/17.
 */

public class FCMCallbackService extends FirebaseMessagingService {
    private static final String TAG = "FCMCallbackService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        //Log.d(TAG, ">>>>>>>>>>> From:" + remoteMessage.getFrom());
        //Log.d(TAG, ">>>>>>>>>>> Message Body:" + remoteMessage.getNotification().getBody());
        //Log.d(TAG, ">>>>>>>>>>> Message Data:" + remoteMessage.getData());
        //sendNotification(remoteMessage.getNotification());

        String msgFrom;
        String msgType;
        String msgBody;

        JSONObject dataJson;
        dataJson = new JSONObject(remoteMessage.getData());
        try {
            msgFrom = dataJson.getString("msgfrom");
            msgBody = dataJson.getString("msgbody");
            JSONObject bodyJson = new JSONObject(msgBody);
            msgType = bodyJson.getString("message-type");
        } catch (JSONException e) {
            Log.e(TAG, "[ERROR] FCMCallbackService.onMessageReceived() " + e);
            return;
        }

        if (msgType.equalsIgnoreCase(ChatMessage.MessageType.QUERY.getString())){
            QAMessageManager.getInstance(getApplicationContext()).addMessageBody(msgFrom, msgBody, "FCM");
        }
    }



//    private void sendNotification(RemoteMessage.Notification notification) {
//        //int color = getResources().getColor(R.color.not);
//        Log.i(TAG, ">>>>>>>> sendNotification");
//
//        Intent intent = new Intent(this, MainActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
//                PendingIntent.FLAG_ONE_SHOT);
//
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
//                //.setContentTitle(notification.getTitle())
//                //.setContentText(notification.getBody())
//                .setContentTitle("OneSpace Notification:")
//                .setContentText("You have received new questions.")
//                .setAutoCancel(true)
//                .setSmallIcon(R.drawable.ic_app_notification)
//                //.setColor(color)
//                .setStyle(new NotificationCompat.BigTextStyle().bigText("You have received new questions."))
//                .setContentIntent(pendingIntent);
//
//        Notification builtNotification = builder.build();
//
//        SettingsManager settingsManager = SettingsManager.getSettingsManager(getApplicationContext());
//
//        if (settingsManager.notificationSound)
//            builtNotification.sound = Uri.parse(settingsManager.notificationRingtone);
//
//        if (settingsManager.notificationVibrate)
//            builtNotification.vibrate = new long[]{25, 100, 100, 200};
//
//        Log.i(TAG, ">>>>>>>> LED setting???");
//        if (settingsManager.notificationLED) {
//            Log.i(TAG, ">>>>>>>> LED setting");
//            builtNotification.ledOnMS = 1000;
//            builtNotification.ledOffMS = 2000;
//            builtNotification.ledARGB = Color.MAGENTA;
//            builtNotification.flags |= Notification.FLAG_SHOW_LIGHTS;
//        }
//
//
//        NotificationManager notificationManager = (NotificationManager)
//                getSystemService(Context.NOTIFICATION_SERVICE);
//        com.sesame.onespace.utils.Log.i(">>>>>>>>>>>>>> Push put FCM Notification");
//        notificationManager.notify(111222, builtNotification);
//    }


}