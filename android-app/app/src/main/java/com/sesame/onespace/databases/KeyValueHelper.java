package com.sesame.onespace.databases;

import android.content.Context;

/**
 * Created by chongos on 10/23/15 AD.
 */
public class KeyValueHelper {

    public static final String KEY_LAST_RECIPIENT = "lastRecipient";
    public static final String KEY_SEND_DIR = "sendDir";
    public static final String KEY_SMS_ID = "smsID";
    public static final String KEY_SINTENT = "sIntent";
    public static final String KEY_DINTENT = "dIntent";
    public static final String KEY_XMPP_STATUS = "xmppStatus";

    private static KeyValueHelper instance = null;

    private KeyValueHelper(Context context) {
        new KeyValueDatabase(context);
    }

    public static KeyValueHelper getInstance(Context ctx) {
        if (instance == null) {
            instance = new KeyValueHelper(ctx);
        }
        return instance;
    }

    public boolean addKey(String key, String value) {
        if (key.contains("'") || value.contains("'"))
            return false;

        addOrUpdate(key, value);
        return true;
    }

    public boolean deleteKey(String key) {
        if(!key.contains("'") && KeyValueDatabase.containsKey(key)) {
            return KeyValueDatabase.deleteKey(key);
        } else {
            return false;
        }
    }

    public boolean containsKey(String key) {
        if(!key.contains("'")) {
            return KeyValueDatabase.containsKey(key);
        } else {
            return false;
        }

    }

    public String getValue(String key) {
        if (!key.contains("'")) {
            return KeyValueDatabase.getValue(key);
        } else {
            return null;
        }
    }

    public Integer getIntegerValue(String key) {
        String value = getValue(key);
        Integer res;
        try {
            res = Integer.parseInt(value);
        } catch (Exception e) {
            res = null;
        }
        return res;
    }

    public String[][] getAllKeyValue() {
        String[][] res = KeyValueDatabase.getFullDatabase();
        if (res.length == 0)
            res = null;
        return res;
    }

    private void addOrUpdate(String key, String value) {
        if(KeyValueDatabase.containsKey(key)) {
            KeyValueDatabase.updateKey(key, value);
        } else {
            KeyValueDatabase.addKey(key, value);
        }
    }
}