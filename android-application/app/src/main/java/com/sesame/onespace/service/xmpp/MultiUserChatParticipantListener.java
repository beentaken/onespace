package com.sesame.onespace.service.xmpp;

import android.content.Context;
import android.content.Intent;

import com.sesame.onespace.databases.GroupParticipantsDatabase;
import com.sesame.onespace.databases.GroupParticipantsHelper;
import com.sesame.onespace.models.chat.Chat;
import com.sesame.onespace.service.MessageService;
import com.sesame.onespace.utils.Log;

import org.jivesoftware.smack.PresenceListener;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.muc.MultiUserChat;

/**
 * Created by chongos on 11/2/15 AD.
 */
public class MultiUserChatParticipantListener implements PresenceListener {

    private Context context;
    private MultiUserChat muc;

    public MultiUserChatParticipantListener(Context context, MultiUserChat muc) {
        this.context = context;
        this.muc = muc;
    }

    @Override
    public void processPresence(Presence presence) {
        Log.i("Groupchat [" + muc.getRoom() + "] participant changed, count : " + muc.getOccupantsCount());
        String[] occupants = new String[muc.getOccupantsCount()];
        int count = 0;
        for (String user : muc.getOccupants()) {
            Log.i("Groupchat [" + muc.getRoom() + "] -> User : " + user);
            occupants[count] = user;
            count++;
        }

        Intent i = new Intent(MessageService.ACTION_XMPP_PARTICIPANT_CHANGED, null, context, MessageService.class);
        i.putExtra(MessageService.KEY_BUNDLE_CHAT_ID, muc.getRoom());
        i.putExtra(MessageService.KEY_BUNDLE_GROUP_PARTICIPANT, occupants);
        MessageService.sendToServiceHandler(i);
    }

}
