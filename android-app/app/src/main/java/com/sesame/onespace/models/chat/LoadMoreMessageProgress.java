package com.sesame.onespace.models.chat;

import android.view.View;
import android.view.View.OnClickListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chongos on 10/1/15 AD.
 */
public class LoadMoreMessageProgress implements OnClickListener {

    private enum State {
        LOAD, COMPLETED, ERROR
    }

    private List<Listener> listeners;
    private State state = State.LOAD;

    public LoadMoreMessageProgress() {
        clearListener();
    }

    public LoadMoreMessageProgress(Listener listener) {
        this();
        addListener(listener);
    }

    public void addListener(Listener listener) {
        this.listeners.add(listener);
    }

    public void clearListener() {
        listeners = new ArrayList<>();
    }

    public void load() {
        this.state = State.LOAD;
        for(Listener listener : listeners)
            listener.onLoad();
    }

    public void error(Throwable t) {
        this.state = State.ERROR;
        for(Listener listener : listeners)
            listener.onError(t);
    }

    public void completed(List<Object> result) {
        this.state = State.COMPLETED;
        for(Listener listener : listeners)
            listener.onCompleted(result);
    }

    @Override
    public void onClick(View v) {
        if(this.state == State.ERROR)
            load();
    }

    public interface Listener {
        void onCompleted(List<Object> result);
        void onError(Throwable t);
        void onLoad();
    }

}
