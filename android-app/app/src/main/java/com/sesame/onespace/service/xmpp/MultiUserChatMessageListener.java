package com.sesame.onespace.service.xmpp;

import android.content.Context;
import android.os.AsyncTask;

import com.sesame.onespace.managers.SettingsManager;
import com.sesame.onespace.models.chat.Chat;
import com.sesame.onespace.models.chat.ChatMessage;
import com.sesame.onespace.service.MessageService;
import com.sesame.onespace.utils.DateTimeUtil;
import com.sesame.onespace.utils.Log;

import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by chongos on 10/28/15 AD.
 */
public class MultiUserChatMessageListener implements MessageListener {

    private Context mContext;
    private Chat chat;
    private MultiUserChat multiUserChat;
    private SettingsManager mSettings;

    public MultiUserChatMessageListener(Context context, Chat chat, MultiUserChat multiUserChat) {
        this.mContext = context;
        this.chat = chat;
        this.multiUserChat = multiUserChat;
        this.mSettings = SettingsManager.getSettingsManager(context);
    }

    @Override
    public void processMessage(Message message) {
        final String msgFrom = message.getFrom();

        try {
            String[] msgFromSplited = msgFrom.split("/");
            final String msgFromRoomJID = msgFromSplited[0];
            final String msgFromUser = msgFromSplited[1];

            final String msgBody = message.getBody();
            if (msgFromRoomJID.equals(chat.getId())
                    && !msgFromUser.equals(mSettings.getUserAccountManager().getUsername())) {
                Log.i("MultiUserChatMessage received from " + msgFrom + " ,body : " + message.getBody());
                new AsyncTask<Void, Void, Object>() {

                    @Override
                    protected Object doInBackground(Void... params) {
                        try {
                            JSONObject jsonObject = new JSONObject(msgBody);
                            String messageType = jsonObject.getString(ChatMessage.KEY_MESSAGE_TYPE);

                            if (messageType.equals("chat")) {
                                return new ChatMessage.Builder()
                                        .setChatID(msgFrom.split("/")[0])
                                        .setFromJID(msgFrom)
                                        .setBody(msgBody)
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
                        if (obj instanceof ChatMessage)
                            Tools.sendToService(mContext, MessageService.ACTION_XMPP_MESSAGE_RECEIVED, chat, (ChatMessage) obj);
                    }
                }.execute();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
