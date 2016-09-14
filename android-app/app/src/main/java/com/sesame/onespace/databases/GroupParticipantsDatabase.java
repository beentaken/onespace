package com.sesame.onespace.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

/**
 * Created by chongos on 10/21/15 AD.
 */
public class GroupParticipantsDatabase extends Database {

    public GroupParticipantsDatabase(Context context) {
        super(context);
    }

    public static boolean add(String gjid, String jid) {
        ContentValues numbers = composeValues(gjid, jid);
        long ret = dbWrite.insert(DatabaseOpenHelper.TABLE_GROUP_PARTICIPANTS, null, numbers);
        return ret != -1;
    }

    public static boolean update(String gjid, String jid) {
        ContentValues numbers = composeValues(gjid, jid);
        int ret = dbWrite.update(DatabaseOpenHelper.TABLE_GROUP_PARTICIPANTS, numbers,
                DatabaseOpenHelper.KEY_GROUP_GJID + "='" + gjid + "'", null);
        return ret == 1;
    }

    public static boolean delete(String gjid) {
        int ret = dbWrite.delete(DatabaseOpenHelper.TABLE_GROUP_PARTICIPANTS, DatabaseOpenHelper.KEY_GROUP_GJID + "='" + gjid + "'", null);
        return ret == 1;
    }

    public static boolean delete(String gjid, String jid) {
        int ret = dbWrite.delete(DatabaseOpenHelper.TABLE_GROUP_PARTICIPANTS, DatabaseOpenHelper.KEY_GROUP_GJID + "='" + gjid
                + "' AND " + DatabaseOpenHelper.KEY_GROUP_JID + "='" + jid + "'", null);
        return ret == 1;
    }

    public static String[] getParticipants(String gjid) {
        Cursor c = dbRead.query(DatabaseOpenHelper.TABLE_GROUP_PARTICIPANTS, new String[]{DatabaseOpenHelper.KEY_GROUP_JID}
                , null, null, null, null, null);
        int rowCount = c.getCount();
        c.moveToFirst();
        String[] participants = new String[rowCount];
        for (int i = 0; i < rowCount; i++) {
            participants[i] = c.getString(0);
            c.moveToNext();
        }
        c.close();
        return participants;
    }

    public static boolean contains(String groupJID) {
        Cursor c = dbRead.query(DatabaseOpenHelper.TABLE_GROUP_PARTICIPANTS, new String[]{DatabaseOpenHelper.KEY_GROUP_JID},
                DatabaseOpenHelper.KEY_GROUP_GJID + "='" + groupJID + "'", null, null, null, null);
        boolean ret = c.getCount() == 1;
        c.close();
        return ret;
    }

    public static boolean contains(String groupJID, String jid) {
        Cursor c = dbRead.query(DatabaseOpenHelper.TABLE_GROUP_PARTICIPANTS, new String[]{DatabaseOpenHelper.KEY_GROUP_JID},
                DatabaseOpenHelper.KEY_GROUP_GJID + "='" + groupJID + "' AND " + DatabaseOpenHelper.KEY_GROUP_JID + "='" + jid + "'",
                null, null, null, null);
        boolean ret = c.getCount() == 1;
        c.close();
        return ret;
    }

    private static ContentValues composeValues(String groupJID, String jid) {
        ContentValues values = new ContentValues();
        values.put(DatabaseOpenHelper.KEY_GROUP_GJID, groupJID);
        values.put(DatabaseOpenHelper.KEY_GROUP_JID, jid);
        return values;
    }

}
