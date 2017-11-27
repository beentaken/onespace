package com.sesame.onespace.service.xmpp;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.NotificationCompat;

import com.sesame.onespace.R;
import com.sesame.onespace.activities.dialogActivities.QAChoiceDialogActivity;
import com.sesame.onespace.activities.dialogActivities.QAImageDialogActivity;
import com.sesame.onespace.databases.qaMessageDatabases.QAMessageHelper;
import com.sesame.onespace.fragments.MainMenuFragment;
import com.sesame.onespace.managers.SettingsManager;
import com.sesame.onespace.managers.service.OnespaceNotificationManager;
import com.sesame.onespace.models.chat.ChatMessage;
import com.sesame.onespace.models.qaMessage.QAMessage;
import com.sesame.onespace.service.MessageService;
import com.sesame.onespace.utils.DateTimeUtil;
import com.sesame.onespace.utils.Log;
import com.sesame.onespace.utils.date.DateConvert;

import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.packet.Message;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chongos on 10/22/15 AD.
 */

// Modified code by Thianchai for QAMessage
    // Last Update 27/12/2016
    // 1. Show notification message
    // 2. Save Q&A message into database

public class ChatMessageListener implements org.jivesoftware.smack.chat.ChatMessageListener {

    private Context mContext;

    public ChatMessageListener(Context context) {
        this.mContext = context;
    }

    @Override
    public void processMessage(Chat chat, final Message message) {
        if (message.getType().equals(Message.Type.chat) || message.getType().equals(Message.Type.normal)) {
            if (message.getBody() != null) {
                final String msgBody = message.getBody();
                final String msgFrom = message.getFrom().split("/")[0];

                new AsyncTask<Void, Void, Object>() {

                    @Override
                    protected Object doInBackground(Void... params) {
                        return new ChatMessage.Builder()
                                .setChatID(msgFrom)
                                .setBody(msgBody)
                                .setFromJID(msgFrom)
                                .setFromMe(false)
                                .setTimestamp(DateTimeUtil.getCurrentTimeStamp())
                                .build();
                    }

                    @Override
                    protected void onPostExecute(Object obj) {
                        super.onPostExecute(obj);
                        if(obj instanceof ChatMessage) {
                            Tools.sendToService(mContext, MessageService.ACTION_XMPP_MESSAGE_RECEIVED, (ChatMessage) obj);
                        }
                    }
                }.execute();

            } else {
                Log.i("XMPP Packet received - but without body (body == null)");
            }
        } else {
            Log.i("XMPP Packet received - but type of packet is not chat : " + message.toString());
        }

    }

    //----------------------------------------------------------------------------------------------

    //Thianchai (I add this)
    public static boolean isAppOnForeground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }
        final String packageName = context.getPackageName();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }
    //**

}
