package com.sesame.onespace.databases;

import android.content.Context;

import com.sesame.onespace.models.chat.ChatMessage;

import java.util.List;

/**
 * Created by chongos on 10/27/15 AD.
 */
public class MessagesHelper {

    private static MessagesHelper instance = null;
    private MessagesDatabase messagesDatabase;

    private MessagesHelper(Context context) {
        messagesDatabase = new MessagesDatabase(context);
    }

    public static MessagesHelper getInstance(Context context) {
        if (instance == null)
            instance = new MessagesHelper(context);
        return instance;
    }

    public long addMessage(ChatMessage msg) {
        return messagesDatabase.addMessage(msg);
    }

    public boolean updateMessage(ChatMessage msg) {
        return messagesDatabase.updateMessage(msg);
    }

    public ChatMessage getMessage(String id) {
        return messagesDatabase.getMessage(id);
    }

    public List<ChatMessage> getMessages(String chatId, String lastSentDate) {
        return messagesDatabase.getMessages(chatId, lastSentDate);
    }

    public List<ChatMessage> getMessages(String chatId, String lastSentDate, int limit) {
        return messagesDatabase.getMessages(chatId, lastSentDate, limit);
    }

    public List<ChatMessage> getAllNeedPushMessages() {
        return messagesDatabase.getAllNeedPushMessages();
    }

    public boolean deleteMessages(String chatID) {
        return messagesDatabase.deleteMessages(chatID);
    }

    public boolean deleteMessages(String chatID, String lastSentDate) {
        return messagesDatabase.deleteMessages(chatID, lastSentDate);
    }
}
