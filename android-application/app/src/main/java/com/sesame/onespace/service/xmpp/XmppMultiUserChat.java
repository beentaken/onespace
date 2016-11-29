package com.sesame.onespace.service.xmpp;

import android.content.Context;

import com.sesame.onespace.databases.MultiUserChatHelper;
import com.sesame.onespace.managers.SettingsManager;
import com.sesame.onespace.managers.chat.ChatHistoryManager;
import com.sesame.onespace.models.chat.Chat;
import com.sesame.onespace.utils.Log;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jivesoftware.smackx.muc.RoomInfo;
import org.jivesoftware.smackx.xdata.Form;
import org.jivesoftware.smackx.xdata.FormField;
import org.jivesoftware.smackx.xdata.packet.DataForm;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by chongos on 10/27/15 AD.
 */
public class XmppMultiUserChat {

    private static final int JOIN_TIMEOUT = 5000;
    private static final long REJOIN_ROOMS_SLEEP = 1000;

    private static XmppMultiUserChat instance;

    private Context mContext;
    private SettingsManager mSettings;
    private HashMap<String, Map.Entry<Chat, MultiUserChat>> mRooms;
    private AbstractXMPPConnection mConnection;
    private MultiUserChatManager multiUserChatManager;
    private String mMucServer;
    private MultiUserChatHelper mMultiUserChatHelper;

    private XmppMultiUserChat(Context context) {
        this.mContext = context;
        this.mSettings = SettingsManager.getSettingsManager(context);
        this.mRooms = new HashMap<>();
        this.mMultiUserChatHelper = MultiUserChatHelper.getInstance(context);
    }

    public static synchronized XmppMultiUserChat getInstance(Context context) {
        if(instance == null)
            instance = new XmppMultiUserChat(context);
        return instance;
    }

    public void registerListener(XmppManager xmppMgr) {
        xmppMgr.registerConnectionChangeListener(new XmppConnectionChangeListener() {

            public void newConnection(AbstractXMPPConnection connection) {
                mConnection = connection;
                multiUserChatManager = MultiUserChatManager.getInstanceFor(connection);

                // clear the roomNumbers and room ArrayList as we have a new connection
                mRooms.clear();

                // async rejoin rooms, since there is a delay for every room
                Runnable rejoinRoomsRunnable = new RejoinRoomsRunnable();
                Thread t = new Thread(rejoinRoomsRunnable);
                t.setDaemon(true);
                t.start();

                try {
                    Collection<String> mucComponents = multiUserChatManager.getServiceNames();

                    if (mucComponents.size() > 0) {
                        Iterator<String> i = mucComponents.iterator();
                        mMucServer = i.next();
                    }
                } catch (XMPPException|SmackException.NoResponseException|SmackException.NotConnectedException e) {
                    // This is not fatal, just log a warning
                    Log.w("Could not discover local MUC component: ", e);
                }
            }
        });
    }

    public boolean roomExists(String name) {
        return mRooms.containsKey(name);
    }

    public Map.Entry<Chat, MultiUserChat> getRoom(String roomname) {
        return mRooms.get(roomname);
    }

    private MultiUserChat createRoom(String room, String name) throws Exception {
        MultiUserChat multiUserChat = null;
        String roomJID = getRoomJID(room);
        String username = mSettings.getUserAccountManager().getUsername();

        Log.i("Creating room " + roomJID);

        try {
            multiUserChat = multiUserChatManager.getMultiUserChat(roomJID);
        } catch (Exception e) {
            Log.e("MUC creation failed: ", e);
            throw e;
        }

        try {
            multiUserChat.create(username);
        } catch (Exception e) {
            throw e;
        }

        try {
            multiUserChat.sendConfigurationForm(new Form(DataForm.Type.submit));

            Form form = multiUserChat.getConfigurationForm();
            Form answerForm = form.createAnswerForm();

            for(FormField field : form.getFields() ){
                if(!FormField.Type.hidden.name().equals(field.getType().name()) && field.getVariable() != null) {
                    answerForm.setDefaultAnswer(field.getVariable());
                }
            }
            answerForm.setAnswer("muc#roomconfig_persistentroom", true);
            answerForm.setAnswer("muc#roomconfig_publicroom", true);
            multiUserChat.sendConfigurationForm(answerForm);

        } catch (XMPPException e1) {
            Log.w("Unable to send conference room configuration form.", e1);
            throw e1;
        } catch (SmackException e) {
            e.printStackTrace();
        }
        return multiUserChat;
    }

    public boolean joinRoom(String room, String name) {
        String roomJID = getRoomJID(room);
        if (roomExists(roomJID))
            return true;

        String username = mSettings.getUserAccountManager().getUsername();
        DiscussionHistory history = new DiscussionHistory();
        history.setMaxStanzas(0);
        MultiUserChat muc;

        try {
            muc = createRoom(room, name);
        } catch (Exception e) {
            muc = multiUserChatManager.getMultiUserChat(roomJID);
            e.printStackTrace();
        }

        try {
            muc.join(username, null, history, JOIN_TIMEOUT);
            Log.i("Join Group " + roomJID);
            registerRoom(roomJID, name, muc);
            return true;
        } catch (XMPPException|SmackException e) {
            e.printStackTrace();
        }

        return false;
    }

    public void leaveRoom(Chat chat) {
        String roomJID = chat.getId();
        if(mRooms.containsKey(roomJID)) {
            leaveRoom(mRooms.get(roomJID).getValue());
            ChatHistoryManager.getInstance(mContext).deleteChat(chat);
        }
    }

    private void leaveRoom(MultiUserChat muc) {
        String room = muc.getRoom();
        mMultiUserChatHelper.delete(room);
        if (muc.isJoined())
            try {
                muc.leave();
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
            }

        if (mRooms.size() > 0) {
            mRooms.remove(room);
        }
    }

    private void registerRoom(String roomJID, String name, MultiUserChat muc) {
        if(mRooms.containsKey(roomJID)) {
            Log.i("Room " + name + "[" + roomJID + "] register already");
            return;
        }

        Chat chat = ChatHistoryManager.getInstance(mContext)
                .createGroupChat(roomJID, name);
        MultiUserChatMessageListener chatListener = new  MultiUserChatMessageListener(mContext, chat, muc);
        muc.addMessageListener(chatListener);
        muc.addParticipantListener(new MultiUserChatParticipantListener(mContext, muc));
        mRooms.put(roomJID, new AbstractMap.SimpleEntry<>(chat, muc));
        mMultiUserChatHelper.add(roomJID, name);
        Log.i("Register Room " + name + "[" + roomJID + "]");
    }

    public String[] getOccupants(Chat chat) {
        String roomJID = chat.getId();
        if(mRooms.containsKey(roomJID)) {
            MultiUserChat muc = mRooms.get(roomJID).getValue();
            String[] occupants = new String[muc.getOccupantsCount()];
            int count = 0;
            for (String user : muc.getOccupants()) {
                occupants[count] = user;
                count++;
            }
            return occupants;
        }
        return null;
    }

    private RoomInfo getRoomInfo(String room) {
        RoomInfo info;
        try {
            info = multiUserChatManager.getRoomInfo(room);
        } catch (XMPPException|SmackException.NotConnectedException|SmackException.NoResponseException e) {
            return null;
        }
        return info;
    }

    private String getRoomJID(String name) {
        return name + "@" + mSettings.xmppRecource + "." + mSettings.xmppServer;
    }

    private class RejoinRoomsRunnable implements Runnable {

        @Override
        public void run() {
            rejoinRooms();
        }

        private void rejoinRooms() {
            String[][] mucDB = mMultiUserChatHelper.getAll();
            if (mucDB == null)
                return;

            String username = mSettings.getUserAccountManager().getUsername();
            DiscussionHistory history = new DiscussionHistory();
            history.setMaxStanzas(0);
            for (int i = 0; i < mucDB.length; i++) {
                if (!mConnection.isAuthenticated())
                    return;

                RoomInfo info = getRoomInfo(mucDB[i][0]);
                // if info is not null, the room exists on the server
                // so lets check if we can reuse it
                if (info != null) {
                    MultiUserChat muc = multiUserChatManager.getMultiUserChat(mucDB[i][0]);
                    String name = mucDB[i][1];
                    try {
                        Log.i("Rejoin group "+name);
                        muc.join(username, null, history, JOIN_TIMEOUT);

                        // Openfire needs some time to collect the owners list
                        try {
                            Thread.sleep(REJOIN_ROOMS_SLEEP);
                        } catch (InterruptedException e1) {
                            /* Ignore */
                        }
                    } catch (XMPPException|SmackException.NotConnectedException|SmackException.NoResponseException e) {
                        Log.i("rejoinRooms: leaving " + muc.getRoom() + " because of XMMPException", e);

                        if (mConnection.isAuthenticated()) {
                            leaveRoom(muc);
                            continue;
                        } else {
                            break;
                        }
                    }

                    registerRoom(mucDB[i][0], name, muc);
                }
            }
        }
    }


}
