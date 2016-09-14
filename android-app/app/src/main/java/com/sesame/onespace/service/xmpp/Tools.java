package com.sesame.onespace.service.xmpp;

import android.content.Context;
import android.content.Intent;

import com.sesame.onespace.models.chat.Chat;
import com.sesame.onespace.models.chat.ChatMessage;
import com.sesame.onespace.service.MessageService;

/**
 * Created by chongos on 10/22/15 AD.
 */
public class Tools {

    public static void sendToService(Context context, String action, ChatMessage chatMessage) {
        Intent i = new Intent(action, null, context, MessageService.class);
        i.putExtra(MessageService.KEY_BUNDLE_CHAT_MESSAGE, chatMessage);
        MessageService.sendToServiceHandler(i);
    }

    public static void sendToService(Context context, String action, Chat chat) {
        Intent i = new Intent(action, null, context, MessageService.class);
        i.putExtra(MessageService.KEY_BUNDLE_CHAT, chat);
        MessageService.sendToServiceHandler(i);
    }

    public static void sendToService(Context context, String action, Chat chat, ChatMessage chatMessage) {
        Intent i = new Intent(action, null, context, MessageService.class);
        i.putExtra(MessageService.KEY_BUNDLE_CHAT, chat);
        i.putExtra(MessageService.KEY_BUNDLE_CHAT_MESSAGE, chatMessage);
        MessageService.sendToServiceHandler(i);
    }

    public static void startService(Context context, String action) {
        Intent intent = new Intent(action, null, context, MessageService.class);
        context.startService(intent);
    }
}
