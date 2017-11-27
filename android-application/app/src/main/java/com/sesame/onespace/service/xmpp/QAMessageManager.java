package com.sesame.onespace.service.xmpp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.app.NotificationCompat;

import com.sesame.onespace.R;
import com.sesame.onespace.activities.dialogActivities.QAChoiceDialogActivity;
import com.sesame.onespace.activities.dialogActivities.QAImageDialogActivity;
import com.sesame.onespace.databases.qaMessageDatabases.QAMessageHelper;
import com.sesame.onespace.fragments.MainMenuFragment;
import com.sesame.onespace.managers.SettingsManager;
import com.sesame.onespace.managers.service.OnespaceNotificationManager;
import com.sesame.onespace.models.qaMessage.QAMessage;
import com.sesame.onespace.network.OneSpaceApi;
import com.sesame.onespace.utils.Log;
import com.sesame.onespace.utils.date.DateConvert;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import retrofit.GsonConverterFactory;
import retrofit.RxJavaCallAdapterFactory;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by christian on 8/2/17.
 */

public class QAMessageManager {

    private static QAMessageManager instance;

    private Context mContext;
    private SettingsManager mSettingsManager;

    private QAMessageManager(Context context) {
        this.mContext = context;
        this.mSettingsManager = SettingsManager.getSettingsManager(context);
    }


    public static QAMessageManager getInstance(Context context) {
        if(instance == null)
            instance = new QAMessageManager(context);
        return instance;
    }


    public void addMessageBody(String msgFrom, String msgBody, String source) {
        long time = System.currentTimeMillis() / 1000L;
        this.sendReport(time ,"[QAMessageManager.addMessageBody()] Function called with msgFrom="+msgFrom+", source="+source);

        QAMessage qaMessage = this.createQAMessage(msgFrom, msgBody);

        if (qaMessage == null) {
            Log.i("QAMessageManager.addMessageBody(): qaMessage is null.");
            return;
        }

        this.sendReport(time, "[QAMessageManager.addMessageBody()] QAMessage created: " + qaMessage.getQuestionStr());

        QAMessageHelper qaMessageHelper = new QAMessageHelper(this.mContext);
        long rowId = qaMessageHelper.addQAMessage(qaMessage);

        this.sendReport(time, "[QAMessageManager.addMessageBody()] QAMessage added to DB ("+rowId+"): " + qaMessage.getQuestionStr());

        if (rowId > 0) {
            qaMessage.setId((int)rowId);
            this.displayNotification(qaMessage, msgFrom, source);
        }
    }



    public void displayNotification(QAMessage qaMessage, String msgFrom, String source){

        if (ChatMessageListener.isAppOnForeground(mContext) == false || MainMenuFragment.getbFocusQA() == false) {

            Intent dialogIntent;

            if (qaMessage.getType().equals("text")) {
                dialogIntent = new Intent(mContext, QAChoiceDialogActivity.class);
            } else if (qaMessage.getType().equals("image")) {
                dialogIntent = new Intent(mContext, QAImageDialogActivity.class);
            } else {
                return;
            }

            dialogIntent.putExtra("id", qaMessage.getId());
            dialogIntent.putExtra("msgFrom", qaMessage.getMsgFrom());
            dialogIntent.putExtra("type", qaMessage.getType());
            dialogIntent.putExtra("questionId", qaMessage.getQuestionID());
            dialogIntent.putExtra("questionStr", qaMessage.getQuestionStr());
            dialogIntent.putStringArrayListExtra("answerIdList", qaMessage.getAnswerIDList());
            dialogIntent.putStringArrayListExtra("answerStrList", qaMessage.getAnswerStrList());
            dialogIntent.putExtra("date", qaMessage.getDate());
            dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            dialogIntent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            dialogIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            PendingIntent pi = PendingIntent.getActivity(mContext, qaMessage.getId(), dialogIntent, PendingIntent.FLAG_ONE_SHOT);
            Notification notification = new NotificationCompat.Builder(mContext)
                    .setSmallIcon(R.drawable.ic_app_notification)
                    .setContentTitle("[" + source +"] " + msgFrom.split("@")[0] + " asked:")
                    .setContentText(qaMessage.getQuestionStr())
                    .setContentIntent(pi)
                    .setAutoCancel(true)
                    .setGroup("ONESPACE_NOTIFICATION_GROUP_KEY__QA_MESSAGE")
                    .build();

            if (mSettingsManager.notificationSound)
                notification.sound = Uri.parse(mSettingsManager.notificationRingtone);

            if (mSettingsManager.notificationVibrate)
                notification.vibrate = new long[]{25, 100, 100, 200};

            if (mSettingsManager.notificationLED) {
                notification.ledOnMS = 1000;
                notification.ledOffMS = 2000;
                notification.ledARGB = Color.MAGENTA;
                notification.flags |= Notification.FLAG_SHOW_LIGHTS;
            }

            NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            Integer notificationId = qaMessage.getId();

            notificationManager.notify(notificationId, notification);

            OnespaceNotificationManager.getSettingsManager(mContext).addNotification(notificationId, notification);

        }


    }




    public QAMessage createQAMessage(String msgFrom, String msgBody) {
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(msgBody);
        } catch (JSONException e) {
            Log.d("Could not concert message body to JSON");
            return null;
        }

        try {
            String mediaType = jsonObject.get("media") + "";
            String questionId = jsonObject.getJSONObject("question").get("id") + "";
            String questionStr = jsonObject.getJSONObject("question").get("str") + "";
            ArrayList<String> answerIdList = new ArrayList<String>();
            ArrayList<String> answerStrList = new ArrayList<String>();

            Long tsLong = System.currentTimeMillis() / 1000;
            String ts = tsLong.toString();
            String date = DateConvert.convertTimeStampToDate(ts, DateConvert.DATE_FORMAT2);

            JSONArray jsonArray = jsonObject.getJSONArray("answers");

            for (int i = 0; i < jsonArray.length(); i++)
                answerIdList.add((String) ((JSONObject) jsonArray.get(i)).get("id"));

            for (int i = 0; i < jsonArray.length(); i++)
                answerStrList.add((String) ((JSONObject) jsonArray.get(i)).get("str"));

            return new QAMessage(msgFrom,
                                 mediaType,
                                 questionId,
                                 questionStr,
                                 answerIdList,
                                 answerStrList,
                                 date);

        } catch (JSONException e) {
            Log.d("Malformed message body (JSON)");
            return null;
        }

    }


    private void sendReport(long groupId, String report) {

        Observable<String> observable = new OneSpaceApi.Builder(mContext)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build()
                .sendReport(groupId, report);

        observable.observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                        Log.i("Report sent.");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.i("Reporting failed.");
                    }

                    @Override
                    public void onNext(String s) {

                    }
                });


    }

}
