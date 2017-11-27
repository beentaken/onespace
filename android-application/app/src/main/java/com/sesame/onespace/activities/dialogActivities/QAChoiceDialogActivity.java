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
import com.sesame.onespace.models.qaMessage.QAMessage;
import com.sesame.onespace.service.xmpp.XmppManager;
import com.sesame.onespace.utils.DateTimeUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Thian on 8/12/2559.
 */

public final class QAChoiceDialogActivity
        extends AppCompatActivity {

    //===========================================================================================================//
    //  ATTRIBUTE                                                                                   ATTRIBUTE
    //===========================================================================================================//

    // QAMessage ---------------------------------------------------
    private QAMessage qaMessage;

    private Integer id;
    private String msgFrom;
    private String type;
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
        QAChoiceDialogActivity.super.onCreate(savedInstanceState);

        QAChoiceDialogActivity.super.setContentView(R.layout.activity_dialog_qa);
        QAChoiceDialogActivity.this.initDefaultValue();

    }

    @Override
    protected void onStart(){
        QAChoiceDialogActivity.super.onStart();

        QAChoiceDialogActivity.this.startDialog();

    }

    @Override
    protected void onStop(){
        QAChoiceDialogActivity.super.onStop();

        if (QAChoiceDialogActivity.this.alertDialog != null){

            QAChoiceDialogActivity.this.alertDialog.dismiss();
            QAChoiceDialogActivity.this.alertDialog = null;

        }

    }

    //===========================================================================================================//
    //  ON CREATE                                                                                   ON CREATE
    //===========================================================================================================//
    //  method  ------------------------------------------------------------------------------------****method****

    private void initDefaultValue(){

        Intent intent = getIntent();
        QAChoiceDialogActivity.this.id = intent.getIntExtra("id", 0);
        QAChoiceDialogActivity.this.msgFrom = intent.getStringExtra("msgFrom");
        QAChoiceDialogActivity.this.type = intent.getStringExtra("type");
        QAChoiceDialogActivity.this.questionID = intent.getStringExtra("questionId");
        QAChoiceDialogActivity.this.questionStr = intent.getStringExtra("questionStr");
        QAChoiceDialogActivity.this.answerIdList = intent.getStringArrayListExtra("answerIdList");
        QAChoiceDialogActivity.this.answerStrList = intent.getStringArrayListExtra("answerStrList");
        QAChoiceDialogActivity.this.date = intent.getStringExtra("date");

        QAChoiceDialogActivity.this.qaMessage = new QAMessage(QAChoiceDialogActivity.this.id,
                                                              QAChoiceDialogActivity.this.msgFrom,
                                                              QAChoiceDialogActivity.this.type,
                                                              QAChoiceDialogActivity.this.questionID,
                                                              QAChoiceDialogActivity.this.questionStr,
                                                              QAChoiceDialogActivity.this.answerIdList,
                                                              QAChoiceDialogActivity.this.answerStrList,
                                                              QAChoiceDialogActivity.this.date);

        //

        QAChoiceDialogActivity.this.alertDialog = null;

        QAChoiceDialogActivity.this.xmppManager = XmppManager.getInstance(QAChoiceDialogActivity.this.getApplicationContext());

        QAChoiceDialogActivity.this.answerIdArray = new String[QAChoiceDialogActivity.this.answerIdList.size()];
        QAChoiceDialogActivity.this.answerStrArray = new String[QAChoiceDialogActivity.this.answerStrList.size()];

        int index = 0;

        while(index < QAChoiceDialogActivity.this.answerIdList.size()){

            QAChoiceDialogActivity.this.answerIdArray[index] = QAChoiceDialogActivity.this.answerIdList.get(index);

            index = index + 1;

        }

        index = 0;

        while(index < QAChoiceDialogActivity.this.answerStrList.size()){

            QAChoiceDialogActivity.this.answerStrArray[index] = QAChoiceDialogActivity.this.answerStrList.get(index);

            index = index + 1;

        }

        QAChoiceDialogActivity.this.select = 0;

    }

    //===========================================================================================================//
    //  ON START                                                                                    ON START
    //===========================================================================================================//
    //  method  ------------------------------------------------------------------------------------****method****

    private void startDialog(){

        QAChoiceDialogActivity.this.select = 0;

        QAChoiceDialogActivity.QAAlertDialogBuilder qaAlertDialogBuilder = new QAChoiceDialogActivity.QAAlertDialogBuilder(QAChoiceDialogActivity.this, R.style.QAMessageDialogStyle);

        qaAlertDialogBuilder.setSender(QAChoiceDialogActivity.this.msgFrom.split("@")[0] + " asked:");
        qaAlertDialogBuilder.setTitle(QAChoiceDialogActivity.this.questionStr);
        qaAlertDialogBuilder.setIcon(R.mipmap.ic_launcher_trim);

        qaAlertDialogBuilder.setSingleChoiceItems(QAChoiceDialogActivity.this.answerStrArray, -1, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int item) {

                QAChoiceDialogActivity.this.select = item;

            }

        });

        qaAlertDialogBuilder.setPositiveButton("Send", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int id) {

                String answerID = QAChoiceDialogActivity.this.answerIdArray[QAChoiceDialogActivity.this.select];
                String answerStr = QAChoiceDialogActivity.this.answerStrArray[QAChoiceDialogActivity.this.select];

                JSONObject jsonObject = new JSONObject();
                JSONObject responseJsonObject = new JSONObject();
                JSONObject questionJsonObject = new JSONObject();
                JSONObject answerJsonObject = new JSONObject();

                try {

                    jsonObject.put("message-type", "query");
                    jsonObject.put("primitive", "response");
                    jsonObject.put("media", "text");

                    questionJsonObject.put("id", QAChoiceDialogActivity.this.questionID);
                    questionJsonObject.put("str", QAChoiceDialogActivity.this.questionStr);

                    answerJsonObject.put("id", answerID);
                    answerJsonObject.put("str", answerStr);

                    responseJsonObject.put("question", questionJsonObject);
                    responseJsonObject.put("answer", answerJsonObject);

                    jsonObject.put("response", responseJsonObject);

                    ChatMessage chatMessage = new ChatMessage.Builder().setChatID(QAChoiceDialogActivity.this.msgFrom).setBody(jsonObject.toString()).setFromMe(true).setTimestamp(DateTimeUtil.getCurrentTimeStamp()).build();

                    if (chatMessage != null) {

                        QAChoiceDialogActivity.this.xmppManager.sendQAMessage(chatMessage);

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                QAChoiceDialogActivity.this.removeQAMessage();

                QAChoiceDialogActivity.super.finish();
                QAChoiceDialogActivity.this.alertDialog.dismiss();
                QAChoiceDialogActivity.this.alertDialog = null;

            }

        });

        qaAlertDialogBuilder.setNeutralButton("Later", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int id) {

                QAChoiceDialogActivity.super.finish();
                QAChoiceDialogActivity.this.alertDialog.dismiss();
                QAChoiceDialogActivity.this.alertDialog = null;

            }

        });

        qaAlertDialogBuilder.setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int id) {

                String answerID = "-1";
                String answerStr = "";

                JSONObject jsonObject = new JSONObject();
                JSONObject responseJsonObject = new JSONObject();
                JSONObject questionJsonObject = new JSONObject();
                JSONObject answerJsonObject = new JSONObject();

                try {

                    jsonObject.put("message-type", "query");
                    jsonObject.put("primitive", "response");
                    jsonObject.put("media", "text");

                    questionJsonObject.put("id", QAChoiceDialogActivity.this.questionID);
                    questionJsonObject.put("str", QAChoiceDialogActivity.this.questionStr);

                    answerJsonObject.put("id", answerID);
                    answerJsonObject.put("str", answerStr);

                    responseJsonObject.put("question", questionJsonObject);
                    responseJsonObject.put("answer", answerJsonObject);

                    jsonObject.put("response", responseJsonObject);

                    ChatMessage chatMessage = new ChatMessage.Builder().setChatID(QAChoiceDialogActivity.this.msgFrom).setBody(jsonObject.toString()).setFromMe(true).setTimestamp(DateTimeUtil.getCurrentTimeStamp()).build();

                    if (chatMessage != null) {

                        QAChoiceDialogActivity.this.xmppManager.sendQAMessage(chatMessage);

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                QAChoiceDialogActivity.this.removeQAMessage();

                QAChoiceDialogActivity.super.finish();
                QAChoiceDialogActivity.this.alertDialog.dismiss();
                QAChoiceDialogActivity.this.alertDialog = null;

            }

        });

        qaAlertDialogBuilder.setOnKeyListener(new Dialog.OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {

                // TODO Auto-generated method stub
                if (keyCode == KeyEvent.KEYCODE_HOME) {

                    QAChoiceDialogActivity.super.finish();
                    QAChoiceDialogActivity.this.alertDialog.dismiss();
                    QAChoiceDialogActivity.this.alertDialog = null;

                }

                if (keyCode == KeyEvent.KEYCODE_BACK) {

                    QAChoiceDialogActivity.super.finish();
                    QAChoiceDialogActivity.this.alertDialog.dismiss();
                    QAChoiceDialogActivity.this.alertDialog = null;

                }

                return true;
            }

        });

        QAChoiceDialogActivity.this.alertDialog = qaAlertDialogBuilder.create();
        QAChoiceDialogActivity.this.alertDialog.setCanceledOnTouchOutside(false);

        QAChoiceDialogActivity.this.alertDialog.show();

        QAChoiceDialogActivity.this.alertDialog.getListView().setItemChecked(QAChoiceDialogActivity.this.select, true);

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

        QAMessageHelper qaMessageHelper = new QAMessageHelper(QAChoiceDialogActivity.this.getApplicationContext());
        qaMessageHelper.deleteQAMessage(QAChoiceDialogActivity.this.qaMessage);

    }

    //===========================================================================================================//
    //  NOTE                                                                                        NOTE
    //===========================================================================================================//

}
