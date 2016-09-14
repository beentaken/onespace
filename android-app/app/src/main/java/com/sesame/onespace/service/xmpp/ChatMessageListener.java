package com.sesame.onespace.service.xmpp;

import android.content.Context;
import android.os.AsyncTask;

import com.sesame.onespace.models.chat.ChatMessage;
import com.sesame.onespace.service.MessageService;
import com.sesame.onespace.utils.DateTimeUtil;
import com.sesame.onespace.utils.Log;

import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.packet.Message;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by chongos on 10/22/15 AD.
 */
public class ChatMessageListener implements org.jivesoftware.smack.chat.ChatMessageListener {

    private Context mContext;

    public ChatMessageListener(Context context) {
        this.mContext = context;
    }

    @Override
    public void processMessage(Chat chat, Message message) {
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
