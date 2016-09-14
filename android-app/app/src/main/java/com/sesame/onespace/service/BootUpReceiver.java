package com.sesame.onespace.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.sesame.onespace.managers.UserAccountManager;

/**
 * Created by chongos on 9/20/15 AD.
 */
public class BootUpReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

//        if(MessageService.getState().equals(SmackConnection.ConnectionState.DISCONNECTED)
//                && UserAccountManager.getInstance(context).isLoggedIn()) {
//            Intent i = new Intent(context, MessageService.class);
//            context.startService(i);
//        }

    }

}