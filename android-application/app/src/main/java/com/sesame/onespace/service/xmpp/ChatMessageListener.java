package com.sesame.onespace.service.xmpp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.NotificationCompat;

import com.sesame.onespace.R;
import com.sesame.onespace.activities.dialogActivities.QAMessageDialogActivity;
import com.sesame.onespace.databases.qaMessageDatabases.QAMessageHelper;
import com.sesame.onespace.models.chat.ChatMessage;
import com.sesame.onespace.models.qaMessage.QAMessage;
import com.sesame.onespace.service.MessageService;
import com.sesame.onespace.utils.DateTimeUtil;
import com.sesame.onespace.utils.Log;
import com.sesame.onespace.utils.date.DateConvert;

import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.packet.Message;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by chongos on 10/22/15 AD.
 */

// Modified code by Thianchai for QAMessage
    // Last Update 27/12/2016
    // 1. Show notification message
    // 2. Save Q&A message into database

public class ChatMessageListener implements org.jivesoftware.smack.chat.ChatMessageListener {

    private Context mContext;

    public ChatMessageListener(Context context) {
        this.mContext = context;
    }

    @Override
    public void processMessage(Chat chat, final Message message) {
        if (message.getType().equals(Message.Type.chat) || message.getType().equals(Message.Type.normal)) {
            if (message.getBody() != null) {
                final String msgBody = message.getBody();
                final String msgFrom = message.getFrom().split("/")[0];

                new AsyncTask<Void, Void, Object>() {

                    @Override
                    protected Object doInBackground(Void... params) {
                        try {
                            JSONObject jsonObject = new JSONObject(msgBody);
                            String messageType = jsonObject.getString(ChatMessage.KEY_MESSAGE_TYPE);

                            //----------------------------------------------------------------------
                            //Thianchai (I add this for QAMessage)

                            String questionId = jsonObject.getJSONObject("question").get("id") + "";
                            String questionStr = jsonObject.getJSONObject("question").get("str") + "";
                            ArrayList<String> answerIdList = new ArrayList<String>();
                            ArrayList<String> answerStrList = new ArrayList<String>();

                            Long tsLong = System.currentTimeMillis()/1000;
                            String ts = tsLong.toString();
                            String date = DateConvert.convertTimeStampToDate(ts, DateConvert.DATE_FORMAT1);

                            JSONArray jsonArray = jsonObject.getJSONArray("answers");

                            int index = 0;

                            while(index < jsonArray.length()){

                                answerIdList.add((String) ((JSONObject) jsonArray.get(index)).get("id"));

                                index = index + 1;

                            }

                            index = 0;

                            while(index < jsonArray.length()){

                                answerStrList.add((String) ((JSONObject) jsonArray.get(index)).get("str"));

                                index = index + 1;

                            }

                            QAMessageHelper qaMessageHelper = new QAMessageHelper(mContext);
                            qaMessageHelper.addQAMessage(new QAMessage(msgFrom ,
                                                                       questionId,
                                                                       questionStr,
                                                                       answerIdList,
                                                                       answerStrList,
                                                                       date));

                            ArrayList<QAMessage> list = qaMessageHelper.getAllQAMessages();

                            Intent dialogIntent = new Intent(mContext, QAMessageDialogActivity.class);
                            dialogIntent.putExtra("id", list.get(list.size() - 1).getId());
                            dialogIntent.putExtra("msgFrom", msgFrom);
                            dialogIntent.putExtra("questionId", questionId);
                            dialogIntent.putExtra("questionStr", questionStr);
                            dialogIntent.putStringArrayListExtra("answerIdList", answerIdList);
                            dialogIntent.putStringArrayListExtra("answerStrList", answerStrList);
                            dialogIntent.putExtra("date", date);
                            dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            dialogIntent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

                            PendingIntent pi = PendingIntent.getActivity(mContext, 0, dialogIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                            Notification notification = new NotificationCompat.Builder(mContext)
                                    .setSmallIcon(R.mipmap.ic_launcher_trim)
                                    .setContentTitle(msgFrom.split("@")[0] + " send message.")
                                    .setContentText(jsonObject.getJSONObject("question").get("str") + "")
                                    .setContentIntent(pi)
                                    .setAutoCancel(true)
                                    .build();

                            NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
                            notificationManager.notify(0, notification);

                            //----------------------------------------------------------------------

                            if(messageType.equals("chat")) {
                                return new ChatMessage.Builder()
                                        .setChatID(msgFrom)
                                        .setBody(msgBody)
                                        .setFromJID(msgFrom)
                                        .setFromMe(false)
                                        .setTimestamp(DateTimeUtil.getCurrentTimeStamp())
                                        .build();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        return null;
                    }

                    @Override
                    protected void onPostExecute(Object obj) {
                        super.onPostExecute(obj);
                        if(obj instanceof ChatMessage)
                            Tools.sendToService(mContext, MessageService.ACTION_XMPP_MESSAGE_RECEIVED, (ChatMessage) obj);
                    }
                }.execute();

            } else {
                Log.i("XMPP Packet received - but without body (body == null)");
            }
        } else {
            Log.i("XMPP Packet received - but type of packet is not chat : " + message.toString());
        }

    }

}
