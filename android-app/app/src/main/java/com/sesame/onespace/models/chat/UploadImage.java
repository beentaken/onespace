package com.sesame.onespace.models.chat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chongos on 9/27/15 AD.
 */
public class UploadImage {

    private List<OnUploadImageInteractionListener> listeners;

    private String filename;
    private String mimetype;
    private int progress;
    private boolean isError;
    private boolean isCanceled;
    private boolean isCompleted;

    public UploadImage(String filename, String mimetype) {
        this.filename = filename;
        this.mimetype = mimetype;
        this.progress = 0;

        this.listeners = new ArrayList<>();
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getMimetype() {
        return mimetype;
    }

    public void setMimetype(String mimetype) {
        this.mimetype = mimetype;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
        for(OnUploadImageInteractionListener listener : listeners)
            listener.onProgressUpdate(progress);
    }

    public boolean isError() {
        return isError;
    }

    public void error(Throwable e) {
        this.isError = true;
        for(OnUploadImageInteractionListener listener : listeners)
            listener.onError(e);
    }

    public boolean isCanceled() {
        return isCanceled;
    }

    public void cancel() {
        this.isCanceled = true;
        for(OnUploadImageInteractionListener listener : listeners)
            listener.onCanceled();
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void completed(String result) {
        this.isCompleted = true;
        for(OnUploadImageInteractionListener listener : listeners)
            listener.onCompleted(result);
    }

    public void rotateCompleted(String result) {
        this.filename = result;
        for(OnUploadImageInteractionListener listener: listeners) {
            listener.onRotateCompleted(result);
        }
    }

    public void addListener(OnUploadImageInteractionListener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(OnUploadImageInteractionListener listener) {
        this.listeners.remove(listener);
    }


    public interface OnUploadImageInteractionListener {
        void onRotateCompleted(String path);
        void onProgressUpdate(int percentage);
        void onCanceled();
        void onError(Throwable e);
        void onCompleted(String result);
    }
}
