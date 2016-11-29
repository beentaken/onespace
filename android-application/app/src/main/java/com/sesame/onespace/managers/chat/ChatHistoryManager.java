package com.sesame.onespace.managers.chat;

import android.content.Context;

import com.sesame.onespace.constant.Constant;
import com.sesame.onespace.databases.ChatHelper;
import com.sesame.onespace.databases.MessagesHelper;
import com.sesame.onespace.managers.SettingsManager;
import com.sesame.onespace.models.chat.Chat;
import com.sesame.onespace.models.chat.ChatMessage;
import com.sesame.onespace.models.chat.HistoryMessages;
import com.sesame.onespace.models.map.Surfer;
import com.sesame.onespace.models.map.Walker;
import com.sesame.onespace.network.OneSpaceApi;
import com.sesame.onespace.utils.DateTimeUtil;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.util.PacketParserUtils;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit.Call;
import retrofit.GsonConverterFactory;
import retrofit.Response;

/**
 * Created by chongos on 9/10/15 AD.
 */
public class ChatHistoryManager {

    private static ChatHistoryManager sInstance;
    private Context mContext;
    private SettingsManager mSettingManager;

    public static synchronized ChatHistoryManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new ChatHistoryManager(context);
        }
        return sInstance;
    }

    private ChatHistoryManager(Context context) {
        this.mContext = context;
        this.mSettingManager = SettingsManager.getSettingsManager(context);
    }

    public Chat createPrivateChat(ChatMessage chatMessage) {
        if(containChat(chatMessage.getChatID()))
            return getChat(chatMessage.getChatID());
        Chat chat = new Chat.Builder()
                .setChatID(chatMessage.getChatID())
                .setChatName(chatMessage.getChatID().split("@")[0])
                .setTimestamp(chatMessage.getTimestamp())
                .setType(Chat.Type.PRIVATE)
                .build();
        return addChat(chat) ? chat : null;
    }

    public Chat createPrivateChat(Walker walker) {
        if(containChat(walker.getJid()))
            return getChat(walker.getJid());
        Chat chat = new Chat.Builder()
                .setChatID(walker.getJid())
                .setChatName(walker.getUserName())
                .setTimestamp(DateTimeUtil.getCurrentTimeStamp())
                .setType(Chat.Type.PRIVATE)
                .build();
        return addChat(chat) ? chat : null;
    }

    public Chat createPrivateChat(Surfer surfer) {
        if(containChat(surfer.getJid()))
            return getChat(surfer.getJid());
        Chat chat = new Chat.Builder()
                .setChatID(surfer.getJid())
                .setChatName(surfer.getUserName())
                .setTimestamp(DateTimeUtil.getCurrentTimeStamp())
                .setType(Chat.Type.PRIVATE)
                .build();
        return addChat(chat) ? chat : null;
    }

    public Chat createGroupChat(String id, String name) {
        if(containChat(id))
            return getChat(id);
        Chat chat = new Chat.Builder()
                .setChatID(id)
                .setChatName(name)
                .setType(Chat.Type.GROUP)
                .setTimestamp(DateTimeUtil.getCurrentTimeStamp())
                .build();
        return addChat(chat) ? chat : null;
    }

    public boolean addChat(Chat chat) {
        ChatHelper chatHelper = ChatHelper.getInstance(mContext);
        if(containChat(chat.getId()))
            return true;
        return chatHelper.addChat(chat);
    }


    public long addMessage(ChatMessage msg) {
        if(!containChat(msg.getChatID()))
            createPrivateChat(msg);
        return insertMessage(msg);
    }

    private long insertMessage(ChatMessage msg) {
        long id = MessagesHelper.getInstance(mContext).addMessage(msg);
        if(ChatHelper.getInstance(mContext).updateChat(id, msg))
            return id;
        return -1;
    }

    public void markReadAllMessage(String chatID) {
        ChatHelper.getInstance(mContext).setUnreadMessageCount(chatID, 0);
    }

    public Chat getChat(String id) {
        Chat chat = ChatHelper.getInstance(mContext).getChat(id);
        try {
            chat.setLastMessage(MessagesHelper.getInstance(mContext).getMessage(chat.getMessageID()));
        } catch (NullPointerException e) { }
        return chat;
    }

    public boolean containChat(String id) {
        return ChatHelper.getInstance(mContext).containChat(id);
    }

    public List<Chat> getChats(String timestamp, int limit) {
        List<Chat> retChats = ChatHelper.getInstance(mContext).getChats(timestamp, limit);
        for(Chat chat : retChats) {
            chat.setLastMessage(MessagesHelper.getInstance(mContext).getMessage(chat.getMessageID()));
        }
        return retChats;
    }

    public List<ChatMessage> getMessages(String chatID, String timestamp, int limit)
            throws XmlPullParserException, IOException, SmackException {
        List<ChatMessage> resMessages = getMessagesFromLocal(chatID, timestamp, limit);
        if(resMessages.size() > 0)
            return resMessages;

        String fromID = mSettingManager.getUserAccountManager().getUsername()
                + "@" + mSettingManager.xmppServer;
        int nowSize = resMessages.size();
        resMessages.addAll(getMessagesFromServer(fromID, chatID,
                nowSize > 0 ? resMessages.get(nowSize - 1).getTimestamp() : timestamp,
                limit - nowSize));

        Collections.sort(resMessages);
        return resMessages;
    }

    private List<ChatMessage> getMessagesFromLocal(String chatID, String timestamp, int limit) {
        List<ChatMessage> resMessages = new ArrayList<>();
        resMessages.addAll(MessagesHelper.getInstance(mContext).getMessages(chatID,
                resMessages.size() > 0
                        ? resMessages.get(resMessages.size()-1).getTimestamp() : timestamp,
                limit - resMessages.size()));
        Collections.sort(resMessages);
        return resMessages;
    }

    private List<ChatMessage> getMessagesFromServer(String fromID, String toID,
                                                    String lastSentDate, int limit)
            throws IOException, XmlPullParserException, SmackException {
        List<ChatMessage> messages = new ArrayList<>();
        Call<HistoryMessages> call = new OneSpaceApi.Builder(mContext)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .messagesHistory(fromID,
                        mSettingManager.xmppRecource,
                        toID,
                        mSettingManager.xmppRecource,
                        lastSentDate+"000",
                        limit);

        Response<HistoryMessages> response = call.execute();
        HistoryMessages historyMessages = response.body();

        for(HistoryMessages.Message message : historyMessages.getMessages()) {
            Message msgStanza = PacketParserUtils.parseMessage(
                    PacketParserUtils.getParserFor(message.getStanza()));

            boolean fromMe = message.getSource().equals("me");
            messages.add(new ChatMessage.Builder()
                    .setChatID(toID)
                    .setBody(msgStanza.getBody())
                    .setFromJID(fromMe ? fromID : toID)
                    .setFromMe(fromMe)
                    .setNeedPush(false)
                    .setTimestamp(String.valueOf(message.getSentDate()))
                    .build());
        }

        return messages;
    }

    public boolean deleteMessages(String chatID, String lastSentDate) {
        return MessagesHelper.getInstance(mContext).deleteMessages(chatID, lastSentDate);
    }

    public boolean deleteOldMessages(String chatID) {
        if(mSettingManager.chatHistoryMessageLimit < 0)
            return true;
        List<ChatMessage> messages = getMessagesFromLocal(chatID, DateTimeUtil.getCurrentTimeStamp(),
                mSettingManager.chatHistoryMessageLimit);

        for(ChatMessage msg : messages) {
            if(msg.needPush())
                return true;
        }

        int messageHistoryLimit = SettingsManager.getSettingsManager(mContext).chatHistoryMessageLimit;
        if(messages.size() == messageHistoryLimit) {
            String timestamp = messages.get(messageHistoryLimit-1).getTimestamp();
            return deleteMessages(chatID, timestamp);
        }
        return false;
    }

    public boolean deleteChat(Chat chat) {
        return ChatHelper.getInstance(mContext).deleteChat(chat.getId())
                && MessagesHelper.getInstance(mContext).deleteMessages(chat.getId());
    }

}
