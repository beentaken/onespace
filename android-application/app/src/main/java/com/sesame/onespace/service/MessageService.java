package com.sesame.onespace.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;

import com.sesame.onespace.constant.Constant;
import com.sesame.onespace.managers.chat.ChatHistoryManager;
import com.sesame.onespace.managers.SettingsManager;
import com.sesame.onespace.managers.chat.ChatNotificationManager;
import com.sesame.onespace.models.chat.Chat;
import com.sesame.onespace.models.chat.ChatMessage;
import com.sesame.onespace.service.xmpp.XmppManager;
import com.sesame.onespace.service.xmpp.XmppStatus;
import com.sesame.onespace.utils.Log;


/**
 * Created by chongos on 9/7/15 AD.
 */
public class MessageService extends Service {
    public static final int ID = 1;

    public static final String ACTION_CONNECT = "com.sesame.onespace.action.CONNECT";
    public static final String ACTION_DISCONNECT = "com.sesame.onespace.action.DISCONNECT";
    public static final String ACTION_TOGGLE = "com.sesame.onespace.action.TOGGLE";
    public static final String ACTION_COMMAND = "com.sesame.onespace.action.COMMAND";

    public static final String ACTION_BROADCAST_STATUS = "com.sesame.onespace.action.BROADCAST_STATUS";
    public static final String ACTION_NETWORK_CHANGED = "com.sesame.onespace.action.NETWORK_CHANGED";

    public static final String ACTION_XMPP_MESSAGE_SEND = "com.sesame.onespace.action.XMPP.MESSAGE.SEND";
    public static final String ACTION_XMPP_MESSAGE_RECEIVED = "com.sesame.onespace.action.XMPP.CHAT.MESSAGE_RECEIVED";
    public static final String ACTION_XMPP_MESSAGE_SENT = "com.sesame.onespace.action.XMPP.MESSAGE_SENT";
    public static final String ACTION_XMPP_MESSAGE_DELIVERED = "com.sesame.onespace.action.XMPP.MESSAGE_DELIVERED";
    public static final String ACTION_XMPP_PRESENCE_CHANGED = "com.sesame.onespace.action.XMPP.PRESENCE_CHANGED";
    public static final String ACTION_XMPP_PARTICIPANT_REQUEST = "com.sesame.onespace.action.XMPP_PARTICIPANTS_REQUEST";
    public static final String ACTION_XMPP_PARTICIPANT_CHANGED = "com.sesame.onespace.action.XMPP.PARTICIPANTS_CHANGED";
    public static final String ACTION_XMPP_CONNECTION_CHANGED = "com.sesame.onespace.action.XMPP.CONNECTION_CHANGED";
    public static final String ACTION_XMPP_COMMAND_RECEIVED = "com.sesame.onespace.action.XMPP_COMMAND_RECEIVED";
    public static final String ACTION_XMPP_GROUP_JOIN = "com.sesame.onespace.action.XMPP_JOIN_GROUP";
    public static final String ACTION_XMPP_GROUP_LEAVE = "com.sesame.onespace.action.XMPP_LEAVE_GROUP";

    public static final String KEY_BUNDLE_CHAT = "chat";
    public static final String KEY_BUNDLE_CHAT_ID = "chat_id";
    public static final String KEY_BUNDLE_CHAT_MESSAGE = "chat_message";
    public static final String KEY_BUNDLE_GROUP_ROOM_JID = "group_room_jid";
    public static final String KEY_BUNDLE_GROUP_NAME = "group_name";
    public static final String KEY_BUNDLE_GROUP_PARTICIPANT = "group_participant";
    public static final String KEY_BUNDLE_GROUP_JOIN_STATUS = "group_join_status";
    public static final String KEY_BUNDLE_ROSTER = "roster";
    public static final String KEY_BUNDLE_OLD_STATE = "old_state";
    public static final String KEY_BUNDLE_NEW_STATE = "new_state";
    public static final String SERVICE_THREAD_NAME = Constant.APP_NAME + ".Service";

    public static boolean isRunning = false;

    private static boolean sListenersActive = false;

    private static SettingsManager settingsManager;
    private static XmppManager xmppManager;
    private static BroadcastReceiver sXmppConChangedReceiver;
    private static ChatNotificationManager notificationManager;

    private final IBinder mBinder = new LocalBinder();

    private long mHandlerThreadId;

    private static Context sUiContext;
    private static Handler sDelayedDisconnectHandler;

    private static volatile Looper sServiceLooper;
    private static volatile ServiceHandler sServiceHandler;

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            onHandleIntent((Intent) msg.obj, msg.arg1);
        }
    }

    public class LocalBinder extends Binder {
        public MessageService getService() {
            return MessageService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        settingsManager = SettingsManager.getSettingsManager(this);

        // Start a new thread for the service
        HandlerThread thread = new HandlerThread(SERVICE_THREAD_NAME);
        thread.start();
        mHandlerThreadId = thread.getId();
        sServiceLooper = thread.getLooper();
        sServiceHandler = new ServiceHandler(sServiceLooper);
        sDelayedDisconnectHandler = new Handler(sServiceLooper);
        notificationManager = ChatNotificationManager.getNotificationManager(this);

        sUiContext = this;

        Log.i("onCreate(): service thread created - isRunning is set to true");
        isRunning = true;

        // it seems that with gingerbread android doesn't issue null intents any
        // more when restarting a service but only calls the service's onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            int lastStatus = XmppStatus.getInstance(this).getLastKnowState();
            int currentStatus = (xmppManager == null) ? XmppManager.DISCONNECTED : xmppManager.getConnectionStatus();
            if (lastStatus != currentStatus && lastStatus != XmppManager.DISCONNECTING) {
                Log.i("onCreate(): issuing connect intent because we are on gingerbread (or higher). " + "lastStatus is " + lastStatus + " and currentStatus is " + currentStatus);
                Intent i = new Intent(MessageService.this, MessageService.class);
                i.setAction(ACTION_CONNECT);
                startService(i);
            }
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
                Intent i = new Intent();
                i.setAction(ACTION_CONNECT);
                startService(i);
            } else {
                Log.w("onStartCommand() null intent with Gingerbread or higher");
            }
            return START_STICKY;
        }
        Log.i("onStartCommand(): Intent " + intent.getAction());
        if (intent.getAction().equals(ACTION_BROADCAST_STATUS)) {
            int state = getConnectionStatus();
            XmppManager.broadcastStatus(this, state, state);
        } else {
            sendToServiceHandler(startId, intent);
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i("MainService onDestroy(): isRunning is set to false");
        isRunning = false;
        if (xmppManager != null) {

            unregisterReceiver(sXmppConChangedReceiver);
            sXmppConChangedReceiver = null;

            xmppManager.xmppRequestStateChange(XmppManager.DISCONNECTED);
            xmppManager = null;
        }
        teardownListenersForConnection();
        sServiceLooper.quit();
        super.onDestroy();
        Log.i("MainService onDestroy(): service destroyed");
    }

   private void onHandleIntent(final Intent intent, int id) {
       if (xmppManager == null)
           setupXmppManager();

       if (intent.getBooleanExtra("force", false) && intent.getBooleanExtra("disconnect", false))
           xmppManager.xmppRequestStateChange(XmppManager.DISCONNECTED);

       if (Thread.currentThread().getId() != mHandlerThreadId)
           throw new IllegalThreadStateException();

       int initialState = getConnectionStatus();
       updateListenersToCurrentState(initialState);

       String action = intent.getAction();
       Log.i("handling action '" + action + "' while in state " + XmppManager.statusAsString(initialState));

       if (action.equals(ACTION_CONNECT)) {
           processActionConnect(intent);
       } else if (action.equals(ACTION_DISCONNECT)) {
           processActionDisconnect(intent);
       } else if (action.equals(ACTION_TOGGLE)) {
           processActionToggle(intent, initialState);
       } else if (action.equals(ACTION_XMPP_MESSAGE_SEND)) {
           processActionSendMessage(intent);
       } else if (action.equals(ACTION_XMPP_MESSAGE_RECEIVED)) {
           processActionMessageReceived(intent);
       } else if (action.equals(ACTION_XMPP_GROUP_JOIN)) {
           processActionJoinGroup(intent);
       } else if (action.equals(ACTION_XMPP_GROUP_LEAVE)) {
           processActionLeaveGroup(intent);
       } else if (action.equals(ACTION_XMPP_PARTICIPANT_REQUEST)) {
            processActionParticipantRequest(intent);
       } else if (action.equals(ACTION_XMPP_PARTICIPANT_CHANGED)) {
           processActionParticipantChanged(intent);
       } else if (action.equals(ACTION_XMPP_PRESENCE_CHANGED)) {
           processActionPresenceChanged(intent);
       } else if (action.equals(ACTION_NETWORK_CHANGED)) {
           processActionNetworkChanged(intent, initialState);
       } else if (!action.equals(ACTION_XMPP_CONNECTION_CHANGED)) {
           Log.w("Unexpected intent: " + action);
       }

        Log.i("handled action '" + action + "' - state now: " + getConnectionStatus());

        // stop the service if we are disconnected (but stopping the service
        // doesn't mean the process is terminated - onStart can still happen.)
        if (getConnectionStatus() == XmppManager.DISCONNECTED) {
            if (stopSelfResult(id)) {
                Log.i("service is stopping because we are disconnected and no pending intents exist");
            } else {
                Log.i("we are disconnected, but more pending intents to be delivered - service will not stop");
            }
        }
   }

    private void processActionConnect(Intent intent) {
        Log.i("MessageService process: ACTION_CONNECT");
        if (intent.getBooleanExtra("disconnect", false) || !isRunning) {
            xmppManager.xmppRequestStateChange(XmppManager.DISCONNECTED);
        } else {
            xmppManager.xmppRequestStateChange(XmppManager.CONNECTED);
        }
    }

    private void processActionDisconnect(Intent intent) {
        Log.i("MessageService process: ACTION_DISCONNECT");
        xmppManager.xmppRequestStateChange(XmppManager.DISCONNECTED);
    }

    private void processActionToggle(Intent intent, int initialState) {
        Log.i("MessageService process: ACTION_TOGGLE");
        switch (initialState) {
            case XmppManager.CONNECTED:
            case XmppManager.CONNECTING:
            case XmppManager.WAITING_TO_CONNECT:
            case XmppManager.WAITING_FOR_NETWORK:
                xmppManager.xmppRequestStateChange(XmppManager.DISCONNECTED);
                break;
            case XmppManager.DISCONNECTED:
            case XmppManager.DISCONNECTING:
                xmppManager.xmppRequestStateChange(XmppManager.CONNECTED);
                break;
            default:
                throw new IllegalStateException("Unkown initialState while handling" + MessageService.ACTION_TOGGLE);
        }
    }

    private void processActionNetworkChanged(Intent intent, int initialState) {
        Log.i("MessageService process: ACTION_NETWORK_CHANGED");
        boolean available = intent.getBooleanExtra("available", true);
        boolean failover = intent.getBooleanExtra("failover", false);
        Log.i("network_changed with available=" + available + ", failover=" + failover + " and when in state: " + XmppManager.statusAsString(initialState));
        // We are in a waiting state and have a network - try to connect.
        if (available && (initialState == XmppManager.WAITING_TO_CONNECT || initialState == XmppManager.WAITING_FOR_NETWORK)) {
            xmppManager.xmppRequestStateChange(XmppManager.CONNECTED);
        } else if (!available && !failover && initialState == XmppManager.CONNECTED) {
            xmppManager.xmppRequestStateChange(XmppManager.WAITING_FOR_NETWORK);
        }
    }

    private void processActionPresenceChanged(Intent intent) {
        Log.i("MessageService process: ACTION_PRESENCE_CHANGED");
    }

    private void processActionSendMessage(final Intent intent) {
        Log.i("MessageService process: ACTION_XMPP_MESSAGE_SEND");
        final ChatMessage msg = intent.getParcelableExtra(KEY_BUNDLE_CHAT_MESSAGE);
        if (msg != null) {
            xmppManager.send(msg);
        }
    }

    private void processActionMessageReceived(final Intent intent) {
        final ChatMessage msg = intent.getParcelableExtra(KEY_BUNDLE_CHAT_MESSAGE);
        if (msg == null)
            return;

        String msgType = msg.getMessageType();
        if (msgType.equalsIgnoreCase(ChatMessage.MESSAGE_TYPE__CHAT)) {
            processActionChatMessageReceived(intent, msg);
        } else if (msgType.equalsIgnoreCase(ChatMessage.MESSAGE_TYPE__QUERY)) {
            processActionQAMessageReceived(intent, msg);
        }
    }


    private void processActionChatMessageReceived(final Intent intent, final ChatMessage msg) {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                xmppManager.received(msg);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                sendBroadcast(intent);
                notificationManager.displayNotification(msg);
            }
        }.execute();
    }


    private void processActionQAMessageReceived(final Intent intent, final ChatMessage msg) {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                xmppManager.handleQAMessage(msg);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                //sendBroadcast(intent);
                //notificationManager.displayNotification(msg);
            }
        }.execute();
    }


    private void processActionJoinGroup(Intent intent) {
        Log.i("MessageService process: ACTION_XMPP_GROUP_JOIN");
        String room = intent.getStringExtra(KEY_BUNDLE_GROUP_ROOM_JID);
        String name = intent.getStringExtra(KEY_BUNDLE_GROUP_NAME);
        boolean status = xmppManager.joinGroupchat(room, name);

        intent.putExtra(KEY_BUNDLE_CHAT, ChatHistoryManager.getInstance(getApplicationContext())
                .getChat(room + "@" + settingsManager.xmppRecource + "." + settingsManager.xmppServiceName));
        intent.putExtra(KEY_BUNDLE_GROUP_JOIN_STATUS, status);
        sendBroadcast(intent);
    }

    private void processActionLeaveGroup(Intent intent) {
        Log.i("MessageService process: ACTION_XMPP_GROUP_LEAVE");
        Chat chat = intent.getParcelableExtra(KEY_BUNDLE_CHAT);
        xmppManager.leaveGroupchat(chat);
    }

    private void processActionParticipantRequest(Intent intent) {
        Log.i("MessageService process: ACTION_XMPP_PARTICIPANT_REQUEST");
        Chat chat = intent.getParcelableExtra(KEY_BUNDLE_CHAT);
        xmppManager.requestGetGroupParticipants(chat);
    }

    private void processActionParticipantChanged(Intent intent) {
        Log.i("MessageService process: ACTION_XMPP_PARTICIPANT_CHANGED");
        sendBroadcast(intent);
    }

    public static int getConnectionStatus() {
        return xmppManager == null ? XmppManager.DISCONNECTED : xmppManager.getConnectionStatus();
    }

    private void setupXmppManager() {
        sXmppConChangedReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                intent.setClass(MessageService.this, MessageService.class);
                onConnectionStatusChanged(intent.getIntExtra("old_state", 0), intent.getIntExtra("new_state", 0));
                startService(intent);
            }
        };
        IntentFilter intentFilter = new IntentFilter(ACTION_XMPP_CONNECTION_CHANGED);
        registerReceiver(sXmppConChangedReceiver, intentFilter);
        xmppManager = XmppManager.getInstance(this);
    }

    private void onConnectionStatusChanged(int oldStatus, int status) {
        Log.i("xmpp connection status changed : "+XmppManager.statusAsString(status));
    }

    private int updateListenersToCurrentState(int currentState) {
        boolean wantListeners;
        switch (currentState) {
            case XmppManager.CONNECTED:
            case XmppManager.CONNECTING:
            case XmppManager.DISCONNECTING:
            case XmppManager.WAITING_TO_CONNECT:
            case XmppManager.WAITING_FOR_NETWORK:
                wantListeners = true;
                break;
            case XmppManager.DISCONNECTED:
                wantListeners = false;
                break;
            default:
                throw new IllegalStateException("updateListeners found invalid  int: " + currentState);
        }

        if (wantListeners && !sListenersActive) {
            setupListenersForConnection();
            sListenersActive = true;
        } else if (!wantListeners) {
            teardownListenersForConnection();
            sListenersActive = false;
        }

        return currentState;
    }

    private void setupListenersForConnection() {
        Log.i("setupListenersForConnection()");
    }

    private void teardownListenersForConnection() {
        Log.i("teardownListenersForConnection()");
        stopForeground(true);
    }

    public static Looper getServiceLooper() {
        return sServiceLooper;
    }

    public static Handler getDelayedDisconnectHandler() {
        return sDelayedDisconnectHandler;
    }

    @Override
    public void sendBroadcast(Intent intent) {
        super.sendBroadcast(intent);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
            intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public static boolean sendToServiceHandler(int i, Intent intent) {
        if (sServiceHandler != null) {
            Message msg = sServiceHandler.obtainMessage();
            msg.arg1 = i;
            msg.obj = intent;
            sServiceHandler.sendMessage(msg);
            return true;
        } else {
            Log.w("sendToServiceHandler() called with "
                    + intent.getAction()
                    + " when service handler is null");
            return false;
        }
    }

    public static boolean sendToServiceHandler(Intent intent) {
        return sendToServiceHandler(0, intent);
    }

}