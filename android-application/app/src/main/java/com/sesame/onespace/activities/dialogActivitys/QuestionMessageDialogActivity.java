package com.sesame.onespace.activities.dialogActivitys;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;;

import com.sesame.onespace.R;
import com.sesame.onespace.models.chat.ChatMessage;
import com.sesame.onespace.models.chat.QueryMessage;
import com.sesame.onespace.service.xmpp.XmppManager;
import com.sesame.onespace.utils.DateTimeUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Thian on 8/12/2559.
 */

public class QuestionMessageDialogActivity
        extends AppCompatActivity {

    //===========================================================================================================//
    //  ATTRIBUTE                                                                                   ATTRIBUTE
    //===========================================================================================================//

    private Context context;

    private Intent intent;

    private AlertDialog alert;

    private XmppManager xmppManager;

    private String msgFrom;
    private String questionID;
    private String questionStr;
    private ArrayList<String> answerListID;
    private ArrayList<String> answerListStr;
    private String[] answerArrayID;
    private String[] answerArrayStr;

    private int select;

    //===========================================================================================================//
    //  ACTIVITY LIFECYCLE (MAIN BLOCK)                                                             ACTIVITY LIFECYCLE (MAIN BLOCK)
    //===========================================================================================================//

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //init


        //before


        //main
        super.onCreate(savedInstanceState);
        this.init();

        //after

    }

    @Override
    protected void onStart(){

        //init


        //before


        //main
        super.onStart();
        this.startDialog();

        //after

    }

    @Override
    protected void onResume() {

        //init


        //before


        //main
        super.onResume();

        //after

    }

    @Override
    public void onBackPressed() {

        //init


        //before


        //main


        //after


    }

    @Override
    protected  void onStop(){

        //init


        //before


        //main
        super.onStop();

        //after

    }

    @Override
    protected void onRestart(){

        //init


        //before


        //main
        super.onRestart();

        //after

    }

    @Override
    protected  void onDestroy(){

        //init


        //before


        //main
        super.onDestroy();

        //after


    }

    //===========================================================================================================//
    //  INIT                                                                                        INIT
    //===========================================================================================================//
    //  method  ------------------------------------------------------------------------------------****method****

    private void init(){

        this.initDefaultValue();
        this.initActivity();

    }

    private void initDefaultValue(){

        //init
        this.context = getApplicationContext();

        this.xmppManager = XmppManager.getInstance(this.context);

        this.intent = getIntent();
        this.msgFrom = this.intent.getStringExtra("msgFrom");
        this.questionID = this.intent.getStringExtra("idQuestion");
        this.questionStr = this.intent.getStringExtra("question");
        this.answerListID = this.intent.getStringArrayListExtra("answerArrayID");
        this.answerListStr = this.intent.getStringArrayListExtra("answerArrayStr");

        this.answerArrayID = new String[this.answerListID.size()];
        this.answerArrayStr = new String[this.answerListStr.size()];

        this.select = 0;

        //before


        //main
        int index = 0;

        while(index < this.answerListID.size()){

            this.answerArrayID[index] = this.answerListID.get(index);

            index = index + 1;

        }

        index = 0;

        while(index < this.answerListStr.size()){

            this.answerArrayStr[index] = this.answerListStr.get(index);

            index = index + 1;

        }

        //after


    }

    private void initActivity(){

        //init


        //before


        //main
        super.setContentView(R.layout.activity_dialog_question_message);

        //after


    }

    //===========================================================================================================//
    //  START DIALOG                                                                                START DIALOG
    //===========================================================================================================//
    //  method  ------------------------------------------------------------------------------------****method****

    private void startDialog(){

        //init
        QuestionMessageAlertDialogBuilder builder = new QuestionMessageAlertDialogBuilder(this, R.style.MyAlertDialogStyle2);

        //before
        builder.setTitle(this.questionStr);
        builder.setIcon(R.mipmap.ic_launcher_trim);

        builder.setSingleChoiceItems(this.answerArrayStr, -1, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int item) {

                select = item;

            }

        });

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int id) {

                //send answer to server

                String answerID = answerArrayID[select];
                String answerStr = answerArrayStr[select];

                JSONObject jsonObject = new JSONObject();
                JSONObject responseJsonObject = new JSONObject();
                JSONObject questionJsonObject = new JSONObject();
                JSONObject answerJsonObject = new JSONObject();

                try {

                    jsonObject.put(ChatMessage.KEY_MESSAGE_TYPE, "query");
                    jsonObject.put(ChatMessage.KEY_PRIMATIVE_TYPE, "response");

                    questionJsonObject.put("id", questionID);
                    questionJsonObject.put("str", questionStr);

                    answerJsonObject.put("id", answerID);
                    answerJsonObject.put("str", answerStr);

                    responseJsonObject.put("question", questionJsonObject);
                    responseJsonObject.put("answer", answerJsonObject);

                    jsonObject.put(QueryMessage.KEY_RESPONSE, responseJsonObject);

                    ChatMessage chatMessage = new ChatMessage.Builder().setChatID(msgFrom).setBody(jsonObject.toString()).setFromMe(true).setTimestamp(DateTimeUtil.getCurrentTimeStamp()).build();

                    if (chatMessage != null) {

                        xmppManager.broadcastMessageSent(chatMessage);

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                finish();
                dialog.dismiss();

            }

        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int id) {

                finish();
                dialog.dismiss();

            }

        });

        builder.setOnKeyListener(new Dialog.OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {

                // TODO Auto-generated method stub
                if (keyCode == KeyEvent.KEYCODE_HOME) {

                    finish();
                    dialog.dismiss();

                }

                if (keyCode == KeyEvent.KEYCODE_BACK) {

                    finish();
                    dialog.dismiss();

                }

                return true;
            }

        });

        this.alert = builder.create();
        this.alert.setCanceledOnTouchOutside(false);

        //main
        this.alert.show();

        //after
        this.alert.getListView().setItemChecked(this.select, true);

    }

    //===========================================================================================================//
    //  PRIVATE CLASS                                                                               PRIVATE CLASS
    //===========================================================================================================//

    public class QuestionMessageAlertDialogBuilder extends AlertDialog.Builder {

        private final Context context;
        private TextView title;
        private ImageView icon;

        public QuestionMessageAlertDialogBuilder(Context context, int theme) {
            super(context, theme);
            this.context = context;

            View customTitle = View.inflate(this.context, R.layout.alert_dialog_question_message_title, null);
            this.title = (TextView) customTitle.findViewById(R.id.alertTitle);
            this.icon = (ImageView) customTitle.findViewById(R.id.icon);
            setCustomTitle(customTitle);

        }

        @Override
        public QuestionMessageAlertDialogBuilder setTitle(int textResId) {
            this.title.setText(textResId);
            return this;
        }
        @Override
        public QuestionMessageAlertDialogBuilder setTitle(CharSequence text) {
            this.title.setText(text);
            return this;
        }

        @Override
        public QuestionMessageAlertDialogBuilder setIcon(int drawableResId) {
            this.icon.setImageResource(drawableResId);
            return this;
        }

        @Override
        public QuestionMessageAlertDialogBuilder setIcon(Drawable icon) {
            this.icon.setImageDrawable(icon);
            return this;
        }

    }

    //===========================================================================================================//
    //  NOTE                                                                                        NOTE
    //===========================================================================================================//

    //    ************9/12/2016 by Thianchai************

    //        android.app.AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle2);

    //before
//        builder.setTitle(this.question);
//        builder.setSingleChoiceItems(this.answerArrayStr, -1, new DialogInterface.OnClickListener() {
//
//            public void onClick(DialogInterface dialog, int item) {
//
//                select = item;
//
//            }
//
//        });

}
