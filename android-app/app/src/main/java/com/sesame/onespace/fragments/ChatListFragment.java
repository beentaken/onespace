package com.sesame.onespace.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sesame.onespace.R;
import com.sesame.onespace.managers.chat.ChatHistoryManager;
import com.sesame.onespace.models.chat.Chat;
import com.sesame.onespace.models.chat.LoadMoreMessageProgress;
import com.sesame.onespace.utils.DateTimeUtil;
import com.sesame.onespace.views.ConncetionStatusView;
import com.sesame.onespace.views.DividerItemDecoration;
import com.sesame.onespace.views.EndlessRecyclerOnScrollListener;
import com.sesame.onespace.views.adapters.ChatListArrayAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chongos on 9/15/15 AD.
 */
public class ChatListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private static final String KEY_CONNCECTION_STATUS = "connection_status";
    private static final int DISPLAY_LIMIT = 20;

    private ChatListFragment.OnChatListInteractionListener mListener;
    private BroadcastReceiver mReceiver;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ConncetionStatusView mConnectionStatusView;
    private ChatListArrayAdapter adapter;
    private LoadMoreChat loadMoreChat;
    private int connectionStatus;

    public ChatListFragment() {

    }

    public static ChatListFragment newInstance(int connStatus) {
        ChatListFragment fragment = new ChatListFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(KEY_CONNCECTION_STATUS, connStatus);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if(bundle != null) {
            connectionStatus = bundle.getInt(KEY_CONNCECTION_STATUS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);

        mConnectionStatusView = new ConncetionStatusView(getContext(), (TextView) view.findViewById(R.id.connection_status_layout)
                .findViewById(R.id.connection_status_textview));

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity().getBaseContext());
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int currentPage) {
                loadMoreChat.load();
            }
        });

        adapter = new ChatListArrayAdapter(getContext());
        adapter.setEmptyStateView(view.findViewById(R.id.empty_chat_state_view));
        adapter.setOnChatListInteractionListener(mListener);
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), 86, 10));

        loadMoreChat = new LoadMoreChat();
        loadMoreChat.load();

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(connectionStatus != 3)
            setConncetionStatus(connectionStatus);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (ChatListFragment.OnChatListInteractionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnChatListInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onRefresh() {
        new AsyncTask<Void, Void, List<Chat>>() {

            @Override
            protected List<Chat> doInBackground(Void... params) {
                return ChatHistoryManager.getInstance(getContext())
                        .getChats(DateTimeUtil.getCurrentTimeStamp(), DISPLAY_LIMIT);
            }

            @Override
            protected void onPostExecute(List<Chat> chats) {
                super.onPostExecute(chats);
                adapter.setItem(chats);
                swipeRefreshLayout.setRefreshing(false);
                if(chats.size() > 0)
                    loadMoreChat.setTimestamp(chats.get(chats.size()-1).getTimestamp());
            }

        }.execute();
    }

    private void updateChatList(final String chatID) {
        new AsyncTask<Void, Void, Chat>() {

            @Override
            protected Chat doInBackground(Void... params) {
                return ChatHistoryManager.getInstance(getContext()).getChat(chatID);
            }

            @Override
            protected void onPostExecute(Chat chat) {
                super.onPostExecute(chat);
                updateChat(chat);
            }

        }.execute();
    }

    public void updateChat(Chat chat) {
        if(adapter != null)
            adapter.updateChat(chat);
    }

    public void removeChat(Chat chat) {
        if(adapter != null)
            adapter.removeItem(chat);
    }

    public void setConncetionStatus(int status) {
        connectionStatus = status;
        if(mConnectionStatusView != null)
            mConnectionStatusView.setStatus(status);
    }

    private class LoadMoreChat {

        private LoadMoreMessageProgress loadMoreMessageProgress;
        private String timestamp;

        public LoadMoreChat() {
            timestamp = DateTimeUtil.getCurrentTimeStamp();
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }

        public void load() {
            if (loadMoreMessageProgress == null) {
                loadMoreMessageProgress = new LoadMoreMessageProgress(listener);
                adapter.addItem(loadMoreMessageProgress);
            }
        }

        private LoadMoreMessageProgress.Listener listener = new LoadMoreMessageProgress.Listener() {

            private AsyncTask<Void, Void, List<Object>> task;

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onCompleted(List<Object> result) {
                loadMoreMessageProgress = null;
                swipeRefreshLayout.setEnabled(true);
            }

            @Override
            public void onLoad() {
                if (task == null || task.getStatus() == AsyncTask.Status.FINISHED)
                    task = initialTask();

                if (task.getStatus() == AsyncTask.Status.PENDING) {
                    swipeRefreshLayout.setEnabled(false);
                    task.execute();
                }
            }
        };

        private AsyncTask<Void, Void, List<Object>> initialTask() {
            return new AsyncTask<Void, Void, List<Object>>() {

                @Override
                protected List<Object> doInBackground(Void... params) {
                    ArrayList<Object> res = new ArrayList<>();
                    res.addAll(ChatHistoryManager.getInstance(getContext()).getChats(timestamp, DISPLAY_LIMIT));
                    return res;
                }

                @Override
                protected void onPostExecute(List<Object> chat) {
                    super.onPostExecute(chat);
                    loadMoreMessageProgress.completed(chat);
                    if (chat.size() > 0)
                        setTimestamp(((Chat) chat.get(chat.size() - 1)).getTimestamp());
                }

            };
        }
    }

    public interface OnChatListInteractionListener {
        void onOpenChat(Chat chat);
        void onRemoveChat(Chat chat);
    }

}
