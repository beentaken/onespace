package com.sesame.onespace.managers.chat;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.sesame.onespace.R;
import com.sesame.onespace.activities.MainActivity;
import com.sesame.onespace.managers.SettingsManager;
import com.sesame.onespace.models.chat.Chat;
import com.sesame.onespace.models.chat.ChatMessage;
import com.sesame.onespace.service.MessageService;
import com.sesame.onespace.utils.Log;

import java.util.HashMap;

/**
 * Created by chongos on 11/19/15 AD.
 */
public class ChatNotificationManager {

    private static ChatNotificationManager notificationManager;
    private Context context;
    private int currentID;
    private HashMap<String, Integer> mapChatIDAndNotifyID;
    private Chat currentOpenningChat;
    private int notificationCount;
    private SettingsManager settingsManager;
    private NotificationManager mNotificationManager;

    private ChatNotificationManager(Context context) {
        this.context = context;
        this.currentID = 0;
        this.notificationCount = 0;
        this.mapChatIDAndNotifyID = new HashMap<>();
        this.settingsManager = SettingsManager.getSettingsManager(context);
        this.mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public static synchronized ChatNotificationManager getNotificationManager(Context context) {
        if(notificationManager == null)
            notificationManager = new ChatNotificationManager(context);
        return notificationManager;
    }

    public void displayNotification(ChatMessage chatMessage) {
        if(!settingsManager.notification)
            return;

        Log.i("Start notification");
        if(currentOpenningChat != null
                && chatMessage.getChatID().equals(currentOpenningChat.getId())) {
            Log.i("Not display notification because chat[" + currentOpenningChat.getName() + "] is openning.");
            return;
        }

        Chat chat = ChatHistoryManager.getInstance(context).getChat(chatMessage.getChatID());
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_app_notification)
                        .setContentTitle(chat.getName())
                        .setContentText(chatMessage.getMessage())
                        .setAutoCancel(true)
                        .setGroup("ONESPACE_NOTIFICATION_GROUP_KEY__CHAT_MESSAGE");

        Intent resultIntent = new Intent(context, MainActivity.class);
        resultIntent.putExtra(MessageService.KEY_BUNDLE_CHAT, chat);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        mBuilder.setContentIntent(resultPendingIntent);
        Notification notification = mBuilder.build();
        if(settingsManager.notificationSound)
            notification.sound = Uri.parse(settingsManager.notificationRingtone);

        if(settingsManager.notificationVibrate)
            notification.vibrate = new long[]{25, 100, 100, 200};

        if(settingsManager.notificationLED) {
            notification.ledOnMS = 1000;
            notification.ledOffMS = 2000;
            notification.ledARGB = Color.MAGENTA;
            notification.flags |= Notification.FLAG_SHOW_LIGHTS;
        }

        Log.i(">>>>>>>>>>>>>> Push put Chat Notification");
        mNotificationManager.notify(getNotifyID(chatMessage.getChatID()), notification);
    }

    public void setOpenningChat(Chat chat) {
        currentOpenningChat = chat;
        mNotificationManager.cancel(getNotifyID(chat.getId()));
    }

    public void closeChat() {
        currentOpenningChat = null;
    }

    public int getNotificationCount() {
        return notificationCount;
    }

    private int getNotifyID(String chatID) {
        if(mapChatIDAndNotifyID.containsKey(chatID))
            return mapChatIDAndNotifyID.get(chatID);

        mapChatIDAndNotifyID.put(chatID, ++currentID);
        return currentID;
    }

}
