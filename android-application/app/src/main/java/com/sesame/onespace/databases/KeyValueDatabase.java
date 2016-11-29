package com.sesame.onespace.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

/**
 * Created by chongos on 10/23/15 AD.
 */
public class KeyValueDatabase extends Database {

    public KeyValueDatabase(Context context) {
        super(context);
    }

    public static boolean addKey(String key, String value) {
        ContentValues values = composeValues(key, value);
        long ret = dbWrite.insert(DatabaseOpenHelper.TABLE_KEY_VALUE, null, values);
        return ret != -1;
    }

    public static boolean updateKey(String key, String value) {
        ContentValues values = composeValues(key, value);
        int ret = dbWrite.update(DatabaseOpenHelper.TABLE_KEY_VALUE, values, DatabaseOpenHelper.KEY_KV_ID + "='" + key + "'", null);
        return ret == 1;
    }

    public static boolean deleteKey(String key) {
        int ret = dbWrite.delete(DatabaseOpenHelper.TABLE_KEY_VALUE, DatabaseOpenHelper.KEY_KV_ID + "='" + key + "'", null);
        return ret == 1;
    }

    public static String getValue(String key) {
        Cursor c = dbRead.query(DatabaseOpenHelper.TABLE_KEY_VALUE, new String[] { DatabaseOpenHelper.KEY_KV_VALUE },
                DatabaseOpenHelper.KEY_KV_ID + "='" + key + "'", null, null , null, null);
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

    public static boolean containsKey(String key) {
        Cursor c = dbRead.query(DatabaseOpenHelper.TABLE_KEY_VALUE, new String[]{ DatabaseOpenHelper.KEY_KV_VALUE },
                DatabaseOpenHelper.KEY_KV_ID + "='" + key + "'", null, null, null, null);
        boolean ret = c.getCount() == 1;
        c.close();
        return ret;
    }

    public static String[][] getFullDatabase() {
        Cursor c = dbRead.query(DatabaseOpenHelper.TABLE_KEY_VALUE, new String[] { DatabaseOpenHelper.KEY_KV_ID,
                DatabaseOpenHelper.KEY_KV_VALUE}, null, null, null , null, null);
        int rowCount = c.getCount();
        c.moveToFirst();
        String[][] res = new String[rowCount][2];
        for (int i = 0; i < rowCount; i++) {
            res[i][0] = c.getString(0);  // key field
            res[i][1] = c.getString(1);  // value field
            c.moveToNext();
        }
        c.close();
        return res;
    }

    private static ContentValues composeValues(String key, String value) {
        ContentValues values = new ContentValues();
        values.put(DatabaseOpenHelper.KEY_KV_ID, key);
        values.put(DatabaseOpenHelper.KEY_KV_VALUE, value);
        return values;
    }
}
