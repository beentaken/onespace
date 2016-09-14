package com.sesame.onespace.databases;

import android.content.Context;

/**
 * Created by chongos on 11/2/15 AD.
 */
public class GroupParticipantsHelper {

    private static GroupParticipantsHelper instance = null;

    private GroupParticipantsHelper(Context context) {
        new GroupParticipantsDatabase(context);
    }

    public static GroupParticipantsHelper getInstance(Context context) {
        if (instance == null) {
            instance = new GroupParticipantsHelper(context);
        }
        return instance;
    }

    public boolean add(String room, String name) {
        if (room.contains("'") || name.contains("'"))
            return false;

        addOrUpdate(room, name);
        return true;
    }

    public boolean delete(String gjid) {
        if(!gjid.contains("'") && GroupParticipantsDatabase.contains(gjid)) {
            return GroupParticipantsDatabase.delete(gjid);
        } else {
            return false;
        }
    }

    public boolean deleteParticipant(String gjid, String jid) {
        if(!gjid.contains("'") && !jid.contains("'")
                && GroupParticipantsDatabase.contains(gjid, jid)) {
            return GroupParticipantsDatabase.delete(gjid, jid);
        } else {
            return false;
        }
    }

    public boolean contains(String gjid, String jid) {
        if(!gjid.contains("'") && !jid.contains("'")) {
            return GroupParticipantsDatabase.contains(gjid, jid);
        } else {
            return false;
        }

    }

    public String[] getParticipants(String gjid) {
        if (!gjid.contains("'")) {
            return GroupParticipantsDatabase.getParticipants(gjid);
        } else {
            return null;
        }
    }


    private void addOrUpdate(String gjid, String jid) {
        if(contains(gjid, jid)) {
            GroupParticipantsDatabase.update(gjid, jid);
        } else {
            GroupParticipantsDatabase.add(gjid, jid);
        }
    }

}
