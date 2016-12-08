package com.sesame.onespace.service.xmpp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.support.v7.app.NotificationCompat;

import com.sesame.onespace.R;
import com.sesame.onespace.activities.dialogActivity.DialogActivity;
import com.sesame.onespace.models.chat.ChatMessage;
import com.sesame.onespace.service.MessageService;
import com.sesame.onespace.utils.DateTimeUtil;
import com.sesame.onespace.utils.Log;

import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.packet.Message;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by chongos on 10/22/15 AD.
 */
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

                            //Thianchai (I add this)

                            Intent dialogIntent = new Intent(mContext, DialogActivity.class);
                            dialogIntent.putExtra("idQuestion", jsonObject.getJSONObject("question").get("id") + "");
                            dialogIntent.putExtra("question", jsonObject.getJSONObject("question").get("str") + "");

                            JSONArray jsonArray = jsonObject.getJSONArray("answers");

                            int index = 0;
                            ArrayList<String> answerArrayID = new ArrayList<String>();

                            while(index < jsonArray.length()){

                                answerArrayID.add((String) ((JSONObject) jsonArray.get(index)).get("id"));

                                index = index + 1;

                            }

                            index = 0;
                            ArrayList<String> answerArrayStr = new ArrayList<String>();

                            while(index < jsonArray.length()){

                                answerArrayStr.add((String) ((JSONObject) jsonArray.get(index)).get("str"));

                                index = index + 1;

                            }

                            dialogIntent.putStringArrayListExtra("answerArrayID", answerArrayID);
                            dialogIntent.putStringArrayListExtra("answerArrayStr", answerArrayStr);
                            dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            dialogIntent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

                            PendingIntent pi = PendingIntent.getActivity(mContext, 0, dialogIntent, PendingIntent.FLAG_ONE_SHOT);
                            Resources r = mContext.getResources();
                            Notification notification = new NotificationCompat.Builder(mContext)
                                    .setTicker("test")
                                    .setSmallIcon(R.mipmap.ic_launcher)
                                    .setContentTitle("Query")
                                    .setContentText(jsonObject.getJSONObject("question").get("str") + "")
                                    .setContentIntent(pi)
                                    .setAutoCancel(true)
                                    .build();

                            NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
                            notificationManager.notify(0, notification);

                            //

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
