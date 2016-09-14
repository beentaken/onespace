package com.sesame.onespace.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.sesame.onespace.models.chat.ChatMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chongos on 10/21/15 AD.
 */
public class MessagesDatabase extends Database {

    public MessagesDatabase(Context context) {
        super(context);
    }

    public long addMessage(ChatMessage msg) {
        ContentValues values = new ContentValues();
        values.put(DatabaseOpenHelper.KEY_MESSAGE_CHAT_ID, msg.getChatID());
        values.put(DatabaseOpenHelper.KEY_MESSAGE_DATA, msg.getBody());
        values.put(DatabaseOpenHelper.KEY_MESSAGE_FROM_ME, msg.isFromMe() ? 1 : 0);
        values.put(DatabaseOpenHelper.KEY_MESSAGE_TIMESTAMP, msg.getTimestamp());
        values.put(DatabaseOpenHelper.KEY_MESSAGE_NEEDS_PUSH, msg.needPush() ? 1 : 0);
        if(msg.getFromJID() != null)
            values.put(DatabaseOpenHelper.KEY_MESSAGE_FROM_JID, msg.getFromJID());

        return dbWrite.insert(DatabaseOpenHelper.TABLE_MESSAGES, null, values);
    }

    public boolean updateMessage(ChatMessage msg) {
        if(msg.getId() < 0)
            return false;

        ContentValues values = new ContentValues();
        values.put(DatabaseOpenHelper.KEY_MESSAGE_NEEDS_PUSH, msg.needPush() ? 1 : 0);

        int ret = dbWrite.update(DatabaseOpenHelper.TABLE_MESSAGES, values,
                DatabaseOpenHelper.KEY_MESSAGE_ID + " = ?",
                new String[]{String.valueOf(msg.getId())});
        return ret == 1;
    }

    public ChatMessage getMessage(String id) {
        List<ChatMessage> message = querySelectChatMessage("SELECT * FROM " + DatabaseOpenHelper.TABLE_MESSAGES
                + " WHERE " + DatabaseOpenHelper.KEY_MESSAGE_ID + " = ?"
                , new String[]{id});
        return message.size() > 0 ? message.get(0) : null;
    }

    public List<ChatMessage> getMessages(String chatId, String lastSentDate) {
        return querySelectChatMessage("SELECT * FROM " + DatabaseOpenHelper.TABLE_MESSAGES
                + " WHERE " + DatabaseOpenHelper.KEY_MESSAGE_CHAT_ID + " = ?"
                + " AND " + DatabaseOpenHelper.KEY_MESSAGE_TIMESTAMP + " < ? "
                + " ORDER BY " + DatabaseOpenHelper.KEY_MESSAGE_TIMESTAMP + " DESC"
                , new String[]{chatId, lastSentDate});
    }

    public List<ChatMessage> getMessages(String chatId, String lastSentDate, int limit) {
        return querySelectChatMessage("SELECT * FROM " + DatabaseOpenHelper.TABLE_MESSAGES
                + " WHERE " + DatabaseOpenHelper.KEY_MESSAGE_CHAT_ID + " = ?"
                + " AND " + DatabaseOpenHelper.KEY_MESSAGE_TIMESTAMP + " < ? "
                + " ORDER BY " + DatabaseOpenHelper.KEY_MESSAGE_TIMESTAMP + " DESC"
                + " LIMIT " + limit, new String[]{chatId, lastSentDate});
    }

    public List<ChatMessage> getAllNeedPushMessages() {
        return querySelectChatMessage("SELECT * FROM " + DatabaseOpenHelper.TABLE_MESSAGES
                + " WHERE " + DatabaseOpenHelper.KEY_MESSAGE_NEEDS_PUSH + " = ?"
                + " ORDER BY " + DatabaseOpenHelper.KEY_MESSAGE_TIMESTAMP + " DESC",
                new String[]{"1"});
    }

    public boolean deleteMessages(String chatID) {
        int ret = dbWrite.delete(DatabaseOpenHelper.TABLE_MESSAGES,
                DatabaseOpenHelper.KEY_MESSAGE_CHAT_ID + " = ?", new String[]{chatID});
        return ret == 1;
    }

    public boolean deleteMessages(String chatID, String lastSentDate) {
        int ret = dbWrite.delete(DatabaseOpenHelper.TABLE_MESSAGES,
                DatabaseOpenHelper.KEY_MESSAGE_CHAT_ID + " = ? AND "
                        + DatabaseOpenHelper.KEY_MESSAGE_TIMESTAMP + " < ? ", new String[]{chatID, lastSentDate});
        return ret == 1;
    }

    private List<ChatMessage> querySelectChatMessage(String sql, String[] selectionArgs) {
        List<ChatMessage> messages = new ArrayList<>();

        Cursor c = dbRead.rawQuery(sql, selectionArgs);

        if (c.moveToFirst()) {
            do {
                ChatMessage message = createChatMessageObject(c);
                messages.add(message);
            } while (c.moveToNext());
        }

        c.close();

        return messages;
    }

    private ChatMessage createChatMessageObject(Cursor c) {
        return new ChatMessage.Builder()
                .setID(c.getLong(c.getColumnIndex(DatabaseOpenHelper.KEY_MESSAGE_ID)))
                .setChatID(c.getString(c.getColumnIndex(DatabaseOpenHelper.KEY_MESSAGE_CHAT_ID)))
                .setFromJID(c.getString(c.getColumnIndex(DatabaseOpenHelper.KEY_MESSAGE_FROM_JID)))
                .setBody(c.getString(c.getColumnIndex(DatabaseOpenHelper.KEY_MESSAGE_DATA)))
                .setFromMe(c.getInt(c.getColumnIndex(DatabaseOpenHelper.KEY_MESSAGE_FROM_ME)) == 1)
                .setTimestamp(c.getString(c.getColumnIndex(DatabaseOpenHelper.KEY_MESSAGE_TIMESTAMP)))
                .setNeedPush(c.getInt(c.getColumnIndex(DatabaseOpenHelper.KEY_MESSAGE_NEEDS_PUSH)) == 1)
                .build();
    }
}
