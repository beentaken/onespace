package com.sesame.onespace.models.chat;

import android.os.Bundle;
import android.os.Parcel;

/**
 * Created by Thian on 9/12/2559.
 */

public final class QueryMessage
        extends ChatMessage {

    //===========================================================================================================//
    //  ATTRIBUTE                                                                                   ATTRIBUTE
    //===========================================================================================================//

    public static final Creator<ChatMessage> CREATOR = new Creator<ChatMessage>() {
        @Override
        public ChatMessage createFromParcel(Parcel in) {
            return new QueryMessage(in.readBundle());
        }

        @Override
        public ChatMessage[] newArray(int size) {
            return new QueryMessage[size];
        }
    };

    public static final String KEY_QUESTION = "question";
    public static final String KEY_ANSWERS = "answers";
    public static final String KEY_RESPONSE = "response";

    //===========================================================================================================//
    //  CONSTRUCTOR                                                                                 CONSTRUCTOR
    //===========================================================================================================//

    public QueryMessage(Bundle bundle) {
        super(bundle);
    }

    @Override
    public String getMessage() {
        return "sent a query";
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

}
