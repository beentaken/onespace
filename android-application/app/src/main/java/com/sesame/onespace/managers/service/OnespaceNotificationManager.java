package com.sesame.onespace.managers.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;

import com.sesame.onespace.utils.Log;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by christian on 7/2/17.
 */

public class OnespaceNotificationManager {


    private static OnespaceNotificationManager sNotificationManager = null;
    private Context context = null;
    private HashMap<Integer, Notification> notificationHashMap = null;
    private NotificationManager notificationManager = null;

    public OnespaceNotificationManager(Context context) {
        this.context = context;
        this.notificationHashMap = new HashMap<Integer, Notification>();
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public static OnespaceNotificationManager getSettingsManager(Context context) {
        if (sNotificationManager == null) {
            sNotificationManager = new OnespaceNotificationManager(context);
        }
        return sNotificationManager;
    }


    public void addNotification(Integer id, Notification notification) {
        this.notificationHashMap.put(id, notification);
    }

    public void cancelNotifications() {
        this.notificationManager.cancelAll();
    }

    public void cancelNotifications(String groupKey) {
        for(Iterator<Map.Entry<Integer, Notification>> it = notificationHashMap.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Integer, Notification> entry = it.next();
            Notification notification = entry.getValue();

            if (notification.getGroup().equalsIgnoreCase(groupKey)) {
                this.notificationManager.cancel(entry.getKey());
                it.remove();
            }

        }
    }

}
