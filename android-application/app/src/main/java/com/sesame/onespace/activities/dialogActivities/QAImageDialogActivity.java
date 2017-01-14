package com.sesame.onespace.activities.dialogActivities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.sesame.onespace.R;
import com.sesame.onespace.databases.qaMessageDatabases.QAMessageHelper;
import com.sesame.onespace.managers.SettingsManager;
import com.sesame.onespace.managers.UserAccountManager;
import com.sesame.onespace.managers.chat.MediaUploadManager;
import com.sesame.onespace.models.chat.ChatMessage;
import com.sesame.onespace.models.chat.ImageMessage;
import com.sesame.onespace.models.chat.UploadImage;
import com.sesame.onespace.models.qaMessage.QAMessage;
import com.sesame.onespace.service.xmpp.XmppManager;
import com.sesame.onespace.utils.DateTimeUtil;
import com.sesame.onespace.utils.FilePathUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Thian on 14/1/2560.
 */

public final class QAImageDialogActivity
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
    private QAImageDialogActivity.QAAlertDialogBuilder qaAlertDialogBuilder;

    private XmppManager xmppManager;

    private String[] answerIdArray;
    private String[] answerStrArray;

    private final static int CAMERA_IMAGE_REQUEST = 0;
    private final static int GALLERY_IMAGE_REQUEST = 1;

    private Uri captureFileUri;
    private String selectedImagePath;

    private SettingsManager settingsManager;
    private String userJID;

    private Handler handler;

    //===========================================================================================================//
    //  ON ACTION                                                                                   ON ACTION
    //===========================================================================================================//
    // lifecycle -----------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        QAImageDialogActivity.super.onCreate(savedInstanceState);

        QAImageDialogActivity.super.setContentView(R.layout.activity_dialog_qa);
        QAImageDialogActivity.this.initDefaultValue();

    }

    @Override
    protected void onStart(){
        QAImageDialogActivity.super.onStart();

        QAImageDialogActivity.this.startDialog();

    }

    @Override
    protected void onStop(){
        QAImageDialogActivity.super.onStop();

        if (QAImageDialogActivity.this.alertDialog != null){

            QAImageDialogActivity.this.alertDialog.dismiss();
            QAImageDialogActivity.this.alertDialog = null;

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {

            if(resultCode == Activity.RESULT_OK) {

                QAImageDialogActivity.this.selectedImagePath = null;

                if (requestCode == QAImageDialogActivity.CAMERA_IMAGE_REQUEST) {

                    QAImageDialogActivity.this.selectedImagePath = QAImageDialogActivity.this.captureFileUri.getPath();

                }

                if (requestCode == QAImageDialogActivity.GALLERY_IMAGE_REQUEST && data != null) {

                    Uri selectedImageUri = data.getData();
                    selectedImageUri.getPath();
                    QAImageDialogActivity.this.selectedImagePath = FilePathUtil.getPath(QAImageDialogActivity.this.getApplicationContext(), selectedImageUri);

                }

            }

        }
        catch (Exception e) {

            e.printStackTrace();

        }

    }

    //===========================================================================================================//
    //  ON CREATE                                                                                   ON CREATE
    //===========================================================================================================//
    //  method  ------------------------------------------------------------------------------------****method****

    private void initDefaultValue(){

        Intent intent = getIntent();
        QAImageDialogActivity.this.id = intent.getIntExtra("id", 0);
        QAImageDialogActivity.this.msgFrom = intent.getStringExtra("msgFrom");
        QAImageDialogActivity.this.type = intent.getStringExtra("type");
        QAImageDialogActivity.this.questionID = intent.getStringExtra("questionId");
        QAImageDialogActivity.this.questionStr = intent.getStringExtra("questionStr");
        QAImageDialogActivity.this.answerIdList = intent.getStringArrayListExtra("answerIdList");
        QAImageDialogActivity.this.answerStrList = intent.getStringArrayListExtra("answerStrList");
        QAImageDialogActivity.this.date = intent.getStringExtra("date");

        QAImageDialogActivity.this.qaMessage = new QAMessage(QAImageDialogActivity.this.id,
                                                             QAImageDialogActivity.this.msgFrom,
                                                             QAImageDialogActivity.this.type,
                                                             QAImageDialogActivity.this.questionID,
                                                             QAImageDialogActivity.this.questionStr,
                                                             QAImageDialogActivity.this.answerIdList,
                                                             QAImageDialogActivity.this.answerStrList,
                                                             QAImageDialogActivity.this.date);

        //

        QAImageDialogActivity.this.alertDialog = null;

        QAImageDialogActivity.this.xmppManager = XmppManager.getInstance(QAImageDialogActivity.this.getApplicationContext());

        QAImageDialogActivity.this.answerIdArray = new String[QAImageDialogActivity.this.answerIdList.size()];
        QAImageDialogActivity.this.answerStrArray = new String[QAImageDialogActivity.this.answerStrList.size()];

        int index = 0;

        while(index < QAImageDialogActivity.this.answerIdList.size()){

            QAImageDialogActivity.this.answerIdArray[index] = QAImageDialogActivity.this.answerIdList.get(index);

            index = index + 1;

        }

        index = 0;

        while(index < QAImageDialogActivity.this.answerStrList.size()){

            QAImageDialogActivity.this.answerStrArray[index] = QAImageDialogActivity.this.answerStrList.get(index);

            index = index + 1;

        }

        QAImageDialogActivity.this.captureFileUri = null;
        QAImageDialogActivity.this.selectedImagePath = null;

        QAImageDialogActivity.this.settingsManager = SettingsManager.getSettingsManager(QAImageDialogActivity.this.getApplicationContext());

        UserAccountManager userAccountManager = settingsManager.getUserAccountManager();
        QAImageDialogActivity.this.userJID = userAccountManager.getUsername() + "@" + settingsManager.xmppServer;

        QAImageDialogActivity.this.handler = new Handler();

    }

    //===========================================================================================================//
    //  ON START                                                                                    ON START
    //===========================================================================================================//
    //  method  ------------------------------------------------------------------------------------****method****

    private void startDialog(){

        QAImageDialogActivity.this.qaAlertDialogBuilder = new QAImageDialogActivity.QAAlertDialogBuilder(QAImageDialogActivity.this, R.style.QAMessageDialogStyle);

        qaAlertDialogBuilder.setSender(QAImageDialogActivity.this.msgFrom.split("@")[0] + " send message.");
        qaAlertDialogBuilder.setTitle(QAImageDialogActivity.this.questionStr);
        qaAlertDialogBuilder.setIcon(R.mipmap.ic_launcher_trim);

        if (QAImageDialogActivity.this.selectedImagePath != null){

            QAImageDialogActivity.this.qaAlertDialogBuilder.showImage();

        }

        qaAlertDialogBuilder.setPositiveButton("Send", null);
        qaAlertDialogBuilder.setNeutralButton("Later", null);
        qaAlertDialogBuilder.setNegativeButton("Dismiss", null);

        qaAlertDialogBuilder.setOnKeyListener(new Dialog.OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {

                // TODO Auto-generated method stub
                if (keyCode == KeyEvent.KEYCODE_HOME) {

                    QAImageDialogActivity.super.finish();
                    QAImageDialogActivity.this.alertDialog.dismiss();
                    QAImageDialogActivity.this.alertDialog = null;

                }

                if (keyCode == KeyEvent.KEYCODE_BACK) {

                    QAImageDialogActivity.super.finish();
                    QAImageDialogActivity.this.alertDialog.dismiss();
                    QAImageDialogActivity.this.alertDialog = null;

                }

                return true;
            }

        });

        QAImageDialogActivity.this.alertDialog = qaAlertDialogBuilder.create();
        QAImageDialogActivity.this.alertDialog.setCanceledOnTouchOutside(false);
        QAImageDialogActivity.this.alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {

                Button positiveButton = QAImageDialogActivity.this.alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        Toast.makeText(QAImageDialogActivity.this, "Uploading...", Toast.LENGTH_SHORT).show();

                        String mimeType = "image/*";
                        QAImageDialogActivity.this.uploadImage(QAImageDialogActivity.this.selectedImagePath, mimeType);

                        QAImageDialogActivity.this.alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

                    }

                });

                Button neutralButton = QAImageDialogActivity.this.alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
                neutralButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        QAImageDialogActivity.super.finish();
                        QAImageDialogActivity.this.alertDialog.dismiss();
                        QAImageDialogActivity.this.alertDialog = null;

                    }

                });

                Button negativeButton = QAImageDialogActivity.this.alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                negativeButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        String answerID = "-1";

                        JSONObject jsonObject = new JSONObject();
                        JSONObject responseJsonObject = new JSONObject();
                        JSONObject questionJsonObject = new JSONObject();
                        JSONObject answerJsonObject = new JSONObject();

                        try {

                            jsonObject.put("message-type", "query");
                            jsonObject.put("primitive", "response");
                            jsonObject.put("media", "image");

                            questionJsonObject.put("id", QAImageDialogActivity.this.questionID);
                            questionJsonObject.put("str", QAImageDialogActivity.this.questionStr);

                            answerJsonObject.put("id", answerID);
                            answerJsonObject.put("thumbnail_url", "");
                            answerJsonObject.put("image_url", "");

                            responseJsonObject.put("question", questionJsonObject);
                            responseJsonObject.put("answer", answerJsonObject);

                            jsonObject.put("response", responseJsonObject);

                            ChatMessage chatMessage = new ChatMessage.Builder().setChatID(QAImageDialogActivity.this.msgFrom).setBody(jsonObject.toString()).setFromMe(true).setTimestamp(DateTimeUtil.getCurrentTimeStamp()).build();

                            if (chatMessage != null) {

                                QAImageDialogActivity.this.xmppManager.sendQAMessage(chatMessage);

                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        QAImageDialogActivity.this.removeQAMessage();

                        QAImageDialogActivity.super.finish();
                        QAImageDialogActivity.this.alertDialog.dismiss();
                        QAImageDialogActivity.this.alertDialog = null;

                    }

                });

            }
        });
        QAImageDialogActivity.this.alertDialog.show();

        if (QAImageDialogActivity.this.selectedImagePath != null){

            QAImageDialogActivity.this.alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);

        }
        else{

            QAImageDialogActivity.this.alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

        }

    }

    //  private class  -----------------------------------------------------------------------------****private class****

    private final class QAAlertDialogBuilder
            extends AlertDialog.Builder {

        private Context context;

        private ImageView icon;
        private TextView sender;
        private TextView title;

        private Button cameraButton;
        private Button galleryButton;
        private ImageView imageView;

        public QAAlertDialogBuilder(Context context, int theme) {
            super(context, theme);

            QAAlertDialogBuilder.this.context = context;

            View customTitle = View.inflate(QAAlertDialogBuilder.this.context, R.layout.alert_dialog_qa_title, null);
            View customBody = View.inflate(QAAlertDialogBuilder.this.context, R.layout.alert_dialog_qa_image_body, null);

            QAAlertDialogBuilder.this.icon = (ImageView) customTitle.findViewById(R.id.icon_onespace);
            QAAlertDialogBuilder.this.sender = (TextView) customTitle.findViewById(R.id.text_sender_name);
            QAAlertDialogBuilder.this.title = (TextView) customTitle.findViewById(R.id.text_title_message);

            QAAlertDialogBuilder.this.cameraButton = (Button) customBody.findViewById(R.id.button_camera);

            QAAlertDialogBuilder.this.cameraButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    QAImageDialogActivity.this.captureFileUri = Uri.fromFile(FilePathUtil.getOutputMediaFile());
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, captureFileUri);
                    QAImageDialogActivity.super.startActivityForResult(intent, QAImageDialogActivity.CAMERA_IMAGE_REQUEST);

                }

            });

            QAAlertDialogBuilder.this.galleryButton = (Button) customBody.findViewById(R.id.button_gallery);

            QAAlertDialogBuilder.this.galleryButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    QAImageDialogActivity.super.startActivityForResult(galleryIntent, QAImageDialogActivity.GALLERY_IMAGE_REQUEST);

                }

            });

            QAAlertDialogBuilder.this.imageView = (ImageView) customBody.findViewById(R.id.image_qa);
            QAAlertDialogBuilder.this.imageView.setVisibility(View.GONE);

            QAAlertDialogBuilder.super.setCustomTitle(customTitle);
            QAAlertDialogBuilder.super.setView(customBody);

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

        public void showImage(){

            QAAlertDialogBuilder.this.cameraButton.setVisibility(View.GONE);
            QAAlertDialogBuilder.this.galleryButton.setVisibility(View.GONE);

            Bitmap image = BitmapFactory.decodeFile(QAImageDialogActivity.this.selectedImagePath);
            QAAlertDialogBuilder.this.imageView.setImageBitmap(image);
            QAAlertDialogBuilder.this.imageView.setVisibility(View.VISIBLE);

        }

    }

    //===========================================================================================================//
    //  OTHER METHOD                                                                                OTHER METHOD
    //===========================================================================================================//

    private void removeQAMessage(){

        QAMessageHelper qaMessageHelper = new QAMessageHelper(QAImageDialogActivity.this.getApplicationContext());
        qaMessageHelper.deleteQAMessage(QAImageDialogActivity.this.qaMessage);

    }

    //===========================================================================================================//
    //  UPLOAD IMAGE                                                                                UPLOAD IMAGE
    //===========================================================================================================//

    private void uploadImage(String path, String mimeType) {

        final UploadImage uploadImage = new UploadImage(path, mimeType);
        uploadImage.addListener(new UploadImage.OnUploadImageInteractionListener() {

            int oldPercentage = -1;

            @Override
            public void onRotateCompleted(String path) {}

            @Override
            public void onProgressUpdate(final int percentage) {

                if (oldPercentage + 30 < percentage || percentage == 100){
                    oldPercentage = percentage;

                    if (oldPercentage == 100){

                        QAImageDialogActivity.this.handler.post(new Runnable() {

                            @Override
                            public void run() {

                                Toast.makeText(QAImageDialogActivity.this, "Upload completed, please waiting", Toast.LENGTH_SHORT).show();

                            }

                        });

                    }
                    else{

                        QAImageDialogActivity.this.handler.post(new Runnable() {

                            @Override
                            public void run() {

                                Toast.makeText(QAImageDialogActivity.this, "Upload " + oldPercentage + "%", Toast.LENGTH_SHORT).show();

                            }

                        });

                    }

                }

            }

            @Override
            public void onCanceled() {

            }

            @Override
            public void onError(Throwable e) {

                QAImageDialogActivity.this.handler.post(new Runnable() {

                    @Override
                    public void run() {

                        Toast.makeText(QAImageDialogActivity.this, "Upload failed", Toast.LENGTH_SHORT).show();

                        QAImageDialogActivity.this.alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);

                    }

                });

            }

            @Override
            public void onCompleted(String result) {

                new AsyncTask<String, Void, String[]>() {

                    @Override
                    protected String[] doInBackground(String... params) {
                        try {
                            String[] ret = new String[3];
                            JSONObject jsonFromServer = new JSONObject(params[0]).getJSONObject(new File(uploadImage.getFilename()).getName());
                            ret[0] = jsonFromServer.getString(ImageMessage.KEY_FILE_URL);
                            ret[1] = jsonFromServer.getString(ImageMessage.KEY_THUMBNAIL_URL);
                            return ret;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(String[] s) {
                        super.onPostExecute(s);

                        if(s != null) {

                            String answerID = QAImageDialogActivity.this.answerIdArray[0];

                            JSONObject jsonObject = new JSONObject();
                            JSONObject responseJsonObject = new JSONObject();
                            JSONObject questionJsonObject = new JSONObject();
                            JSONObject answerJsonObject = new JSONObject();

                            try {

                                jsonObject.put("message-type", "query");
                                jsonObject.put("primitive", "response");
                                jsonObject.put("media", "image");

                                questionJsonObject.put("id", QAImageDialogActivity.this.questionID);
                                questionJsonObject.put("str", QAImageDialogActivity.this.questionStr);

                                answerJsonObject.put("id", answerID);
                                answerJsonObject.put("thumbnail_url", s[1]);
                                answerJsonObject.put("image_url", s[0]);

                                responseJsonObject.put("question", questionJsonObject);
                                responseJsonObject.put("answer", answerJsonObject);

                                jsonObject.put("response", responseJsonObject);

                                ChatMessage chatMessage = new ChatMessage.Builder().setChatID(QAImageDialogActivity.this.msgFrom).setBody(jsonObject.toString()).setFromMe(true).setTimestamp(DateTimeUtil.getCurrentTimeStamp()).build();

                                if (chatMessage != null) {

                                    QAImageDialogActivity.this.xmppManager.sendQAMessage(chatMessage);

                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }

                        Toast.makeText(QAImageDialogActivity.this, "Send completed", Toast.LENGTH_SHORT).show();

                        QAImageDialogActivity.this.removeQAMessage();

                        QAImageDialogActivity.super.finish();
                        QAImageDialogActivity.this.alertDialog.dismiss();
                        QAImageDialogActivity.this.alertDialog = null;

                    }
                }.execute(result);
            }
        });

        new MediaUploadManager(QAImageDialogActivity.this.getApplicationContext(), uploadImage).upload(QAImageDialogActivity.this.userJID, QAImageDialogActivity.this.msgFrom);

    }

    //===========================================================================================================//
    //  NOTE                                                                                        NOTE
    //===========================================================================================================//


}
