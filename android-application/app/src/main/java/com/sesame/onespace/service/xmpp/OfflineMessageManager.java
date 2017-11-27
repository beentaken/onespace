package com.sesame.onespace.service.xmpp;

import android.content.Context;

import com.sesame.onespace.databases.MessagesHelper;
import com.sesame.onespace.managers.chat.ChatHistoryManager;
import com.sesame.onespace.models.chat.ChatMessage;
import com.sesame.onespace.utils.Log;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;

import java.util.List;

/**
 * Created by chongos on 12/3/15 AD.
 */
public class OfflineMessageManager {

    private static OfflineMessageManager instance;
    private XmppManager xmppManager;
    private Context context;

    private OfflineMessageManager(Context context) {
        this.context = context;
    }


    public static OfflineMessageManager getInstance(Context context) {
        if(instance == null)
            instance = new OfflineMessageManager(context);
        return instance;
    }

    public void registerListener(XmppManager xmppManager) {
        this.xmppManager = xmppManager;
        XmppConnectionChangeListener listener = new XmppConnectionChangeListener() {

            @Override
            public void newConnection(AbstractXMPPConnection connection) {
                //
                // This should be handled by the XMPP Server not the app
                //
                //sendOfflineMessages();
            }

        };
        xmppManager.registerConnectionChangeListener(listener);
    }

    public boolean addOfflineMessage(ChatMessage chatMessage) {
        long messageID = chatMessage.getId();
        if(messageID == -1)
            ChatHistoryManager.getInstance(context).addMessage(chatMessage);

        return false;
    }

    private void sendOfflineMessages() {
        if(xmppManager == null)
            return;

        List<ChatMessage> messages = MessagesHelper.getInstance(context).getAllNeedPushMessages();
        for (ChatMessage message : messages) {
            long id = xmppManager.send(message);
            if(id == message.getId()) {
                message.setNeedPush(false);
                MessagesHelper.getInstance(context).updateMessage(message);
            }
        }
    }

}
