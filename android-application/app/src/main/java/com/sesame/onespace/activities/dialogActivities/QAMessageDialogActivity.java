package com.sesame.onespace.activities.dialogActivities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;;

import com.sesame.onespace.R;
import com.sesame.onespace.databases.qaMessageDatabases.QAMessageHelper;
import com.sesame.onespace.models.chat.ChatMessage;
import com.sesame.onespace.models.chat.QueryMessage;
import com.sesame.onespace.models.qaMessage.QAMessage;
import com.sesame.onespace.service.xmpp.XmppManager;
import com.sesame.onespace.utils.DateTimeUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Thian on 8/12/2559.
 */

public final class QAMessageDialogActivity
        extends AppCompatActivity {

    //===========================================================================================================//
    //  ATTRIBUTE                                                                                   ATTRIBUTE
    //===========================================================================================================//

    // QAMessage ---------------------------------------------------
    private QAMessage qaMessage;

    private Integer id;
    private String msgFrom;
    private String questionID;
    private String questionStr;
    private ArrayList<String> answerIdList;
    private ArrayList<String> answerStrList;
    private String date;

    // AlertDialog --------------------------------------------------
    private AlertDialog alertDialog;

    private XmppManager xmppManager;

    private String[] answerIdArray;
    private String[] answerStrArray;

    private Integer select;

    //===========================================================================================================//
    //  ON ACTION                                                                                   ON ACTION
    //===========================================================================================================//
    // lifecycle -----------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        QAMessageDialogActivity.super.onCreate(savedInstanceState);

        QAMessageDialogActivity.super.setContentView(R.layout.activity_dialog_qa);
        QAMessageDialogActivity.this.initDefaultValue();

    }

    @Override
    protected void onStart(){
        QAMessageDialogActivity.super.onStart();

        QAMessageDialogActivity.this.startDialog();

    }

    @Override
    protected void onStop(){
        QAMessageDialogActivity.super.onStop();

        if (QAMessageDialogActivity.this.alertDialog != null){

            QAMessageDialogActivity.this.alertDialog.dismiss();
            QAMessageDialogActivity.this.alertDialog = null;

        }

    }

    //===========================================================================================================//
    //  ON CREATE                                                                                   ON CREATE
    //===========================================================================================================//
    //  method  ------------------------------------------------------------------------------------****method****

    private void initDefaultValue(){

        Intent intent = getIntent();
        QAMessageDialogActivity.this.id = intent.getIntExtra("id", 0);
        QAMessageDialogActivity.this.msgFrom = intent.getStringExtra("msgFrom");
        QAMessageDialogActivity.this.questionID = intent.getStringExtra("questionId");
        QAMessageDialogActivity.this.questionStr = intent.getStringExtra("questionStr");
        QAMessageDialogActivity.this.answerIdList = intent.getStringArrayListExtra("answerIdList");
        QAMessageDialogActivity.this.answerStrList = intent.getStringArrayListExtra("answerStrList");
        QAMessageDialogActivity.this.date = intent.getStringExtra("date");

        QAMessageDialogActivity.this.qaMessage = new QAMessage(QAMessageDialogActivity.this.id,
                                                               QAMessageDialogActivity.this.msgFrom,
                                                               QAMessageDialogActivity.this.questionID,
                                                               QAMessageDialogActivity.this.questionStr,
                                                               QAMessageDialogActivity.this.answerIdList,
                                                               QAMessageDialogActivity.this.answerStrList,
                                                               QAMessageDialogActivity.this.date);

        //

        QAMessageDialogActivity.this.alertDialog = null;

        QAMessageDialogActivity.this.xmppManager = XmppManager.getInstance(QAMessageDialogActivity.this.getApplicationContext());

        QAMessageDialogActivity.this.answerIdArray = new String[QAMessageDialogActivity.this.answerIdList.size()];
        QAMessageDialogActivity.this.answerStrArray = new String[QAMessageDialogActivity.this.answerStrList.size()];

        int index = 0;

        while(index < QAMessageDialogActivity.this.answerIdList.size()){

            QAMessageDialogActivity.this.answerIdArray[index] = QAMessageDialogActivity.this.answerIdList.get(index);

            index = index + 1;

        }

        index = 0;

        while(index < QAMessageDialogActivity.this.answerStrList.size()){

            QAMessageDialogActivity.this.answerStrArray[index] = QAMessageDialogActivity.this.answerStrList.get(index);

            index = index + 1;

        }

        QAMessageDialogActivity.this.select = 0;

    }

    //===========================================================================================================//
    //  ON START                                                                                    ON START
    //===========================================================================================================//
    //  method  ------------------------------------------------------------------------------------****method****

    private void startDialog(){

        QAMessageDialogActivity.this.select = 0;

        QAAlertDialogBuilder qaAlertDialogBuilder = new QAAlertDialogBuilder(QAMessageDialogActivity.this, R.style.QAMessageDialogStyle);

        qaAlertDialogBuilder.setSender(QAMessageDialogActivity.this.msgFrom.split("@")[0] + " send message.");
        qaAlertDialogBuilder.setTitle(QAMessageDialogActivity.this.questionStr);
        qaAlertDialogBuilder.setIcon(R.mipmap.ic_launcher_trim);

        qaAlertDialogBuilder.setSingleChoiceItems(QAMessageDialogActivity.this.answerStrArray, -1, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int item) {

                QAMessageDialogActivity.this.select = item;

            }

        });

        qaAlertDialogBuilder.setPositiveButton("Send", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int id) {

                String answerID = QAMessageDialogActivity.this.answerIdArray[QAMessageDialogActivity.this.select];
                String answerStr = QAMessageDialogActivity.this.answerStrArray[QAMessageDialogActivity.this.select];

                JSONObject jsonObject = new JSONObject();
                JSONObject responseJsonObject = new JSONObject();
                JSONObject questionJsonObject = new JSONObject();
                JSONObject answerJsonObject = new JSONObject();

                try {

                    jsonObject.put(ChatMessage.KEY_MESSAGE_TYPE, "query");
                    jsonObject.put(ChatMessage.KEY_PRIMATIVE_TYPE, "response");

                    questionJsonObject.put("id", QAMessageDialogActivity.this.questionID);
                    questionJsonObject.put("str", QAMessageDialogActivity.this.questionStr);

                    answerJsonObject.put("id", answerID);
                    answerJsonObject.put("str", answerStr);

                    responseJsonObject.put("question", questionJsonObject);
                    responseJsonObject.put("answer", answerJsonObject);

                    jsonObject.put(QueryMessage.KEY_RESPONSE, responseJsonObject);

                    ChatMessage chatMessage = new ChatMessage.Builder().setChatID(QAMessageDialogActivity.this.msgFrom).setBody(jsonObject.toString()).setFromMe(true).setTimestamp(DateTimeUtil.getCurrentTimeStamp()).build();

                    if (chatMessage != null) {

                        QAMessageDialogActivity.this.xmppManager.broadcastMessageSent(chatMessage);

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                QAMessageDialogActivity.this.removeQAMessage();

                QAMessageDialogActivity.super.finish();
                QAMessageDialogActivity.this.alertDialog.dismiss();
                QAMessageDialogActivity.this.alertDialog = null;

            }

        });

        qaAlertDialogBuilder.setNeutralButton("Later", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int id) {

                QAMessageDialogActivity.super.finish();
                QAMessageDialogActivity.this.alertDialog.dismiss();
                QAMessageDialogActivity.this.alertDialog = null;

            }

        });

        qaAlertDialogBuilder.setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int id) {

                QAMessageDialogActivity.this.removeQAMessage();

                QAMessageDialogActivity.super.finish();
                QAMessageDialogActivity.this.alertDialog.dismiss();
                QAMessageDialogActivity.this.alertDialog = null;

            }

        });

        qaAlertDialogBuilder.setOnKeyListener(new Dialog.OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {

                // TODO Auto-generated method stub
                if (keyCode == KeyEvent.KEYCODE_HOME) {

                    QAMessageDialogActivity.super.finish();
                    QAMessageDialogActivity.this.alertDialog.dismiss();
                    QAMessageDialogActivity.this.alertDialog = null;

                }

                if (keyCode == KeyEvent.KEYCODE_BACK) {

                    QAMessageDialogActivity.super.finish();
                    QAMessageDialogActivity.this.alertDialog.dismiss();
                    QAMessageDialogActivity.this.alertDialog = null;

                }

                return true;
            }

        });

        QAMessageDialogActivity.this.alertDialog = qaAlertDialogBuilder.create();
        QAMessageDialogActivity.this.alertDialog.setCanceledOnTouchOutside(false);

        QAMessageDialogActivity.this.alertDialog.show();

        QAMessageDialogActivity.this.alertDialog.getListView().setItemChecked(QAMessageDialogActivity.this.select, true);

    }

    //  private class  -----------------------------------------------------------------------------****private class****

    private final class QAAlertDialogBuilder
            extends AlertDialog.Builder {

        private Context context;
        private ImageView icon;
        private TextView sender;
        private TextView title;

        public QAAlertDialogBuilder(Context context, int theme) {
            super(context, theme);

            QAAlertDialogBuilder.this.context = context;

            View customTitle = View.inflate(QAAlertDialogBuilder.this.context, R.layout.alert_dialog_qa_title, null);
            QAAlertDialogBuilder.this.icon = (ImageView) customTitle.findViewById(R.id.icon_onespace);
            QAAlertDialogBuilder.this.sender = (TextView) customTitle.findViewById(R.id.text_sender_name);
            QAAlertDialogBuilder.this.title = (TextView) customTitle.findViewById(R.id.text_title_message);
            setCustomTitle(customTitle);

        }

        @Override
        public QAAlertDialogBuilder setTitle(int textResId) {
            QAAlertDialogBuilder.this.title.setText(textResId);
            return QAAlertDialogBuilder.this;
        }
        @Override
        public QAAlertDialogBuilder setTitle(CharSequence title) {
            QAAlertDialogBuilder.this.title.setText(title);
            return QAAlertDialogBuilder.this;
        }

        public QAAlertDialogBuilder setSender(CharSequence sender) {
            QAAlertDialogBuilder.this.sender.setText(sender);
            return QAAlertDialogBuilder.this;
        }

        @Override
        public QAAlertDialogBuilder setIcon(int drawableResId) {
            QAAlertDialogBuilder.this.icon.setImageResource(drawableResId);
            return QAAlertDialogBuilder.this;
        }

        @Override
        public QAAlertDialogBuilder setIcon(Drawable icon) {
            QAAlertDialogBuilder.this.icon.setImageDrawable(icon);
            return QAAlertDialogBuilder.this;
        }

    }

    //===========================================================================================================//
    //  OTHER METHOD                                                                                OTHER METHOD
    //===========================================================================================================//

    private void removeQAMessage(){

        QAMessageHelper qaMessageHelper = new QAMessageHelper(QAMessageDialogActivity.this.getApplicationContext());
        qaMessageHelper.deleteQAMessage(QAMessageDialogActivity.this.qaMessage);

    }

    //===========================================================================================================//
    //  NOTE                                                                                        NOTE
    //===========================================================================================================//

}
