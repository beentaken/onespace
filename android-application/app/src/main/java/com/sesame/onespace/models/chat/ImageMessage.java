package com.sesame.onespace.models.chat;

import android.os.Bundle;
import android.os.Parcel;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by chongos on 9/18/15 AD.
 */
public class ImageMessage extends ChatMessage {

    public static final String KEY_FILE_URL = "image-link";
    public static final String KEY_THUMBNAIL_URL = "thumbnail-link";
    public static final String KEY_CAPTION = "caption";

    private String fileURL = "";
    private String thumbnailURL = "";
    private String caption = "";

    public ImageMessage(Bundle bundle) {
        super(bundle);
        try {
            JSONObject jsonObject = new JSONObject(getBody());
            this.fileURL = jsonObject.getString(KEY_FILE_URL);
            this.thumbnailURL = jsonObject.getString(KEY_THUMBNAIL_URL);
//            this.caption = jsonObject.getString(KEY_CAPTION);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getImageURL() {
        return fileURL;
    }

    public String getThumbnailURL() {
        return thumbnailURL;
    }

    public String getCaption() {
        return caption;
    }

    @Override
    public String getMessage() {
        return "sent an image";
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    public static final Creator<ChatMessage> CREATOR = new Creator<ChatMessage>() {
        @Override
        public ChatMessage createFromParcel(Parcel in) {
            return new ImageMessage(in.readBundle());
        }

        @Override
        public ChatMessage[] newArray(int size) {
            return new ImageMessage[size];
        }
    };

}