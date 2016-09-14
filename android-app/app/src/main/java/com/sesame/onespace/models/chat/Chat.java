package com.sesame.onespace.models.chat;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.sesame.onespace.utils.Log;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Created by chongos on 9/10/15 AD.
 */
public class Chat implements Parcelable, Comparator<Chat>, Comparable<Chat> {

    public enum Type {
        PRIVATE(1), GROUP(2);
        private int value;

        Type(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    private static final String KEY_CHAT_ID = "chat_id";
    private static final String KEY_SERVER_ID = "server_id";
    private static final String KEY_CHAT_NAME = "chat_name";
    private static final String KEY_TIMESTAMP = "timestamp";
    private static final String KEY_UNREAD_MSG_COUNT = "unread_message_count";
    private static final String KEY_TYPE = "type";
    private static final String KEY_MESSAGES = "message";
    private static final String KEY_MESSAGE_ID = "message_id";

    private String chatID;
    private String serverID;
    private String chatName;
    private String timestamp;
    private int unreadMessageCount;
    private Type type;
    private ChatMessage message;
    private String message_id;

    private Chat(Bundle bundle) {
        this.chatID = bundle.getString(KEY_CHAT_ID, "");
        this.serverID = bundle.getString(KEY_SERVER_ID, "");
        this.chatName = bundle.getString(KEY_CHAT_NAME, "");
        this.timestamp = bundle.getString(KEY_TIMESTAMP, "");
        this.unreadMessageCount = bundle.getInt(KEY_UNREAD_MSG_COUNT, 0);
        this.type = (Type) bundle.getSerializable(KEY_TYPE);
        this.message_id = bundle.getString(KEY_MESSAGE_ID, "");
    }

    public String getId() {
        return chatID;
    }

    public String getServerID() {
        return serverID;
    }

    public String getName() {
        return chatName;
    }

    public String getSnippet() {
        try {
            if (!message.isFromMe()) {
                String from = "";
                if(getType() == Type.GROUP)
                    from = message.getFromJID().split("/")[1] + ": ";
                return from + message.getMessage();
            } else {
                return "You: " + message.getMessage();
            }
        } catch(NullPointerException e) {
            return "";
        }
    }

    public String getTimestamp() {
        return timestamp;
    }

    public int getUnreadMessageCount() {
        return unreadMessageCount;
    }

    public Type getType() {
        return type;
    }

    public String getMessageID() {
        return message_id;
    }

    public void setLastMessage(ChatMessage message) {
        this.message = message;
    }

    @Override
    public int compare(Chat lhs, Chat rhs) {
        return lhs.getId().equals(rhs.getId()) ? 0 : -1;
    }

    @Override
    public int compareTo(Chat another) {
        return getTimestamp().compareTo(another.getTimestamp());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Bundle bundle = new Bundle();
        bundle.putString(KEY_CHAT_ID, chatID);
        bundle.putString(KEY_SERVER_ID, serverID);
        bundle.putString(KEY_CHAT_NAME, chatName);
        bundle.putString(KEY_TIMESTAMP, timestamp);
        bundle.putInt(KEY_UNREAD_MSG_COUNT, unreadMessageCount);
        bundle.putSerializable(KEY_TYPE, type);
        bundle.putString(KEY_MESSAGE_ID, message_id);
        dest.writeBundle(bundle);
        dest.writeParcelable(message, flags);
    }

    public static final Creator<Chat> CREATOR = new Creator<Chat>() {
        @Override
        public Chat createFromParcel(Parcel in) {
            Chat chat = new Chat(in.readBundle());
            chat.setLastMessage(in.<ChatMessage>readParcelable(ChatMessage.class.getClassLoader()));
            return chat;
        }

        @Override
        public Chat[] newArray(int size) {
            return new Chat[size];
        }
    };

    public static class Builder {

        private String chatID;
        private String serverID;
        private String chatName;
        private String timestamp;
        private int unreadMessageCount;
        private Type type;
        private String messageID;

        public Builder() {

        }

        public Builder setChatID(String chatID) {
            this.chatID = chatID;
            return this;
        }

        public Builder setServerID(String serverID) {
            this.serverID = serverID;
            return this;
        }

        public Builder setChatName(String chatName) {
            this.chatName = chatName;
            return this;
        }

        public Builder setTimestamp(String timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder setUnreadMessageCount(int unreadMessageCount) {
            this.unreadMessageCount = unreadMessageCount;
            return this;
        }

        public Builder setType(Type type) {
            this.type = type;
            return this;
        }

        public Builder setMessageID(String id) {
            this.messageID = id;
            return this;
        }

        public Chat build() {
            Bundle bundle = new Bundle();
            bundle.putString(KEY_CHAT_ID, this.chatID);
            bundle.putString(KEY_SERVER_ID, this.serverID);
            bundle.putString(KEY_CHAT_NAME, this.chatName);
            bundle.putString(KEY_TIMESTAMP, this.timestamp);
            bundle.putInt(KEY_UNREAD_MSG_COUNT, this.unreadMessageCount);
            bundle.putSerializable(KEY_TYPE, this.type);
            bundle.putString(KEY_MESSAGE_ID, this.messageID);
            return new Chat(bundle);
        }

    }

}
