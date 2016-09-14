package com.sesame.onespace.models.chat;

import android.os.Bundle;
import android.os.Parcel;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by chongos on 9/17/15 AD.
 */
public class TextMessage extends ChatMessage {

    public static final String KEY_CONTENT = "content";

    public TextMessage(Bundle bundle) {
        super(bundle);
    }

    @Override
    public String getMessage() {
        try {
            return new JSONObject(getBody()).getString(KEY_CONTENT);
        } catch (JSONException e) {
            return getBody();
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    public static final Creator<ChatMessage> CREATOR = new Creator<ChatMessage>() {
        @Override
        public ChatMessage createFromParcel(Parcel in) {
            return new TextMessage(in.readBundle());
        }

        @Override
        public ChatMessage[] newArray(int size) {
            return new TextMessage[size];
        }
    };

}