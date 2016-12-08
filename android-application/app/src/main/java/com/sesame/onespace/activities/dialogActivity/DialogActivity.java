package com.sesame.onespace.activities.dialogActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.sesame.onespace.R;
import com.sesame.onespace.service.xmpp.XmppManager;

import java.util.ArrayList;

/**
 * Created by Thian on 8/12/2559.
 */

public class DialogActivity extends AppCompatActivity {

    private XmppManager xmppManager;

    private int select;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog_question_message);

        Intent intent = getIntent();
        String idQuestion = intent.getStringExtra("idQuestion");
        String question = intent.getStringExtra("question");
        ArrayList<String> answerListID = intent.getStringArrayListExtra("answerArrayID");
        ArrayList<String> answerListStr = intent.getStringArrayListExtra("answerArrayStr");

        String[] answerArrayID = new String[answerListID.size()];
        String[] answerArrayStr = new String[answerListStr.size()];

        this.select = 0;

        int index = 0;

        while(index < answerListID.size()){

            answerArrayID[index] = answerListID.get(index);

            index = index + 1;

        }

        index = 0;

        while(index < answerListStr.size()){

            answerArrayStr[index] = answerListStr.get(index);

            index = index + 1;

        }

        android.app.AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle2);
        builder.setTitle(question);
        builder.setSingleChoiceItems(answerArrayStr, -1, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int item) {

                select = item;

            }

        });

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int id) {

                //send answer to server

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

        AlertDialog alert = builder.create();
        alert.setCanceledOnTouchOutside(false);
        alert.show();

        alert.getListView().setItemChecked(this.select, true);

    }

}
