package com.sesame.onespace.models.chat;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by chongos on 9/4/15 AD.
 */

// Modified code by Thianchai for QAMessage
    // Last Update 27/12/2016

public abstract class ChatMessage implements Parcelable, Comparable<ChatMessage> {

    public static final String KEY_MESSAGE_TYPE = "message-type";
    public static final String KEY_MEDIA_TYPE = "media";

    private static final String KEY_ID = "id";
    private static final String KEY_CHAT_ID = "chat_id";
    private static final String KEY_FROM_JID = "from_jid";
    private static final String KEY_SERVER_ID = "server_id";
    private static final String KEY_BODY = "content";
    private static final String KEY_TIMESTAMP = "timestamp";
    private static final String KEY_FROM_ME = "from_me";
    private static final String KEY_NEED_PUSH = "need_push";
    private static final String KEY_READ_DEVICE_TIMESTAMP = "read_device_timestamp";

    private long id;
    private String chatID;
    private String fromJID;
    private String serverID;
    private String timestamp;
    private String body;
    private String readDeviceTimestamp;
    private boolean isFromMe;
    private boolean needPush;

    protected ChatMessage(Bundle bundle) {
        id = bundle.getLong(KEY_ID, -1);
        chatID = bundle.getString(KEY_CHAT_ID);
        fromJID = bundle.getString(KEY_FROM_JID);
        serverID = bundle.getString(KEY_SERVER_ID);
        timestamp = bundle.getString(KEY_TIMESTAMP);
        body = bundle.getString(KEY_BODY);
        readDeviceTimestamp = bundle.getString(KEY_READ_DEVICE_TIMESTAMP);
        isFromMe = bundle.getBoolean(KEY_FROM_ME);
        needPush = bundle.getBoolean(KEY_NEED_PUSH, true);
    }

    public long getId() {
        return id;
    }

    public String getChatID() {
        return chatID;
    }

    public String getFromJID() {
        return fromJID;
    }

    public String getServerID() {
        return serverID;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getBody() {
        return body;
    }

    public String getReadDeviceTimestamp() {
        return readDeviceTimestamp;
    }

    public void setReadDeviceTimestamp(String readDeviceTimestamp) {
        this.readDeviceTimestamp = readDeviceTimestamp;
    }

    public boolean isFromMe() {
        return isFromMe;
    }

    public void setNeedPush(boolean need) {
        needPush = need;
    }

    public boolean needPush() {
        return needPush;
    }

    public abstract String getMessage();

    @Override
    public int compareTo(ChatMessage another) {
        return getTimestamp().compareTo(another.getTimestamp()) * -1;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Bundle bundle = new Bundle();
        bundle.putLong(KEY_ID, id);
        bundle.putString(KEY_CHAT_ID, chatID);
        bundle.putString(KEY_FROM_JID, fromJID);
        bundle.putString(KEY_SERVER_ID, serverID);
        bundle.putBoolean(KEY_FROM_ME, isFromMe);
        bundle.putBoolean(KEY_NEED_PUSH, needPush);
        bundle.putString(KEY_TIMESTAMP, timestamp);
        bundle.putString(KEY_BODY, body);
        bundle.putString(KEY_READ_DEVICE_TIMESTAMP, readDeviceTimestamp);
        dest.writeBundle(bundle);
    }


    /**
     * Chat Message Builder
     */
    public static class Builder {

        private long id = -1;
        private String chatID;
        private String fromJID;
        private String serverID;
        private String timestamp;
        private String body;
        private String type;
        private String readDeviceTimestamp;
        private boolean fromMe;
        private boolean needPush = true;

        public Builder() { }

        public Builder setID(long id) {
            this.id = id;
            return this;
        }

        public Builder setChatID(String id) {
            this.chatID = id;
            return this;
        }

        public Builder setFromJID(String from) {
            this.fromJID = from;
            return this;
        }

        public Builder setServerID(String serverID) {
            this.serverID = serverID;
            return this;
        }

        public Builder setFromMe(boolean fromMe) {
            this.fromMe = fromMe;
            return this;
        }

        public Builder setNeedPush(boolean needPush) {
            this.needPush = needPush;
            return this;
        }

        public Builder setTimestamp(String timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder setReadDeviceTimestamp(String timestamp) {
            this.readDeviceTimestamp = timestamp;
            return this;
        }

        public Builder setBody(String body) {
            this.body = body;
            try {
                JSONObject jsonObject = new JSONObject(body);

                //Thianchai (I modified this)
                if (jsonObject.getString("message-type").equals("chat")){

                    this.type = jsonObject.getString(KEY_MEDIA_TYPE);

                }

                if (jsonObject.getString("message-type").equals("query")){

                    this.type = "query";

                }
                //**


            } catch (JSONException e) {
                this.type = Type.TEXT.getString();
            }
            return this;
        }

        public ChatMessage build() {
            Bundle bundle = new Bundle();
            bundle.putLong(KEY_ID, this.id);
            bundle.putString(KEY_CHAT_ID, this.chatID);
            bundle.putString(KEY_FROM_JID, this.fromJID);
            bundle.putString(KEY_SERVER_ID, this.serverID);
            bundle.putBoolean(KEY_FROM_ME, this.fromMe);
            bundle.putBoolean(KEY_NEED_PUSH, this.needPush);
            bundle.putString(KEY_TIMESTAMP, this.timestamp);
            bundle.putString(KEY_BODY, this.body);
            bundle.putString(KEY_READ_DEVICE_TIMESTAMP, this.readDeviceTimestamp);

            if(this.type.equals(Type.IMAGE.getString())) {

                return new ImageMessage(bundle);

            }

            //Thianchai (I add this)
            if(this.type.equals(Type.QUERY.getString())) {

                return new QueryMessage(bundle);

            }
            //

            return new TextMessage(bundle);
        }
    }

    public enum Type {
        TEXT("text"), IMAGE("image"), VIDEO("video"), AUDIO("audio"), QUERY("query"); //Thianchai (I add QUERY("query"))

        private String str;

        Type(String str) {
            this.str = str;
        }

        public String getString() {
            return str;
        }
    }

}