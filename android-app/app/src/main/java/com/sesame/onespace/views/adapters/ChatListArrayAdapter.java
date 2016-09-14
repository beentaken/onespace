package com.sesame.onespace.views.adapters;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sesame.onespace.R;
import com.sesame.onespace.fragments.ChatListFragment;
import com.sesame.onespace.models.chat.Chat;
import com.sesame.onespace.models.chat.LoadMoreMessageProgress;
import com.sesame.onespace.utils.DateTimeUtil;
import com.sesame.onespace.utils.DrawableUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by chongos on 9/10/15 AD.
 */
public class ChatListArrayAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_CHAT = 0;
    private static final int TYPE_LOAD_MORE = -1;

    private List<Object> chats;
    private Context mContext;
    private ChatListFragment.OnChatListInteractionListener mListener;
    private View mEmptyStateView;

    public ChatListArrayAdapter(final Context context) {
        mContext = context;
        chats = new ArrayList<>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case TYPE_LOAD_MORE:
                return new LoadMoreViewHolder(
                        inflater.inflate(R.layout.row_message_load_more, parent, false));
            default:
                return new ChatViewHolder(
                        inflater.inflate(R.layout.row_chat_item, parent, false));
        }
    }

    @Override
    public int getItemViewType(int position) {
        try {
            Object chat = chats.get(position);
            if (chat instanceof Chat)
                return TYPE_CHAT;
            else if (chat instanceof LoadMoreMessageProgress)
                return TYPE_LOAD_MORE;
        } catch (IndexOutOfBoundsException e) {
            return TYPE_LOAD_MORE;
        }
        return super.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        int chatListSize = chats.size();
        displayEmptyStateView(chatListSize <= 0);
        return chatListSize;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        int type = viewHolder.getItemViewType();
        if (type == TYPE_LOAD_MORE)
            configureLoadMoreViewHolder(viewHolder, position);
        else if (type == TYPE_CHAT)
            configureChatViewHolder(viewHolder, position);
    }

    private void displayPopup(final Chat chat) {
        final Dialog dialog = new Dialog(mContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_action_chatlist);
        dialog.setCancelable(true);
        ((TextView) dialog.findViewById(R.id.popup_chatlist_chat_name)).setText(chat.getName());
        dialog.findViewById(R.id.popup_chatlist_open_chat).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onOpenChat(chat);
                dialog.cancel();
            }
        });
        dialog.findViewById(R.id.popup_chatlist_remove_chat).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onRemoveChat(chat);
                dialog.cancel();
            }
        });
        dialog.show();
    }


    private void configureChatViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        final Chat chat = (Chat) chats.get(position);
        boolean hasUnreadMessage = chat.getUnreadMessageCount() > 0;
        ChatViewHolder chatViewHolder = (ChatViewHolder) viewHolder;
        chatViewHolder.username.setText(chat.getName());
        chatViewHolder.snippet.setText(chat.getSnippet());
        chatViewHolder.dateTime.setText(DateTimeUtil.getShortDateTimeByTimeStamp(chat.getTimestamp()));
        chatViewHolder.dateTime.setTextColor(hasUnreadMessage
                ? mContext.getResources().getColor(R.color.green_A700)
                : mContext.getResources().getColor(R.color.grey_700));
        chatViewHolder.unreadCount.setText(String.valueOf(chat.getUnreadMessageCount()));
        chatViewHolder.unreadCount.setVisibility(hasUnreadMessage ? View.VISIBLE : View.INVISIBLE);
        chatViewHolder.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onOpenChat(chat);
            }
        });
        chatViewHolder.rootView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                displayPopup(chat);
                return false;
            }
        });

        if(chat.getType() == Chat.Type.GROUP)
            chatViewHolder.avatar.setImageResource(R.drawable.ic_group_rounded);
        else
            chatViewHolder.avatar.setImageDrawable(DrawableUtil.getTextDrawable(mContext, chat.getName()));
    }

    private void configureLoadMoreViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        final LoadMoreViewHolder loadMoreViewHolder = (LoadMoreViewHolder) viewHolder;
        final LoadMoreMessageProgress loadMoreMessageProgress = (LoadMoreMessageProgress) chats.get(position);

        loadMoreViewHolder.mButton.setOnClickListener(loadMoreMessageProgress);
        loadMoreMessageProgress.addListener(new LoadMoreMessageProgress.Listener() {
            @Override
            public void onCompleted(List<Object> result) {
                chats.remove(loadMoreMessageProgress);
                addAll(result);
            }

            @Override
            public void onError(Throwable t) {
                loadMoreViewHolder.mButton.setVisibility(View.VISIBLE);
                loadMoreViewHolder.mProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onLoad() {
                loadMoreViewHolder.mButton.setVisibility(View.GONE);
                loadMoreViewHolder.mProgressBar.setVisibility(View.VISIBLE);
                loadMoreViewHolder.mProgressBar.setIndeterminate(true);
            }
        });
        loadMoreMessageProgress.load();
    }

    private void displayEmptyStateView(boolean display) {
        if(mEmptyStateView != null)
            mEmptyStateView.setVisibility(display ? View.VISIBLE : View.GONE);
    }

    public void setEmptyStateView(View view) {
        this.mEmptyStateView = view;
    }

    public void setOnChatListInteractionListener(ChatListFragment.OnChatListInteractionListener listener) {
        mListener = listener;
    }

    public void setItem(List<Chat> chats) {
        this.chats.clear();
        this.chats.addAll(chats);
        notifyDataSetChanged();
    }

    public void addItem(int position, Chat chat) {
        chats.add(position, chat);
        notifyDataSetChanged();
    }

    public void addItem(Object obj) {
        chats.add(obj);
        notifyDataSetChanged();
    }

    public void addAll(List<Object> objs) {
        chats.addAll(objs);
        notifyDataSetChanged();
    }

    public void removeItem(final Object obj) {
        new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... params) {
                int position = indexOf(obj);
                if(position < 0)
                    return false;
                chats.remove(position);
                return true;
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
                if(aBoolean)
                    notifyDataSetChanged();
            }
        }.execute();
    }

    public void updateChat(final Chat chat) {
        new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... params) {
                int position = indexOf(chat);
                if(position >= 0)
                    chats.remove(position);
                chats.add(chat);
                Collections.sort(chats, new Comparator<Object>() {
                    @Override
                    public int compare(Object obj1, Object obj2) {
                        if (obj1 instanceof LoadMoreMessageProgress)
                            return 1;
                        else if (obj2 instanceof LoadMoreMessageProgress)
                            return -1;
                        try {
                            return ((Chat) obj2).getTimestamp().compareTo(((Chat) obj1).getTimestamp());
                        } catch(Exception e) {
                            return 0;
                        }
                    }
                });
                return true;
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
                if(aBoolean)
                    notifyDataSetChanged();
            }
        }.execute();
    }

    private int indexOf(Object obj) {
        if(obj instanceof LoadMoreMessageProgress)
            return chats.indexOf(obj);

        if(obj instanceof Chat) {
            for (int i = 0; i < chats.size(); i++) {
                if (((Chat) chats.get(i)).getId().equals(((Chat) obj).getId())) {
                    return i;
                }
            }
        }
        return -1;
    }

    public static class LoadMoreViewHolder extends RecyclerView.ViewHolder {

        public ProgressBar mProgressBar;
        public Button mButton;

        public LoadMoreViewHolder(View view) {
            super(view);
            mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
            mButton = (Button) view.findViewById(R.id.btn_loadmore);
        }
    }


    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        CardView rootView;
        TextView username;
        TextView snippet;
        TextView dateTime;
        TextView unreadCount;
        ImageView avatar;

        ChatViewHolder(View itemView) {
            super(itemView);
            rootView = (CardView)itemView.findViewById(R.id.root_view);
            username = (TextView)itemView.findViewById(R.id.title);
            snippet = (TextView)itemView.findViewById(R.id.snippet);
            dateTime = (TextView)itemView.findViewById(R.id.datetime);
            unreadCount = (TextView)itemView.findViewById(R.id.unread_count);
            avatar = (ImageView)itemView.findViewById(R.id.icon);
        }
    }

}