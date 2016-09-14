package com.sesame.onespace.databases;

import android.content.ContentValues;
import android.content.Context;

import com.sesame.onespace.models.chat.Chat;
import com.sesame.onespace.models.chat.ChatMessage;

import java.util.List;

/**
 * Created by chongos on 10/27/15 AD.
 */
public class ChatHelper {

    private static ChatHelper instance = null;
    private ChatDatabase chatDatabase;

    private ChatHelper(Context context) {
        chatDatabase = new ChatDatabase(context);
    }

    public static ChatHelper getInstance(Context context) {
        if (instance == null)
            instance = new ChatHelper(context);
        return instance;
    }


    public boolean containChat(String chatID) {
        return chatDatabase.containChat(chatID);
    }

    public boolean addChat(Chat chat) {
        return chatDatabase.addChat(chat);
    }

    public boolean updateChat(long messageID, ChatMessage chatMessage) {
        return chatDatabase.updateChat(messageID, chatMessage);
    }

    public int setUnreadMessageCount(String chatID, int count) {
        return chatDatabase.setUnreadMessageCount(chatID, count);
    }

    public Chat getChat(String id) {
        return chatDatabase.getChat(id);
    }

    public List<Chat> getChats(int limit, int page) {
        return chatDatabase.getChats(limit, page);
    }

    public List<Chat> getChats(String timestamp, int limit) {
        return chatDatabase.getChats(timestamp, limit);
    }

    public boolean deleteChat(String id) {
        return chatDatabase.deleteChat(id);
    }

}
