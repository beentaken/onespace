package com.sesame.onespace.databases.qaMessageDatabases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.sesame.onespace.models.qaMessage.QAMessage;
import com.sesame.onespace.utils.database.DatabaseConvert;

import java.util.ArrayList;

/**
 * Created by Thian on 21/12/2559.
 */

public final class QAMessageHelper
        extends SQLiteOpenHelper {

    //===========================================================================================================//
    //  ATTRIBUTE                                                                                   ATTRIBUTE
    //===========================================================================================================//

    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "QAMessageDB";

    private static final String TABLE_QAMESSAGES = "qaMessages";

    private static final String KEY_ID = "id";
    private static final String KEY_MSGFROM = "msgFrom";
    private static final String KEY_QUESTIONID = "questionID";
    private static final String KEY_QUESTIONSTR = "questionStr";
    private static final String KEY_ANSWERIDLIST = "answerIDList";
    private static final String KEY_ANSWERSTRLIST = "answerStrList";
    private static final String KEY_DATE = "date";

    private static final String[] COLUMNS = {QAMessageHelper.KEY_ID,
                                             QAMessageHelper.KEY_MSGFROM,
                                             QAMessageHelper.KEY_QUESTIONID,
                                             QAMessageHelper.KEY_QUESTIONSTR,
                                             QAMessageHelper.KEY_ANSWERIDLIST,
                                             QAMessageHelper.KEY_ANSWERSTRLIST,
                                             QAMessageHelper.KEY_DATE};

    private static final String CREATE_QAMESSAGE_TABLE = "CREATE TABLE " + QAMessageHelper.TABLE_QAMESSAGES + " ( " +
                                                                      "" + QAMessageHelper.KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                                                      "" + QAMessageHelper.KEY_MSGFROM + " TEXT, "+
                                                                      "" + QAMessageHelper.KEY_QUESTIONID + " TEXT, "+
                                                                      "" + QAMessageHelper.KEY_QUESTIONSTR + " TEXT, "+
                                                                      "" + QAMessageHelper.KEY_ANSWERIDLIST + " TEXT, "+
                                                                      "" + QAMessageHelper.KEY_ANSWERSTRLIST + " TEXT, "+
                                                                      "" + QAMessageHelper.KEY_DATE+ " TEXT )";

    //===========================================================================================================//
    //  CONSTRUCTOR                                                                                 CONSTRUCTOR
    //===========================================================================================================//

    public QAMessageHelper(Context context) {
        super(context, QAMessageHelper.DATABASE_NAME, null, QAMessageHelper.DATABASE_VERSION);

    }

    //===========================================================================================================//
    //  ON ACTION                                                                                   ON ACTION
    //===========================================================================================================//

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        sqLiteDatabase.execSQL(QAMessageHelper.CREATE_QAMESSAGE_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + QAMessageHelper.TABLE_QAMESSAGES + "");

        QAMessageHelper.this.onCreate(sqLiteDatabase);

    }

    //===========================================================================================================//
    //  OTHER METHOD                                                                                OTHER METHOD
    //===========================================================================================================//

    public Long getCount(){

        SQLiteDatabase db = this.getReadableDatabase();
        long cnt  = DatabaseUtils.queryNumEntries(db, QAMessageHelper.TABLE_QAMESSAGES);
        db.close();
        return cnt;

    }

    public void addQAMessage(QAMessage qaMessage){

        SQLiteDatabase sqLiteDatabase = QAMessageHelper.this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(QAMessageHelper.KEY_MSGFROM, qaMessage.getMsgFrom());
        contentValues.put(QAMessageHelper.KEY_QUESTIONID, qaMessage.getQuestionID());
        contentValues.put(QAMessageHelper.KEY_QUESTIONSTR, qaMessage.getQuestionStr());
        contentValues.put(QAMessageHelper.KEY_ANSWERIDLIST, DatabaseConvert.convertArrayListToString(qaMessage.getAnswerIDList()));
        contentValues.put(QAMessageHelper.KEY_ANSWERSTRLIST, DatabaseConvert.convertArrayListToString(qaMessage.getAnswerStrList()));
        contentValues.put(QAMessageHelper.KEY_DATE, qaMessage.getDate());

        sqLiteDatabase.insert(QAMessageHelper.TABLE_QAMESSAGES, null, contentValues);

        sqLiteDatabase.close();
    }

    public QAMessage getQAMessage(int id){

        SQLiteDatabase sqLiteDatabase = QAMessageHelper.this.getReadableDatabase();

        Cursor cursor = sqLiteDatabase.query(QAMessageHelper.TABLE_QAMESSAGES,
                                 QAMessageHelper.COLUMNS,
                           " " + QAMessageHelper.KEY_ID + " = ?",
                                 new String[] { String.valueOf(id) },
                                 null,
                                 null,
                                 null,
                                 null);


        if (cursor != null) {

            cursor.moveToFirst();

        }


        QAMessage qaMessage = new QAMessage(Integer.parseInt(cursor.getString(0)),
                                            cursor.getString(1),
                                            cursor.getString(2),
                                            cursor.getString(3),
                                            DatabaseConvert.convertStringToArrayList(cursor.getString(4)),
                                            DatabaseConvert.convertStringToArrayList(cursor.getString(5)),
                                            cursor.getString(6));



        return qaMessage;
    }

    public ArrayList<QAMessage> getAllQAMessages() {

        ArrayList<QAMessage> qaMessages = new ArrayList<QAMessage>();

        String query = "SELECT  * FROM " + QAMessageHelper.TABLE_QAMESSAGES;

        SQLiteDatabase sqLiteDatabase = QAMessageHelper.this.getWritableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);

        QAMessage qaMessage = null;

        if (cursor.moveToFirst()) {
            do {

                qaMessage = new QAMessage(Integer.parseInt(cursor.getString(0)),
                                          cursor.getString(1),
                                          cursor.getString(2),
                                          cursor.getString(3),
                                          DatabaseConvert.convertStringToArrayList(cursor.getString(4)),
                                          DatabaseConvert.convertStringToArrayList(cursor.getString(5)),
                                          cursor.getString(6));

                qaMessages.add(qaMessage);

            } while (cursor.moveToNext());
        }

        return qaMessages;
    }

    public void deleteQAMessage(QAMessage qaMessage) {

        SQLiteDatabase sqLiteDatabase = QAMessageHelper.this.getWritableDatabase();

        sqLiteDatabase.delete(QAMessageHelper.TABLE_QAMESSAGES,
                  QAMessageHelper.KEY_ID + " = ?",
                  new String[] { String.valueOf(qaMessage.getId()) });

        sqLiteDatabase.close();

    }

}
