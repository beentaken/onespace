package com.sesame.onespace.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.sesame.onespace.models.chat.Chat;
import com.sesame.onespace.models.chat.ChatMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chongos on 10/21/15 AD.
 */
public class ChatDatabase extends Database {

    public ChatDatabase(Context context) {
        super(context);
    }

    public boolean addChat(Chat chat) {
        ContentValues values = new ContentValues();
        values.put(DatabaseOpenHelper.KEY_CHAT_JID, chat.getId());
        values.put(DatabaseOpenHelper.KEY_CHAT_NAME, chat.getName());
        values.put(DatabaseOpenHelper.KEY_CHAT_CREATE_TIMESTAMP, chat.getTimestamp());
        values.put(DatabaseOpenHelper.KEY_CHAT_SORT_TIMESTAMP, chat.getTimestamp());
        values.put(DatabaseOpenHelper.KEY_CHAT_UNREAD_COUNT, chat.getUnreadMessageCount());
        values.put(DatabaseOpenHelper.KEY_CHAT_TYPE, chat.getType().getValue());

        long ret = dbWrite.insert(DatabaseOpenHelper.TABLE_CHAT_LIST, null, values);
        return ret != -1;
    }

    public boolean updateChat(Chat chat) {
        ContentValues values = new ContentValues();
        values.put(DatabaseOpenHelper.KEY_CHAT_NAME, chat.getName());
        values.put(DatabaseOpenHelper.KEY_CHAT_SORT_TIMESTAMP, chat.getTimestamp());

        int ret = dbWrite.update(DatabaseOpenHelper.TABLE_CHAT_LIST, values,
                DatabaseOpenHelper.KEY_CHAT_JID + " = ?",
                new String[]{chat.getId()});
        return ret == 1;
    }

    public boolean updateChat(long messageID, ChatMessage chatMessage) {
        ContentValues values = new ContentValues();

        values.put(DatabaseOpenHelper.KEY_CHAT_MESSAGE_TABLE_ID, messageID);
        values.put(DatabaseOpenHelper.KEY_CHAT_SORT_TIMESTAMP, chatMessage.getTimestamp());

        int ret = dbWrite.update(DatabaseOpenHelper.TABLE_CHAT_LIST, values,
                DatabaseOpenHelper.KEY_CHAT_JID + " = ?",
                new String[]{chatMessage.getChatID()});

        if(!chatMessage.isFromMe())
            dbWrite.execSQL("UPDATE " + DatabaseOpenHelper.TABLE_CHAT_LIST +
                    " SET " + DatabaseOpenHelper.KEY_CHAT_UNREAD_COUNT +
                    " = " + DatabaseOpenHelper.KEY_CHAT_UNREAD_COUNT + " + 1" +
                    " WHERE " + DatabaseOpenHelper.KEY_CHAT_JID + " = ?"
                    , new String[]{chatMessage.getChatID()});

        return ret == 1;
    }

    public int setUnreadMessageCount(String chatID, int count) {
        ContentValues values = new ContentValues();
        values.put(DatabaseOpenHelper.KEY_CHAT_UNREAD_COUNT, count);
        return dbWrite.update(DatabaseOpenHelper.TABLE_CHAT_LIST,
                values, DatabaseOpenHelper.KEY_CHAT_JID + " = ?"
                , new String[]{String.valueOf(chatID)});
    }

    public boolean containChat(String chatID) {
        Cursor c = dbRead.query(DatabaseOpenHelper.TABLE_CHAT_LIST,
                new String[] { DatabaseOpenHelper.KEY_CHAT_JID },
                DatabaseOpenHelper.KEY_CHAT_JID + "=?",
                new String[] { chatID },
                null, null, null, null);

        boolean ret = c.getCount() == 1;
        c.close();
        return ret;
    }

    public Chat getChat(String id) {
        List<Chat> chats = querySelectChats("SELECT * FROM " + DatabaseOpenHelper.TABLE_CHAT_LIST
                + " WHERE " + DatabaseOpenHelper.KEY_CHAT_JID
                + " = ?", new String[]{id});
        return chats.isEmpty() ? null : chats.get(0);
    }

    public List<Chat> getChats() {
        return querySelectChats("SELECT * FROM "
                + DatabaseOpenHelper.TABLE_CHAT_LIST + " ORDER BY "
                + DatabaseOpenHelper.KEY_CHAT_SORT_TIMESTAMP + " DESC"
                , null);
    }

    public List<Chat> getChats(int limit, int page) {
        return querySelectChats("SELECT * FROM " + DatabaseOpenHelper.TABLE_CHAT_LIST
                + " ORDER BY " + DatabaseOpenHelper.KEY_CHAT_SORT_TIMESTAMP + " DESC"
                + " LIMIT " + limit + " OFFSET "
                + (limit * page), null);
    }

    public List<Chat> getChats(String timestamp, int limit) {
        return querySelectChats("SELECT * FROM " + DatabaseOpenHelper.TABLE_CHAT_LIST
                + " WHERE " + DatabaseOpenHelper.KEY_CHAT_SORT_TIMESTAMP + " < ? "
                + " ORDER BY " + DatabaseOpenHelper.KEY_CHAT_SORT_TIMESTAMP + " DESC"
                + " LIMIT " + limit, new String[]{timestamp});
    }

    private List<Chat> querySelectChats(String sql, String[] selectionArgs){
        List<Chat> chats = new ArrayList<>();
        Cursor c = dbRead.rawQuery(sql, selectionArgs);
        if (c.moveToFirst()) {
            do {
                Chat chat = createChatObject(c);
                chats.add(chat);
            } while (c.moveToNext());
        }

        return chats;
    }

    public boolean deleteChat(String id) {
        int ret = dbWrite.delete(DatabaseOpenHelper.TABLE_CHAT_LIST,
                DatabaseOpenHelper.KEY_CHAT_JID + " = ?", new String[]{id});
        return ret == 1;
    }

    private Chat createChatObject(Cursor c) {
        return new Chat.Builder()
                .setChatID(c.getString(c.getColumnIndex(DatabaseOpenHelper.KEY_CHAT_JID)))
                .setChatName(c.getString(c.getColumnIndex(DatabaseOpenHelper.KEY_CHAT_NAME)))
                .setMessageID(c.getString(c.getColumnIndex(DatabaseOpenHelper.KEY_CHAT_MESSAGE_TABLE_ID)))
                .setUnreadMessageCount(c.getInt(c.getColumnIndex(DatabaseOpenHelper.KEY_CHAT_UNREAD_COUNT)))
                .setTimestamp(c.getString(c.getColumnIndex(DatabaseOpenHelper.KEY_CHAT_SORT_TIMESTAMP)))
                .setType(c.getInt(c.getColumnIndex(DatabaseOpenHelper.KEY_CHAT_TYPE)) == Chat.Type.PRIVATE.getValue()
                        ? Chat.Type.PRIVATE : Chat.Type.GROUP)
                .build();
    }

}
