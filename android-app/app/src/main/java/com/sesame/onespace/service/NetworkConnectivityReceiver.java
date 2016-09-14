package com.sesame.onespace.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.sesame.onespace.constant.Constant;
import com.sesame.onespace.utils.Log;

/**
 * Created by chongos on 10/23/15 AD.
 */
public class NetworkConnectivityReceiver extends BroadcastReceiver {

    public static boolean lastKnownIsConnected = false;

    @SuppressWarnings("deprecation")
    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            Log.e("NetworkConnectivityReceiver: Connectivity Manager is null!");
            return;
        }

//        if (Constant.DEBUG) {
//            for (NetworkInfo network : cm.getAllNetworkInfo()) {
//                Log.i("NetworkConnectivityReceiver: "
//                        + " available=" + (network.isAvailable()?1:0)
//                        + ", connected=" + (network.isConnected()?1:0)
//                        + ", connectedOrConnecting=" + (network.isConnectedOrConnecting()?1:0)
//                        + ", failover=" + (network.isFailover()?1:0)
//                        + ", roaming=" + (network.isRoaming()?1:0)
//                        + ", networkName=" + network.getTypeName());
//            }
//        }

        NetworkInfo network = cm.getActiveNetworkInfo();
        lastKnownIsConnected = network != null && network.isConnected();

        if (network != null && MessageService.isRunning) {
            Log.i("NetworkConnectivityReceiver: " + MessageService.ACTION_NETWORK_CHANGED + " " + network.getTypeName());
            Intent svcintent = new Intent(context, MessageService.class);
            svcintent.setAction(MessageService.ACTION_NETWORK_CHANGED);
            svcintent.putExtra("available", network.isConnected());
            svcintent.putExtra("failover", network.isFailover());
            context.startService(svcintent);
        }

        network = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
        if (network.getTypeName().equals("WIFI") && network.isConnected()) {
            Log.i("NetworkConnectivityReceiver: startOnWifiConnected enabled, wifi connected, sending intent");
            Intent i = new Intent(context, MessageService.class);
            i.setAction(MessageService.ACTION_CONNECT);
            context.startService(new Intent(intent));
        }

//        else if (network.getTypeName().equals("WIFI") && !network.isConnected()) {
//            Log.i("NetworkConnectivityReceiver: stopOnWifiDisconnected enabled, wifi disconnected, sending intent");
//            Intent i = new Intent(context, MessageService.class);
//            i.setAction(MessageService.ACTION_DISCONNECT);
//            context.startService(i);
//        }
    }

}