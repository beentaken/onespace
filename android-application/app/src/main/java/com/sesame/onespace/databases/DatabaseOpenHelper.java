package com.sesame.onespace.databases;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by chongos on 10/21/15 AD.
 */
public class DatabaseOpenHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "onespace";

    // chat_list table
    public static final String TABLE_CHAT_LIST = "chat_list";
    public static final String KEY_CHAT_ID = "id";
    public static final String KEY_CHAT_JID = "jid";
    public static final String KEY_CHAT_MESSAGE_TABLE_ID = "message_table_id";
    public static final String KEY_CHAT_NAME = "subject";
    public static final String KEY_CHAT_TYPE = "type";
    public static final String KEY_CHAT_CREATE_TIMESTAMP = "create_timestamp";
    public static final String KEY_CHAT_SORT_TIMESTAMP = "sort_timestamp";
    public static final String KEY_CHAT_UNREAD_COUNT = "unread_count";
    public static final String KEY_CHAT_LAST_READ_MESSAGE_TABLE_ID = "last_read_message_table_id";
    public static final String KEY_CHAT_LAST_READ_RECEIPT_SENT_MESSAGE_TABLE_ID = "last_read_receipt_sent_message_table_id";
    private static final String CREATE_TABLE_CHAT = "CREATE TABLE " + TABLE_CHAT_LIST + "("
            + KEY_CHAT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_CHAT_JID + " TEXT UNIQUE,"
            + KEY_CHAT_MESSAGE_TABLE_ID + " INTEGER,"
            + KEY_CHAT_NAME + " TEXT,"
            + KEY_CHAT_TYPE + " INTEGER,"
            + KEY_CHAT_CREATE_TIMESTAMP + " TEXT,"
            + KEY_CHAT_SORT_TIMESTAMP + " TEXT,"
            + KEY_CHAT_UNREAD_COUNT + " INTEGER,"
            + KEY_CHAT_LAST_READ_MESSAGE_TABLE_ID +  " INTEGER,"
            + KEY_CHAT_LAST_READ_RECEIPT_SENT_MESSAGE_TABLE_ID + " INTEGER" + ")";


    // group_participants table
    public static final String TABLE_GROUP_PARTICIPANTS = "group_participants";
    public static final String KEY_GROUP_ID = "id";
    public static final String KEY_GROUP_GJID = "gjid";
    public static final String KEY_GROUP_JID = "jid";
    public static final String KEY_GROUP_ADMIN = "admin";
    public static final String KEY_GROUP_PENDING = "pending";
    public static final String KEY_GROUP_SENT_SENDER_KEY = "sent_sender_key";
    private static final String CREATE_TABLE_GROUP_PARTICIPANTS = "CREATE TABLE " + TABLE_GROUP_PARTICIPANTS + "("
            + KEY_GROUP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_GROUP_GJID + " TEXT NOT NULL,"
            + KEY_GROUP_JID + " TEXT NOT NULL,"
            + KEY_GROUP_ADMIN + " INTEGER,"
            + KEY_GROUP_PENDING + " INTEGER,"
            + KEY_GROUP_SENT_SENDER_KEY + " INTEGER" + ")";


    // messages table
    public static final String TABLE_MESSAGES = "messages";
    public static final String KEY_MESSAGE_ID = "id";
    public static final String KEY_MESSAGE_SERVER_ID = "server_id";
    public static final String KEY_MESSAGE_CHAT_ID = "chat_id";
    public static final String KEY_MESSAGE_FROM_JID = "from_jid";
    public static final String KEY_MESSAGE_FROM_JID_RESOURCE = "from_jid_resource";
    public static final String KEY_MESSAGE_FROM_ME = "from_me";
    public static final String KEY_MESSAGE_THREAD_ID = "thread_id";
    public static final String KEY_MESSAGE_STATUS = "status";
    public static final String KEY_MESSAGE_NEEDS_PUSH = "needs_push";
    public static final String KEY_MESSAGE_DATA = "data";
    public static final String KEY_MESSAGE_TIMESTAMP = "timestamp";
    public static final String KEY_MESSAGE_RECEIVED_TIMESTAMP = "received_timestamp";
    public static final String KEY_MESSAGE_SEND_TIMESTAMP = "send_timestamp";
    public static final String KEY_MESSAGE_RECEIVED_SERVER_TIMESTAMP = "received_server_timestamp";
    public static final String KEY_MESSAGE_RECEIVED_DEVICE_TIMESTAMP = "received_device_timestamp";
    public static final String KEY_MESSAGE_READ_DEVICE_TIMESTAMP = "read_device_timestamp";
    public static final String KEY_MESSAGE_RECIPIENT_COUNT = "recipient_count";
    private static final String CREATE_TABLE_MESSAGES = "CREATE TABLE " + TABLE_MESSAGES + "("
            + KEY_MESSAGE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_MESSAGE_SERVER_ID + " INTEGER,"
            + KEY_MESSAGE_CHAT_ID + " TEXT NOT NULL,"
            + KEY_MESSAGE_FROM_JID + " TEXT,"
            + KEY_MESSAGE_FROM_JID_RESOURCE + " TEXT,"
            + KEY_MESSAGE_FROM_ME + " INTEGER,"
            + KEY_MESSAGE_THREAD_ID + " TEXT,"
            + KEY_MESSAGE_STATUS + " INTEGER,"
            + KEY_MESSAGE_NEEDS_PUSH + " INTEGER,"
            + KEY_MESSAGE_DATA + " TEXT,"
            + KEY_MESSAGE_TIMESTAMP + " TEXT,"
            + KEY_MESSAGE_RECEIVED_TIMESTAMP + " TEXT,"
            + KEY_MESSAGE_SEND_TIMESTAMP + " TEXT,"
            + KEY_MESSAGE_RECEIVED_SERVER_TIMESTAMP + " TEXT,"
            + KEY_MESSAGE_RECEIVED_DEVICE_TIMESTAMP + " TEXT,"
            + KEY_MESSAGE_READ_DEVICE_TIMESTAMP + " TEXT,"
            + KEY_MESSAGE_RECIPIENT_COUNT + " TEXT" + ")";


//    // contacts table
//    public static final String TABLE_CONTACTS = "contacts";
//    public static final String KEY_CONTACT_ID = "id";
//    public static final String KEY_CONTACT_JID = "jid";
//    public static final String KEY_CONTACT_NAME = "display_name";
//    public static final String KEY_CONTACT_STATUS = "status";
//    public static final String KEY_CONTACT_STATUS_TIMESTAMP = "status_timestamp";
//    public static final String KEY_CONTACT_PHOTO_THUMBNAIL = "photo_thumbnail";
//    public static final String KEY_CONTACT_PHOTO_URL = "photo_url";
//    public static final String KEY_CONTACT_PHOTO_TIMESTAMP = "photo_timestamp";
//    private static final String CREATE_TABLE_CONTACTS = "CREATE TABLE "+ TABLE_CONTACTS + "("
//            + KEY_CONTACT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
//            + KEY_CONTACT_JID + " TEXT NOT NULL,"
//            + KEY_CONTACT_NAME + " TEXT,"
//            + KEY_CONTACT_STATUS + " TEXT,"
//            + KEY_CONTACT_STATUS_TIMESTAMP + " TEXT,"
//            + KEY_CONTACT_PHOTO_THUMBNAIL + " TEXT,"
//            + KEY_CONTACT_PHOTO_URL + " TEXT,"
//            + KEY_CONTACT_PHOTO_TIMESTAMP + " TEXT" + ")";

    // key_value table
    public static final String TABLE_KEY_VALUE = "key_value";
    public static final String KEY_KV_ID = "key";
    public static final String KEY_KV_VALUE = "value";
    private static final String CREATE_TABLE_KEY_VALUE = "CREATE TABLE " + TABLE_KEY_VALUE + " ("
            + KEY_KV_ID + " TEXT PRIMARY KEY NOT NULL,"
            + KEY_KV_VALUE + " TEXT NOT NULL"+ ")";


    // muc(MultiUserChat) table
    public static final String TABLE_MUC = "muc";
    public static final String KEY_MUC_NAME = "name";
    public static final String KEY_MUC_ROOMJID = "room_jid";
    private static final String CREATE_TABLE_MUC = "CREATE TABLE " + TABLE_MUC + " ("
            + KEY_MUC_ROOMJID + " TEXT PRIMARY KEY NOT NULL,"
            + KEY_MUC_NAME + " TEXT NOT NULL" + ")";


    public DatabaseOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_KEY_VALUE);
        db.execSQL(CREATE_TABLE_MUC);
        db.execSQL(CREATE_TABLE_CHAT);
        db.execSQL(CREATE_TABLE_MESSAGES);
        db.execSQL(CREATE_TABLE_GROUP_PARTICIPANTS);
//        db.execSQL(CREATE_TABLE_CONTACTS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }



}