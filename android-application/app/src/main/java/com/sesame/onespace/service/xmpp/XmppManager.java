package com.sesame.onespace.service.xmpp;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;

import com.sesame.onespace.BuildConfig;
import com.sesame.onespace.activities.LoginActivity;
import com.sesame.onespace.fragments.ChatRoomFragment;
import com.sesame.onespace.managers.SettingsManager;
import com.sesame.onespace.managers.chat.ChatHistoryManager;
import com.sesame.onespace.models.chat.ChatMessage;
import com.sesame.onespace.service.MessageService;
import com.sesame.onespace.service.NetworkConnectivityReceiver;
import com.sesame.onespace.utils.Log;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.bosh.BOSHConfiguration;
import org.jivesoftware.smack.bosh.XMPPBOSHConnection;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.disco.ServiceDiscoveryManager;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.xhtmlim.XHTMLManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chongos on 10/22/15 AD.
 */

// Modified code by Thianchai on 29/10/16

public class XmppManager {

    // my first measuring showed that the disconnect in fact does not hang
    // but takes sometimes a lot of time
    // disconnectED xmpp connection. Took: 1048.576 s
    public static final int DISCON_TIMEOUT = 1000 * 10; // 10s
    // The timeout for XMPP connections that get created with
    // DNS SRV information
    public static final int DNSSRV_TIMEOUT = 1000 * 30; // 30s

    public static final int DISCONNECTED = 1;
    // A "transient" state - will only be CONNECTING *during* a call to start()
    public static final int CONNECTING = 2;
    public static final int CONNECTED = 3;
    // A "transient" state - will only be DISCONNECTING *during* a call to stop()
    public static final int DISCONNECTING = 4;
    // This state means we are waiting for a retry attempt etc.
    // mostly because a connection went down
    public static final int WAITING_TO_CONNECT = 5;
    // We are waiting for a valid data connection
    public static final int WAITING_FOR_NETWORK = 6;

    private static XmppManager sXmppManager = null;
    private static int sReusedConnectionCount = 0;
    private static int sNewConnectionCount = 0;

    private int mStatus = DISCONNECTED;

    private List<XmppConnectionChangeListener> mConnectionChangeListeners;
    private AbstractXMPPConnection mConnection = null;
    private ChatMessageListener mChatMessageListener;
    private XmppMultiUserChat mXmppMultiUserChat;
    private ConnectionListener mConnectionListener = null;
    private ChatHistoryManager mChatHistoryManager;
    private OfflineMessageManager mOfflineMessageManager;

    // Our current retry attempt, plus a runnable and handler to implement retry
    private int mCurrentRetryCount = 0;
    private Runnable mReconnectRunnable = new Runnable() {
        public void run() {
            Log.i("attempting reconnection by issuing intent " + MessageService.ACTION_CONNECT);
            Tools.startService(mContext, MessageService.ACTION_CONNECT);
        }
    };

    private Handler mReconnectHandler;
    private SettingsManager mSettings;
    private XmppStatus mXmppStatus;

    private Context mContext;

    private XmppManager(Context context, XMPPTCPConnection connection) {
        mContext = context;
        mReconnectHandler = new Handler(MessageService.getServiceLooper());

        mConnectionChangeListeners = new ArrayList<>();
        mSettings = SettingsManager.getSettingsManager(context);
        mXmppStatus = XmppStatus.getInstance(context);
        mXmppMultiUserChat = XmppMultiUserChat.getInstance(context);
        mXmppMultiUserChat.registerListener(this);
        mChatHistoryManager = ChatHistoryManager.getInstance(mContext);
        mOfflineMessageManager = OfflineMessageManager.getInstance(context);
        mOfflineMessageManager.registerListener(this);
        sReusedConnectionCount = 0;
        sNewConnectionCount = 0;
        Roster.setDefaultSubscriptionMode(Roster.SubscriptionMode.manual);

        // connection can be null, it is created on demand
        mConnection = connection;
    }

    public static XmppManager getInstance(Context ctx) {
        if (sXmppManager == null) {
            sXmppManager = new XmppManager(ctx, null);
        }
        return sXmppManager;
    }

    public static XmppManager getInstance(Context ctx, XMPPTCPConnection connection) {
        if (sXmppManager == null) {
            sXmppManager = new XmppManager(ctx, connection);
        } else {
            sXmppManager.cleanupConnection();
            sXmppManager.onConnectionEstablished(connection);
        }
        return sXmppManager;
    }

    private void start(int initialState) {
        switch (initialState) {
            case CONNECTED:
                initConnection();
                break;
            case WAITING_TO_CONNECT:
            case WAITING_FOR_NETWORK:
                updateStatus(initialState);
                break;
            default:
                throw new IllegalStateException("xmppManager start() Invalid State: " + initialState);
        }
    }

    private void stop() {
        updateStatus(DISCONNECTING);
        cleanupConnection();
        updateStatus(DISCONNECTED);
    }

    private void cleanupConnection() {
        mReconnectHandler.removeCallbacks(mReconnectRunnable);

        if (mConnection != null) {
            if (mConnection.isConnected()) {
                xmppDisconnect(mConnection);
            }

            if (mConnection != null) {
                if (mConnectionListener != null) {
                    mConnection.removeConnectionListener(mConnectionListener);
                }
            }
        }
        mConnectionListener = null;
    }

    public void xmppRequestStateChange(int newState) {
        int currentState = getConnectionStatus();
        switch (newState) {
            case XmppManager.CONNECTED:
                switch (currentState) {
                    case XmppManager.CONNECTED:
                        break;
                    case XmppManager.CONNECTING:
                    case XmppManager.DISCONNECTED:
                    case XmppManager.WAITING_TO_CONNECT:
                    case XmppManager.WAITING_FOR_NETWORK:
                        cleanupConnection();
                        start(XmppManager.CONNECTED);
                        break;
                    default:
                        throw new IllegalStateException("xmppRequestStateChange() unexpected current state when moving to connected: " + currentState);
                }
                break;
            case XmppManager.DISCONNECTED:
                stop();
                break;
            case XmppManager.WAITING_TO_CONNECT:
                switch (currentState) {
                    case XmppManager.CONNECTED:
                        stop();
                        start(XmppManager.WAITING_TO_CONNECT);
                        break;
                    case XmppManager.DISCONNECTED:
                        start(XmppManager.WAITING_TO_CONNECT);
                        break;
                    case XmppManager.WAITING_TO_CONNECT:
                        break;
                    case XmppManager.WAITING_FOR_NETWORK:
                        cleanupConnection();
                        start(XmppManager.CONNECTED);
                        break;
                    default:
                        throw new IllegalStateException("xmppRequestStateChange() xmppRequestStateChangeunexpected current state when moving to waiting: " + currentState);
                }
                break;
            case XmppManager.WAITING_FOR_NETWORK:
                switch (currentState) {
                    case XmppManager.CONNECTED:
                        stop();
                        start(XmppManager.WAITING_FOR_NETWORK);
                        break;
                    case XmppManager.DISCONNECTED:
                        start(XmppManager.WAITING_FOR_NETWORK);
                        break;
                    case XmppManager.WAITING_TO_CONNECT:
                        cleanupConnection();
                        break;
                    case XmppManager.WAITING_FOR_NETWORK:
                        break;
                    default:
                        throw new IllegalStateException("xmppRequestStateChange() xmppRequestStateChangeunexpected current state when moving to waiting: " + currentState);
                }
                break;
            default:
                throw new IllegalStateException("xmppRequestStateChange() invalid state to switch to: " + statusAsString(newState));
        }
    }

    private void xmppDisconnect(AbstractXMPPConnection connection) {
        class DisconnectRunnable implements Runnable {
            private AbstractXMPPConnection con;

            public DisconnectRunnable(AbstractXMPPConnection con) {
                this.con = con;
            }

            public void run() {
                if (con.isConnected()) {
                    Log.i("disconnectING xmpp connection");
                    float start = System.currentTimeMillis();
                    try {
                        con.disconnect();
                    } catch (Exception e) {
                        // Even if we double check that the connection is still connected
                        // sometimes the connection timeout occurs when the disconnect method
                        // is running, so we just log that here
                        Log.i("xmpp disconnect failed: " + e);
                    }
                    float stop = System.currentTimeMillis();
                    float diff = stop - start;
                    diff = diff / 1000;
                    Log.i("disconnectED xmpp connection. Took: " + diff + " s");
                }
            }
        }

        Thread t = new Thread(new DisconnectRunnable(connection), "xmpp-disconnector");
        // we don't want this thread to hold up process shutdown so mark as daemon.
        t.setDaemon(true);
        t.start();

        try {
            t.join(DISCON_TIMEOUT);
        } catch (InterruptedException e) {}
        // the thread is still alive, this means that the disconnect is still running
        // we don't have the time, so prepare for a new connection
        if (t.isAlive()) {
            Log.i(t.getName() + " was still alive: connection will be set to null");
            mConnection = null;
        }
    }

    public static void broadcastStatus(Context context, int old_state, int new_state) {
        Intent intent = new Intent(MessageService.ACTION_XMPP_CONNECTION_CHANGED);
        intent.putExtra(MessageService.KEY_BUNDLE_OLD_STATE, old_state);
        intent.putExtra(MessageService.KEY_BUNDLE_NEW_STATE, new_state);
        context.sendBroadcast(intent);
    }

    public void broadcastConnectAndAuthStatus(int status, String msg) {
        Intent intent = new Intent();
        intent.setAction(LoginActivity.ACTION_LOGIN_STATUS);
        intent.putExtra(LoginActivity.KEY_LOGIN_STATUS, status);
        intent.putExtra(LoginActivity.KEY_LOGIN_MESSAGE, msg);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    public void broadcastMessageSent(ChatMessage chatMessage) {
        Log.i("broadcast message sent: id=" + chatMessage.getId() + " - " + chatMessage.getBody());
        Intent intent = new Intent();
        intent.setAction(MessageService.ACTION_XMPP_MESSAGE_SENT);
        intent.putExtra(MessageService.KEY_BUNDLE_CHAT_MESSAGE, chatMessage);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    private void updateStatus(int status) {
        if (status != mStatus) {
            int old = mStatus;
            mStatus = status;
            mXmppStatus.setState(status);
            Log.i("broadcasting state transition from " + statusAsString(old) + " to " + statusAsString(status) + " via Intent " + MessageService.ACTION_XMPP_CONNECTION_CHANGED);
            broadcastStatus(mContext, old, status);
        }
    }

    private void maybeStartReconnect() {
        int timeout;
        updateStatus(WAITING_TO_CONNECT);
        cleanupConnection();
        mCurrentRetryCount += 1;
        if (mCurrentRetryCount < 20) {
            // a simple linear-backoff strategy.
            timeout = 5000 * mCurrentRetryCount;
        } else {
            // every 5 min
            timeout = 1000 * 60 * 5;
        }
        if(MessageService.isRunning) {
            Log.i("maybeStartReconnect scheduling retry in " + timeout + "ms. Retry #" + mCurrentRetryCount);
            mReconnectHandler.postDelayed(mReconnectRunnable, timeout);
        }
    }


    private void initConnection() {
        AbstractXMPPConnection connection;

        // assert we are only ever called from one thread
        String currentThreadName = Thread.currentThread().getName();
        if(BuildConfig.DEBUG && !currentThreadName.equals(MessageService.SERVICE_THREAD_NAME))
            throw new AssertionError();

        NetworkInfo active = ((ConnectivityManager)mContext.getSystemService(Service.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (active == null || !active.isAvailable()) {
            Log.e("initConnection: connection request, but no network available");
            updateStatus(WAITING_FOR_NETWORK);
            return;
        }

        // everything is ready for a connection attempt
        updateStatus(CONNECTING);

        // create a new connection if the connection is obsolete or if the
        // old connection is still active
        if (SettingsManager.connectionSettingsObsolete
                || mConnection == null
                || mConnection.isConnected() ) {

            try {
                connection = createNewConnection(mSettings);
            } catch (Exception e) {
                // connection failure
                Log.e("Exception creating new XMPP Connection", e);
                maybeStartReconnect();
                return;
            }
            SettingsManager.connectionSettingsObsolete = false;
            sNewConnectionCount++;
        } else {
            // reuse the old connection settings
            connection = mConnection;
            sReusedConnectionCount++;
        }

        onConnectionEstablished(connection);

        if(connectAndAuth(connection)) {
            updateStatus(CONNECTED);
            informListeners(mConnection);
        }
    }

    private void onConnectionEstablished(AbstractXMPPConnection connection) {
        mConnection = connection;
        mConnectionListener = new ConnectionListener() {
            @Override
            public void connected(XMPPConnection connection) {
                Log.i("ConnectionListener: connected() called");
            }

            @Override
            public void authenticated(XMPPConnection connection, boolean resumed) {
                Log.i("ConnectionListener: authenticated() called");
            }

            @Override
            public void connectionClosed() {
                Log.i("ConnectionListener: connectionClosed() called - connection was shutdown by foreign host or by us");
            }

            @Override
            public void connectionClosedOnError(Exception e) {
                Log.w("xmpp disconnected due to error: ", e);
                if (e.getMessage().startsWith("Attr.value missing")) {
                    Log.w((android.util.Log.getStackTraceString(e)));
                }
                maybeStartReconnect();
            }

            @Override
            public void reconnectingIn(int arg0) {
                throw new IllegalStateException("Reconnection Manager is running");
            }

            @Override
            public void reconnectionFailed(Exception arg0) {
                throw new IllegalStateException("Reconnection Manager is running");
            }

            @Override
            public void reconnectionSuccessful() {
                throw new IllegalStateException("Reconnection Manager is running");
            }
        };
        mConnection.addConnectionListener(mConnectionListener);

        mChatMessageListener = new ChatMessageListener(mContext);
        ChatManager.getInstanceFor(mConnection).addChatListener(new ChatManagerListener() {

            @Override
            public void chatCreated(Chat chat, boolean createdLocally) {
                Log.i("chatCreated() called - " + chat.getParticipant());
                chat.addMessageListener(mChatMessageListener);
            }

        });

        Log.i("connection established with parameters: con=" + mConnection.isConnected() +
                " auth=" + mConnection.isAuthenticated() +
                " comp=" + mConnection.isUsingCompression());

        mCurrentRetryCount = 0;
    }

    private boolean connectAndAuth(AbstractXMPPConnection connection) {
        try {
            connection.connect();
            broadcastConnectAndAuthStatus(LoginActivity.CONNECTED, "");
        } catch (Exception e) {
            Log.w("xmpp connection failed: " + e.getMessage());
            broadcastConnectAndAuthStatus(LoginActivity.CONNECT_FAIL, e.getMessage());
            if (e.getMessage() != null && e.getMessage().startsWith("Connection failed. No response from server")) {
                Log.w("xmpp connection in an unusable state, marking it as obsolete", e);
                mConnection = null;
            }
            if(mSettings.getUserAccountManager().isLoggedIn())
                maybeStartReconnect();
            return false;
        }

        // we reuse the connection and the auth was done with the connect()
        if (connection.isAuthenticated()) {
            return true;
        }

        // Create new account
        try {
            if (mSettings.getUserAccountManager().isCreateNewAccount()) {
                createAccount(connection);
                broadcastConnectAndAuthStatus(LoginActivity.SIGNED_UP, "");
            }
        } catch (Exception e) {
            xmppDisconnect(connection);
            broadcastConnectAndAuthStatus(LoginActivity.SIGNUP_FAIL, e.getMessage());
            Log.e("xmpp create account failed: " + e.getMessage());
            stop();
            return false;
        }

        ServiceDiscoveryManager serviceDiscoMgr = ServiceDiscoveryManager.getInstanceFor(connection);
        XHTMLManager.setServiceEnabled(connection, false);
        serviceDiscoMgr.addFeature("http://jabber.org/protocol/disco#info");
        serviceDiscoMgr.addFeature("http://jabber.org/protocol/muc");

        try {
            connection.login();
            broadcastConnectAndAuthStatus(LoginActivity.AUTHENED, "");
        } catch (Exception e) {
            xmppDisconnect(connection);
            broadcastConnectAndAuthStatus(LoginActivity.AUTHEN_FAIL, e.getMessage());
            Log.e("xmpp login failed: " + e.getMessage());
            stop();
            return false;
        }

        return true;
    }

    private void createAccount(AbstractXMPPConnection connection) throws SmackException.NotConnectedException,
            XMPPException.XMPPErrorException, SmackException.NoResponseException {
        String username = mSettings.getUserAccountManager().getUsername();
        String password = mSettings.getUserAccountManager().getPassword();
        AccountManager accountManager = AccountManager.getInstance(connection);
        if(accountManager.supportsAccountCreation()) {
            Log.i("create account : "+username);
            Map<String, String> mp = new HashMap<>();
            mp.put("username", username);
            mp.put("password", password);
            mp.put("name", username);
            mp.put("email", "");
            accountManager.sensitiveOperationOverInsecureConnection(true);
            accountManager.createAccount(username, password, mp);
        } else {
            Log.e("xmpp create account failed: server not support remote account creation");
        }
    }

    private static AbstractXMPPConnection createNewConnection(SettingsManager settings) throws XMPPException {
        String username = settings.getUserAccountManager().getUsername();
        String password = settings.getUserAccountManager().getPassword();

        XMPPTCPConnectionConfiguration.Builder configBuilder = XMPPTCPConnectionConfiguration.builder();
        configBuilder.setUsernameAndPassword(username, password);
        configBuilder.setResource(settings.xmppRecource);
        configBuilder.setServiceName(settings.xmppServer);
        configBuilder.setHost(settings.xmppServer);
        configBuilder.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
        configBuilder.setCompressionEnabled(false);
        configBuilder.setDebuggerEnabled(true);
        configBuilder.setConnectTimeout(DNSSRV_TIMEOUT);

//        BOSHConfiguration.Builder boshConfigBuilder =   BOSHConfiguration.builder();
//        boshConfigBuilder.setUsernameAndPassword(username, password);
//        boshConfigBuilder.setHost(settings.xmppServer);
//        boshConfigBuilder.setPort(Integer.parseInt(settings.onespacePort));
//        boshConfigBuilder.setFile("/http-bind/");
//        boshConfigBuilder.setUseHttps(false);
//        boshConfigBuilder.setServiceName(settings.xmppServer);

        //new XMPPTCPConnection(configBuilder.build());
        //new XMPPBOSHConnection(boshConfigBuilder.build());

        return new XMPPTCPConnection(configBuilder.build());
    }

    public int getConnectionStatus() {
        return mStatus;
    }


    private boolean sendToPrivate(ChatMessage chatMessage) {
        Log.i("Sending message \"" + chatMessage.getBody() + "\"");

        String toJid = chatMessage.getChatID() + "/" + mSettings.xmppRecource;
        String body = chatMessage.getBody();

        Chat chat = ChatManager.getInstanceFor(mConnection).createChat(toJid, mChatMessageListener);
        try {
            chat.sendMessage(body);
        } catch (SmackException.NotConnectedException e) {
            Log.e("sending message: ", e);
            return false;
        }
        return true;
    }

    private boolean sendToGroup(ChatMessage chatMessage) throws NullPointerException {
        String chatID = chatMessage.getChatID();
        MultiUserChat muc = mXmppMultiUserChat.getRoom(chatID).getValue();
        Log.i("Sending group message \"" + chatMessage.getBody() + "\"");
        Message msg = new Message(chatID);
        msg.setType(Message.Type.groupchat);
        msg.setBody(chatMessage.getBody());
        try {
            muc.sendMessage(msg);
        } catch (Exception e) {
            Log.w("Sending message fail : ", e);
            return false;
        }
        return true;
    }

    public long send(ChatMessage chatMessage) {
        if (isConnected()) {
            boolean isSent;
            try {
                isSent = sendToGroup(chatMessage);
            } catch (NullPointerException e) {
                isSent = sendToPrivate(chatMessage);
            }
            long id = chatMessage.getId();
            chatMessage.setNeedPush(!isSent);
            if (isSent && id == -1) {
                id = mChatHistoryManager.addMessage(chatMessage);
            }
            broadcastMessageSent(chatMessage);
            return id;
        } else {
            Log.d("Adding message: \"" + chatMessage.getBody() + "\" to offline queue, because  we are not connected. Status=" + statusString());
            mOfflineMessageManager.addOfflineMessage(chatMessage);
            return -1;
        }
    }

    //Thianchai (I add this)
    public long sendQAMessage(ChatMessage chatMessage){

        if (isConnected()) {
            boolean isSent;
            isSent = sendToPrivate(chatMessage);
            long id = chatMessage.getId();
            chatMessage.setNeedPush(!isSent);
            broadcastMessageSent(chatMessage);
            return id;
        } else {
            Log.d("Adding message: \"" + chatMessage.getBody() + "\" to offline queue, because  we are not connected. Status=" + statusString());
            mOfflineMessageManager.addOfflineMessage(chatMessage);
            return -1;
        }

    }
    //**

    public void received(ChatMessage msg) {
        mChatHistoryManager.addMessage(msg);
    }

    public boolean joinGroupchat(String room, String name) {
        return mXmppMultiUserChat.joinRoom(room, name);
    }

    public void leaveGroupchat(com.sesame.onespace.models.chat.Chat chat) {
        mXmppMultiUserChat.leaveRoom(chat);
    }

    public void requestGetGroupParticipants(com.sesame.onespace.models.chat.Chat chat) {
        String[] participants = mXmppMultiUserChat.getOccupants(chat);
        if(participants != null) {
            Intent i = new Intent(MessageService.ACTION_XMPP_PARTICIPANT_CHANGED, null, mContext, MessageService.class);
            i.putExtra(MessageService.KEY_BUNDLE_CHAT_ID, chat.getId());
            i.putExtra(MessageService.KEY_BUNDLE_GROUP_PARTICIPANT, participants);
            MessageService.sendToServiceHandler(i);
        }
    }

    public boolean isConnected() {
        boolean res = (mConnection != null
                && mConnection.isConnected()
                && mStatus == CONNECTED);
        return res;
    }

    private void informListeners(AbstractXMPPConnection connection) {
        for (XmppConnectionChangeListener listener : mConnectionChangeListeners) {
            listener.newConnection(connection);
        }
    }

    public void registerConnectionChangeListener(XmppConnectionChangeListener listener) {
        mConnectionChangeListeners.add(listener);
    }

    public static int getNewConnectionCount() {
        return sNewConnectionCount;
    }

    public static int getReusedConnectionCount() {
        return sReusedConnectionCount;
    }

    public static String statusAsString(int state) {
        String res;
        switch(state) {
            case 1:
                res = "Disconnected";
                break;
            case 2:
                res = "Connecting";
                break;
            case 3:
                res = "Connected";
                break;
            case 4:
                res = "Disconnecting";
                break;
            case 5:
                res = "Waiting to connect";
                break;
            case 6:
                res = "Waiting for network";
                break;
            default:
                throw new IllegalStateException();
        }
        return res;
    }

    public String statusString() {
        return statusAsString(mStatus);
    }
}
