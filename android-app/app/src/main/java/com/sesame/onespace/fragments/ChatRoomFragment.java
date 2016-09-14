package com.sesame.onespace.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.sesame.onespace.R;
import com.sesame.onespace.activities.SettingsActivity;
import com.sesame.onespace.managers.SettingsManager;
import com.sesame.onespace.managers.chat.ChatHistoryManager;
import com.sesame.onespace.managers.UserAccountManager;
import com.sesame.onespace.managers.chat.ChatNotificationManager;
import com.sesame.onespace.models.chat.Chat;
import com.sesame.onespace.models.chat.ChatMessage;
import com.sesame.onespace.models.chat.ImageMessage;
import com.sesame.onespace.models.chat.TextMessage;
import com.sesame.onespace.models.chat.UploadImage;
import com.sesame.onespace.managers.chat.MediaUploadManager;
import com.sesame.onespace.service.MessageService;
import com.sesame.onespace.service.xmpp.Tools;
import com.sesame.onespace.utils.DateTimeUtil;
import com.sesame.onespace.utils.DrawableUtil;
import com.sesame.onespace.utils.FilePathUtil;
import com.sesame.onespace.views.ConncetionStatusView;
import com.sesame.onespace.views.EndlessRecyclerOnScrollListener;
import com.sesame.onespace.models.chat.LoadMoreMessageProgress;
import com.sesame.onespace.views.adapters.ChatMessageArrayAdapter;

import org.jivesoftware.smack.SmackException;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.codetail.animation.SupportAnimator;
import io.codetail.animation.ViewAnimationUtils;


/**
 * A ChatRoomFragment
 * by using layout (fragment_chat_room.xml)
 *
 * Created by chongos on 9/6/15 AD.
 */
public class ChatRoomFragment extends Fragment {

    private static int GALLERY_REQUEST_CODE = 1;
    private static int CAMERA_REQUEST_CODE = 2;

    private static final String KEY_CONNCECTION_STATUS = "connection_status";
    private static final String KEY_CHAT = "chat";

    private BroadcastReceiver mReceiver;
    private OnChatFragmentInteractionListener mListener;
    private CoordinatorLayout mRootView;
    private Toolbar mToolbar;
    private EditText mSendText;
    private RecyclerView mRecyclerView;
    private ImageButton mAttachButton;
    private FloatingActionButton mSendButton;
    private CardView mRevealView;
    private View mDiscardRevealView;
    private ConncetionStatusView mConnectionStatusView;
    private ChatMessageArrayAdapter chatMessageArrayAdapter;

    private SettingsManager settingsManager;
    private LoadMessageHistory loadMessageHistory;
    private String userJID;
    private Uri captureFileUri;
    private int connectionStatus;
    private Chat chat;

    /**
     * For get new instance of ChatRoomFragment by giving two params
     * @param chat Chat object opened
     * @param connStatus XMPP Connection status
     * @return ChatRoomFragment object
     */
    public static ChatRoomFragment newInstance(Chat chat, int connStatus) {
        ChatRoomFragment fragment = new ChatRoomFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_CHAT, chat);
        bundle.putInt(KEY_CONNCECTION_STATUS, connStatus);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if(bundle != null) {
            chat = bundle.getParcelable(KEY_CHAT);
            connectionStatus = bundle.getInt(KEY_CONNCECTION_STATUS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_chat_room, container, false);
        // set this fragment has option menu
        setHasOptionsMenu(true);

        // init SettingsManager
        settingsManager = SettingsManager.getSettingsManager(getContext());

        // init All View
        initView(view);

        // get UserAccountManager from SettingsManager
        UserAccountManager userAccountManager = settingsManager.getUserAccountManager();

        // UserJID = username + @ + XMPP Server host
        userJID = userAccountManager.getUsername()
                + "@" + settingsManager.xmppServer;

        // load more history message
        loadMessageHistory = new LoadMessageHistory();
        loadMessageHistory.load();

        return view;
    }

    private void initView(View view) {
        // Casting Activity to AppCompatActivity
        AppCompatActivity activity = (AppCompatActivity) getActivity();

        // Toolbar
        mToolbar = (Toolbar) view.findViewById(R.id.toolbar);
        activity.setSupportActionBar(mToolbar);
        mToolbar.setTitle(chat.getName());
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
        if(chat.getType() == Chat.Type.PRIVATE)
            mToolbar.setLogo(DrawableUtil.getTextDrawable(getContext(), chat.getName()));
        else
            mToolbar.setLogo(R.drawable.ic_group_rounded);
        mToolbar.invalidate();

        // RootView
        mRootView = (CoordinatorLayout) view.findViewById(R.id.coordinator_layout);

        // TextField for insert sending message
        mSendText = (EditText) view.findViewById(R.id.et_message);
        mSendText.setTextSize(TypedValue.COMPLEX_UNIT_SP, settingsManager.chatFontSize);
        mSendText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                int icon = R.drawable.ic_send_grey;
                int bgColor = getResources().getColor(R.color.color_background);
                int elevation = 0;

                if (getSendMessage().length() > 0) {
                    icon = R.drawable.ic_send_white;
                    bgColor = getResources().getColor(R.color.color_primary);
                    elevation = 20;
                }
                mSendButton.setImageResource(icon);
                mSendButton.setBackgroundTintList(ColorStateList.valueOf(bgColor));
                mSendButton.setElevation(elevation);
            }
        });

        if(settingsManager.chatSendWithEnter) {
            // Set KeyListener to sending message TextField for listened enter key
            mSendText.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                        String text = getSendMessage();
                        if (text.length() > 0)
                            SendMessage(ChatMessage.Type.TEXT, text);
                        return true;
                    }
                    return false;
                }
            });
        }

        // Send Button
        mSendButton = (FloatingActionButton) view.findViewById(R.id.fab_send);
        mSendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                String text = getSendMessage();
                if (text.length() > 0)
                    SendMessage(ChatMessage.Type.TEXT, text);
            }
        });

        // Connection Status view
        mConnectionStatusView = new ConncetionStatusView(getContext(), (TextView) view.findViewById(R.id.connection_status_layout)
                .findViewById(R.id.connection_status_textview));


        // Attach image view
        View attachLayout = view.findViewById(R.id.attach_layout);
        mRevealView = (CardView) attachLayout.findViewById(R.id.reveal_attach);
        mDiscardRevealView = attachLayout.findViewById(R.id.discard_reveal_view);
        mDiscardRevealView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mRevealView.getVisibility() == View.VISIBLE)
                    showRevealView(false);
            }
        });

        // Open camera button
        attachLayout.findViewById(R.id.btn_open_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isDeviceSupportCamera() && isDeviceHasDefualtCameraApp()) {
                    openCamera();
                    showRevealView(false);
                } else {
                    Snackbar.make(mRootView, getString(R.string.error_camera_not_support), Snackbar.LENGTH_LONG).show();
                }
            }
        });

        // Open gallery button
        attachLayout.findViewById(R.id.btn_open_gallery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
                showRevealView(false);
            }
        });

        // Button for toggle attach image view
        mAttachButton = (ImageButton) view.findViewById(R.id.btn_attach);
        mAttachButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRevealView(mRevealView.getVisibility() == View.INVISIBLE);
            }
        });


        // RecyclerView for display messages
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int current_page) {
                loadMessageHistory.load();
            }
        });
        chatMessageArrayAdapter = new ChatMessageArrayAdapter(getContext(), chat, new ArrayList<>());
        chatMessageArrayAdapter.setOnChatFragmentInteractionListener(mListener);
        mRecyclerView.setAdapter(chatMessageArrayAdapter);

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // if connection status not equal to [connected state]
        if(connectionStatus != 3)
            setConncetionStatus(connectionStatus);
    }

    @Override
    public void onStart() {
        super.onStart();
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals(MessageService.ACTION_XMPP_MESSAGE_SENT)) {
                    ChatMessage message = intent.getParcelableExtra(MessageService.KEY_BUNDLE_CHAT_MESSAGE);
                    if(message.getChatID().equals(chat.getId())) {
                        // update to adapter that message sent.
                        chatMessageArrayAdapter.updateMessageSent(message);
                    }
                } else if(intent.getAction().equals(MessageService.ACTION_XMPP_PARTICIPANT_CHANGED)) {
                    String chatID = intent.getStringExtra(MessageService.KEY_BUNDLE_CHAT_ID);
                    if(chatID != null && chatID.equals(chat.getId())) {
                        String[] participants = intent.getStringArrayExtra(MessageService.KEY_BUNDLE_GROUP_PARTICIPANT);
                        int count = participants.length;
                        mToolbar.setSubtitle(count + " Participant" + (count > 1 ? "s" : ""));
                        mToolbar.invalidate();
                    }
                }
            }
        };

        // Register broadcast receiver for listened ACTION_XMPP_MESSAGE_SENT and ACTION_XMPP_PARTICIPANT_CHANGED
        IntentFilter filter = new IntentFilter();
        filter.addAction(MessageService.ACTION_XMPP_MESSAGE_SENT);
        filter.addAction(MessageService.ACTION_XMPP_PARTICIPANT_CHANGED);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mReceiver, filter);
    }

    @Override
    public void onStop() {
        // Unregister broadcast receiver when Stopped
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mReceiver);
        super.onStop();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if(chat.getType() == Chat.Type.PRIVATE)
            inflater.inflate(R.menu.menu_chat_private, menu);
        else if(chat.getType() == Chat.Type.GROUP)
            inflater.inflate(R.menu.menu_chat_group, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.leave_group:
                // Display alert dialog for confirm
                new AlertDialog.Builder(getActivity())
                        .setMessage(R.string.alert_confirm_leave_group)
                        .setPositiveButton(R.string.confirm_ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                mListener.onLeaveGroup(chat);
                                getActivity().onBackPressed();
                            }
                        })
                        .setNegativeButton(R.string.confirm_cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        })
                        .create()
                        .show();
                return true;
            case R.id.open_setting:
                Intent i = new Intent(getContext(), SettingsActivity.class);
                i.putExtra(SettingsActivity.KEY_SETTING_ITEM,
                        new SettingsListFragment.SettingItem(
                                getString(R.string.pref_header_chats),
                                R.drawable.ic_header_setting_chat,
                                SettingsActivity.FRAGMENT_CHAT));
                startActivity(i);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Set openning chat object to ChatNotificationManager for mute this chat
        ChatNotificationManager.getNotificationManager(getContext()).setOpenningChat(chat);
        if(chat.getType() == Chat.Type.GROUP)
            // request participant of group chat
            Tools.sendToService(getContext(), MessageService.ACTION_XMPP_PARTICIPANT_REQUEST, chat);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Unset openning chat object to ChatNotificationManager for mute this chat
        ChatNotificationManager.getNotificationManager(getContext()).closeChat();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (OnChatFragmentInteractionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnChatFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        // Create bg thread for delete old message and mark all message as read
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                if(chat.getType() == Chat.Type.PRIVATE)
                    ChatHistoryManager.getInstance(getContext()).deleteOldMessages(chat.getId());
                ChatHistoryManager.getInstance(getContext()).markReadAllMessage(chat.getId());
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                mListener.onChatUpdated(chat.getId());
                mListener = null;
            }

        }.execute();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if(resultCode == Activity.RESULT_OK) {
                String selectedImagePath = null;
                // Activity result from gallery
                if (requestCode == GALLERY_REQUEST_CODE && null != data) {
                    Uri selectedImageUri = data.getData();
                    selectedImageUri.getPath();
                    selectedImagePath = FilePathUtil.getPath(getContext(), selectedImageUri);
                }
                // Activity result from camera
                else if(requestCode == CAMERA_REQUEST_CODE) {
                    selectedImagePath = captureFileUri.getPath();
                }

                String mimeType = "image/*";
                if(selectedImagePath != null)
                    uploadImage(selectedImagePath, mimeType);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get Text in send message field
     * @return string of text message
     */
    public String getSendMessage() {
        return mSendText.getText().toString().trim();
    }

    /**
     * For clear text in send message field
     */
    public void clearSendMessage() {
        mSendText.setText("");
    }

    /**
     * Add message object to adapter
     * @param message ChatMessage object
     */
    public void addMessage(Object message) {
        chatMessageArrayAdapter.addItem(message);
        mRecyclerView.scrollToPosition(0);
    }

    /**
     * Set XMPP connection status
     * @param status XMPP Connection state
     */
    public void setConncetionStatus(int status) {
        connectionStatus = status;
        if(mConnectionStatusView != null)
            mConnectionStatusView.setStatus(status);
    }

    /**
     * Display attach image view
     * @param show
     */
    private void showRevealView(boolean show) {
        int cx = (mRevealView.getLeft() + mRevealView.getRight()) / 2;
        int cy = mRevealView.getTop();
        int radius = Math.max(mRevealView.getWidth(), mRevealView.getHeight());

        final SupportAnimator animator =
                ViewAnimationUtils.createCircularReveal(mRevealView, cx, cy, 0, radius);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(400);
        final SupportAnimator animatorReverse = animator.reverse();

        if (show) {
            mDiscardRevealView.setVisibility(View.VISIBLE);
            mRevealView.setVisibility(View.VISIBLE);
            animator.start();
        } else {
            animatorReverse.addListener(new SupportAnimator.AnimatorListener() {

                @Override
                public void onAnimationEnd() {
                    mRevealView.setVisibility(View.INVISIBLE);
                    mDiscardRevealView.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationStart() {
                }

                @Override
                public void onAnimationCancel() {
                }

                @Override
                public void onAnimationRepeat() {
                }
            });
            animatorReverse.start();
        }
    }

    /**
     * For display upload fail dialog
     * @param uploadImage UploadImage object
     */
    private void showUploadFailDialog(final UploadImage uploadImage) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(getActivity())
                        .setTitle("Upload fail")
                        .setMessage("Please try again")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                chatMessageArrayAdapter.removeItem(uploadImage);
                            }
                        })
                        .show();
            }
        }, 1000);

    }

    /**
     * Start Gallery activity
     */
    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE);
    }

    /**
     * Start Camera activity
     */
    private void openCamera() {
        captureFileUri = Uri.fromFile(FilePathUtil.getOutputMediaFile());
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, captureFileUri);
        startActivityForResult(intent, CAMERA_REQUEST_CODE);
    }

    /**
     * Upload image to server
     * @param path image path in device
     * @param mimeType mime type of image
     */
    private void uploadImage(String path, String mimeType) {
        final UploadImage uploadImage = new UploadImage(path, mimeType);
        uploadImage.addListener(new UploadImage.OnUploadImageInteractionListener() {
            @Override
            public void onRotateCompleted(String path) {}

            @Override
            public void onProgressUpdate(int percentage) {}

            @Override
            public void onCanceled() {
                chatMessageArrayAdapter.removeItem(uploadImage);
            }

            @Override
            public void onError(Throwable e) {
                showUploadFailDialog(uploadImage);
            }

            @Override
            public void onCompleted(String result) {
                new AsyncTask<String, Void, String[]>() {

                    @Override
                    protected String[] doInBackground(String... params) {
                        try {
                            String[] ret = new String[3];
                            JSONObject jsonFromServer = new JSONObject(params[0])
                                    .getJSONObject(new File(uploadImage.getFilename()).getName());
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
                            chatMessageArrayAdapter.removeItem(uploadImage);
                            SendMessage(ChatMessage.Type.IMAGE, s[0], s[1]);
                        }
                    }
                }.execute(result);
            }
        });

        addMessage(uploadImage);
        new MediaUploadManager(getContext(), uploadImage).upload(userJID, chat.getId());
    }

    private boolean isDeviceSupportCamera() {
        return getActivity().getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA);
    }

    private boolean isDeviceHasDefualtCameraApp() {
        final PackageManager packageManager = getActivity().getPackageManager();
        final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

        return list.size() > 0;
    }

    /**
     * For sending message
     * @param type type of ChatMessage [TEXT, IMAGE]
     * @param body if type equal to IMAGE body[0] must be {image_url} and body[1] must be {thumbnail_url}
     */
    private void SendMessage(final ChatMessage.Type type, final String... body) {
        new AsyncTask<Void, Void, ChatMessage>() {

            @Override
            protected ChatMessage doInBackground(Void... params) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put(ChatMessage.KEY_MESSAGE_TYPE, "chat");
                    jsonObject.put(ChatMessage.KEY_MEDIA_TYPE, type.getString());

                    if(type == ChatMessage.Type.TEXT) {
                        jsonObject.put(TextMessage.KEY_CONTENT, body[0]);
                    } else if(type == ChatMessage.Type.IMAGE) {
                        jsonObject.put(ImageMessage.KEY_FILE_URL, body[0]);
                        jsonObject.put(ImageMessage.KEY_THUMBNAIL_URL, body[1]);
                        if(body.length > 2)
                            jsonObject.put(ImageMessage.KEY_CAPTION, body[2]);
                    }

                    return new ChatMessage.Builder()
                            .setChatID(chat.getId())
                            .setBody(jsonObject.toString())
                            .setFromMe(true)
                            .setTimestamp(DateTimeUtil.getCurrentTimeStamp())
                            .build();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(ChatMessage message) {
                super.onPostExecute(message);
                if(message == null)
                    return;

                addMessage(message);
                mListener.onSendMessage(message);

                if(type == ChatMessage.Type.TEXT)
                    clearSendMessage();
            }
        }.execute();

    }

    public interface OnChatFragmentInteractionListener {
        void onSendMessage(ChatMessage chatMessage);
        void onLeaveGroup(Chat chat);
        void onOpenLink(String url);
        void onChatUpdated(String chatID);
    }

    /**
     * History messages loader
     */
    private class LoadMessageHistory {

        private LoadMoreMessageProgress loadMoreMessageProgress;
        private String lastSentTime;

        public LoadMessageHistory() {
            // init with current time
            lastSentTime = DateTimeUtil.getCurrentTimeStamp();
        }

        public void load() {
            if(loadMoreMessageProgress == null) {
                loadMoreMessageProgress = new LoadMoreMessageProgress(listener);
                chatMessageArrayAdapter.addItemToTop(loadMoreMessageProgress);
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
            }

            @Override
            public void onLoad() {
                if(task == null || task.getStatus() == AsyncTask.Status.FINISHED)
                    task = initialTask();

                if(task.getStatus() == AsyncTask.Status.PENDING)
                    task.execute();
            }
        };

        private AsyncTask<Void, Void, List<Object>> initialTask() {
            return new AsyncTask<Void, Void, List<Object>>() {

                @Override
                protected List<Object> doInBackground(Void... params) {
                    ArrayList<Object> res = new ArrayList<>();
                    try {
                        res.addAll(ChatHistoryManager.getInstance(getContext())
                                .getMessages(chat.getId(), lastSentTime, 30));
                        return res;
                    } catch (XmlPullParserException|IOException|SmackException e) {
                        e.printStackTrace();
                        return null;
                    }
                }

                @Override
                protected void onPostExecute(List<Object> chatMessages) {
                    super.onPostExecute(chatMessages);
                    if(chatMessages == null)
                        loadMoreMessageProgress.error(null);
                    else {
                        loadMoreMessageProgress.completed(chatMessages);
                        if (chatMessages.size() > 0)
                            lastSentTime = ((ChatMessage) chatMessages.get(chatMessages.size() - 1)).getTimestamp();
                    }
                }

            };
        }


    }

}
