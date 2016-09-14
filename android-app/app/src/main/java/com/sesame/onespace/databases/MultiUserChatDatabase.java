package com.sesame.onespace.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

/**
 * Created by chongos on 11/1/15 AD.
 */
public class MultiUserChatDatabase extends Database {

    public MultiUserChatDatabase(Context context) {
        super(context);
    }

    public static boolean add(String roomjid, String name) {
        ContentValues values = composeValues(roomjid, name);
        long ret = dbWrite.insert(DatabaseOpenHelper.TABLE_MUC, null, values);
        return ret != -1;
    }

    public static boolean update(String roomjid, String name) {
        ContentValues values = composeValues(roomjid, name);
        int ret = dbWrite.update(DatabaseOpenHelper.TABLE_MUC, values, DatabaseOpenHelper.KEY_MUC_ROOMJID + "='" + roomjid + "'", null);
        return ret == 1;
    }

    public static boolean delete(String roomjid) {
        int ret = dbWrite.delete(DatabaseOpenHelper.TABLE_MUC, DatabaseOpenHelper.KEY_MUC_ROOMJID + "='" + roomjid + "'", null);
        return ret == 1;
    }

    public static String getName(String roomjid) {
        Cursor c = dbRead.query(DatabaseOpenHelper.TABLE_MUC, new String[] { DatabaseOpenHelper.KEY_MUC_NAME },
                DatabaseOpenHelper.KEY_MUC_ROOMJID + "='" + roomjid + "'", null, null , null, null);
        if(c.getCount() == 1) {
            c.moveToFirst();
            String res = c.getString(0);
            c.close();
            return res;
        } else {
            c.close();
            return null;
        }
    }

    public static boolean contains(String roomjid) {
        Cursor c = dbRead.query(DatabaseOpenHelper.TABLE_MUC, new String[] { DatabaseOpenHelper.KEY_MUC_NAME },
                DatabaseOpenHelper.KEY_MUC_ROOMJID + "='" + roomjid + "'", null, null, null, null);
        boolean ret = c.getCount() == 1;
        c.close();
        return ret;
    }

    public static String[][] getFullDatabase() {
        Cursor c = dbRead.query(DatabaseOpenHelper.TABLE_MUC, new String[] { DatabaseOpenHelper.KEY_MUC_ROOMJID,
                DatabaseOpenHelper.KEY_MUC_NAME }, null, null, null , null, null);
        int rowCount = c.getCount();
        c.moveToFirst();
        String[][] res = new String[rowCount][2];
        for (int i = 0; i < rowCount; i++) {
            res[i][0] = c.getString(0);
            res[i][1] = c.getString(1);
            c.moveToNext();
        }
        c.close();
        return res;
    }

    private static ContentValues composeValues(String roomjid, String name) {
        ContentValues values = new ContentValues();
        values.put(DatabaseOpenHelper.KEY_MUC_ROOMJID, roomjid);
        values.put(DatabaseOpenHelper.KEY_MUC_NAME, name);
        return values;
    }
}
