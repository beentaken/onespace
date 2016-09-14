package com.sesame.onespace.databases;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by chongos on 10/21/15 AD.
 */
public abstract class Database {

    protected static SQLiteDatabase dbWrite;
    protected static SQLiteDatabase dbRead;

    public Database(Context context) {
        if (dbWrite == null) {
            DatabaseOpenHelper helper = new DatabaseOpenHelper(context);
            dbWrite = helper.getWritableDatabase();
            dbRead = helper.getReadableDatabase();
        }
    }

    public static class Cleaner extends Database {

        private static Cleaner instance;

        private Cleaner(Context context) {
            super(context);
        }

        public static Cleaner getCleaner(Context context) {
            if(instance == null)
                instance = new Cleaner(context);
            return instance;
        }

        public void cleanChatHistory() {
            dbWrite.execSQL("DELETE FROM "+DatabaseOpenHelper.TABLE_MESSAGES);
            dbWrite.execSQL("DELETE FROM "+DatabaseOpenHelper.TABLE_CHAT_LIST);
        }

        public void cleanAll() {
            cleanChatHistory();
            dbWrite.execSQL("DELETE FROM "+DatabaseOpenHelper.TABLE_MUC);
            dbWrite.execSQL("DELETE FROM "+DatabaseOpenHelper.TABLE_KEY_VALUE);
        }

    }

}