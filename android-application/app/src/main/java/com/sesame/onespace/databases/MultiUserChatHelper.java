package com.sesame.onespace.databases;

import android.content.Context;

/**
 * Created by chongos on 11/1/15 AD.
 */
public class MultiUserChatHelper {

    private static MultiUserChatHelper instance = null;

    private MultiUserChatHelper(Context context) {
        new MultiUserChatDatabase(context);
    }

    public static MultiUserChatHelper getInstance(Context context) {
        if (instance == null) {
            instance = new MultiUserChatHelper(context);
        }
        return instance;
    }

    public boolean add(String room, String name) {
        if (room.contains("'") || name.contains("'"))
            return false;

        addOrUpdate(room, name);
        return true;
    }

    public boolean delete(String room) {
        if(!room.contains("'") && MultiUserChatDatabase.contains(room)) {
            return MultiUserChatDatabase.delete(room);
        } else {
            return false;
        }
    }

    public boolean contains(String room) {
        if(!room.contains("'")) {
            return MultiUserChatDatabase.contains(room);
        } else {
            return false;
        }

    }

    public String getName(String room) {
        if (!room.contains("'")) {
            return MultiUserChatDatabase.getName(room);
        } else {
            return null;
        }
    }

    public String[][] getAll() {
        String[][] res = MultiUserChatDatabase.getFullDatabase();
        if (res.length == 0)
            res = null;
        return res;
    }

    private void addOrUpdate(String room, String name) {
        if(MultiUserChatDatabase.contains(room)) {
            MultiUserChatDatabase.update(room, name);
        } else {
            MultiUserChatDatabase.add(room, name);
        }
    }

}
