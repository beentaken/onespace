package com.sesame.onespace.views.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.style.URLSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sesame.onespace.R;
import com.sesame.onespace.activities.FullScreenViewActivity;
import com.sesame.onespace.fragments.ChatRoomFragment;
import com.sesame.onespace.managers.SettingsManager;
import com.sesame.onespace.models.chat.Chat;
import com.sesame.onespace.models.chat.ChatMessage;
import com.sesame.onespace.models.chat.ImageMessage;
import com.sesame.onespace.models.chat.TextMessage;
import com.sesame.onespace.models.chat.UploadImage;
import com.sesame.onespace.utils.DateTimeUtil;
import com.sesame.onespace.models.chat.LoadMoreMessageProgress;
import com.sesame.onespace.utils.DrawableUtil;
import com.sesame.onespace.utils.Log;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by chongos on 9/4/15 AD.
 */
public class ChatMessageArrayAdapter extends RecyclerView.Adapter {

    private static final int TYPE_TEXT = 1;
    private static final int TYPE_IMAGE = 3;
    private static final int TYPE_IMAGE_UPLOAD = 4;
    private static final int TYPE_LOAD_MORE = -1;

    private List<Object> messages;
    private Chat mChat;
    private Context mContext;
    private ChatRoomFragment.OnChatFragmentInteractionListener listener;
    private SettingsManager settingsManager;

    public ChatMessageArrayAdapter(Context context, Chat chat, List<Object> dataset) {
        messages = dataset;
        mChat = chat;
        mContext = context;
        settingsManager = SettingsManager.getSettingsManager(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case TYPE_LOAD_MORE:
                return new LoadMoreViewHolder(
                        inflater.inflate(R.layout.row_message_load_more, parent, false));
            case TYPE_IMAGE_UPLOAD:
                return new ImageUploadViewHolder(
                        inflater.inflate(R.layout.row_image_upload, parent, false));
            case TYPE_IMAGE:
                return new ImageMessageViewHolder(
                        inflater.inflate(R.layout.row_message, parent, false));
            case TYPE_TEXT:
            default:
                return new TextMessageViewHolder(
                        inflater.inflate(R.layout.row_message, parent, false));
        }
    }

    @Override
    public int getItemViewType(int position) {
        try {
            Object message = messages.get(position);
            if (message instanceof TextMessage)
                return TYPE_TEXT;
            else if (message instanceof ImageMessage)
                return TYPE_IMAGE;
            else if (message instanceof UploadImage)
                return TYPE_IMAGE_UPLOAD;
            else if (message instanceof LoadMoreMessageProgress)
                return TYPE_LOAD_MORE;
        } catch (IndexOutOfBoundsException e) {
            return TYPE_LOAD_MORE;
        }
        return super.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        int type = viewHolder.getItemViewType();
        if (type == TYPE_LOAD_MORE) {
            configureLoadMoreViewHolder(viewHolder, position);
        } else if(type == TYPE_IMAGE_UPLOAD) {
            configureImageUploadViewHolder(viewHolder, position);
        } else {
            configureChatMessageViewHolder(viewHolder, position);
            switch (type) {
                case TYPE_IMAGE:
                    configureImageMessageViewHolder(viewHolder, position);
                    break;
                case TYPE_TEXT:
                default:
                    configureTextMessageViewHolder(viewHolder, position);
            }
        }

    }

    private void configureLoadMoreViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        final LoadMoreViewHolder loadMoreViewHolder = (LoadMoreViewHolder) viewHolder;
        final LoadMoreMessageProgress loadMoreMessageProgress = (LoadMoreMessageProgress) messages.get(position);

        loadMoreViewHolder.mButton.setOnClickListener(loadMoreMessageProgress);
        loadMoreMessageProgress.addListener(new LoadMoreMessageProgress.Listener() {
            @Override
            public void onCompleted(List<Object> result) {
                removeItem(loadMoreMessageProgress);
                addItemToTop(result);
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

    private void configureChatMessageViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        ChatMessageViewHolder chatMessageViewHolder = (ChatMessageViewHolder) viewHolder;

        ChatMessage message = (ChatMessage) messages.get(position);
        String date = DateTimeUtil.getDateByTimestamp(message.getTimestamp());
        String time = DateTimeUtil.getTimeByTimestamp(message.getTimestamp());

        boolean fromMe = message.isFromMe();
        boolean isMessageStartOfDate;
        try {
            isMessageStartOfDate = !(position < messages.size() - 1
                    && DateTimeUtil.isSameDate(message.getTimestamp(),
                    ((ChatMessage) messages.get(position + 1)).getTimestamp()));
        } catch (ClassCastException e) {
            isMessageStartOfDate = true;
        }

        if(!fromMe && mChat.getType() == Chat.Type.GROUP) {
            chatMessageViewHolder.mFromName.setVisibility(View.VISIBLE);
            chatMessageViewHolder.mFromName.setText(message.getFromJID().split("/")[1]);
        } else {
            chatMessageViewHolder.mFromName.setVisibility(View.GONE);
        }

        // set container gravity
        chatMessageViewHolder.mChatMessageContainer.setGravity(fromMe ? Gravity.RIGHT : Gravity.LEFT);

        // set date
        chatMessageViewHolder.mChatMessageDate.setText(date);
        chatMessageViewHolder.mChatMessageDate.setVisibility(isMessageStartOfDate
                ? View.VISIBLE : View.GONE);

        // set avatar
        chatMessageViewHolder.mAvatarImage.setVisibility(fromMe ? View.GONE : View.VISIBLE);
        if(!fromMe) {
            String name = mChat.getType() == Chat.Type.PRIVATE
                    ? message.getChatID().split("@")[0] : message.getFromJID().split("/")[1];
            chatMessageViewHolder.mAvatarImage.setImageDrawable(DrawableUtil.getTextDrawable(mContext, name));
        }

        // set sending status
        chatMessageViewHolder.mSendingStatus.setVisibility(fromMe ? View.VISIBLE : View.GONE);
        chatMessageViewHolder.mSendingStatus.setImageResource(message.needPush()
                    ? R.drawable.ic_message_status_sending : R.drawable.ic_message_status_sent);

        // set time
        chatMessageViewHolder.mChatMessageTime.setText(time);
        chatMessageViewHolder.setMessageTimeGravity(fromMe ? Gravity.LEFT : Gravity.RIGHT);

    }

    private void configureTextMessageViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        final TextMessage textMessage = (TextMessage) messages.get(position);
        boolean isSend = textMessage.isFromMe();
        int textColor = mContext.getResources().getColor(isSend ? R.color.white : R.color.black);
        int linkTextColor = mContext.getResources().getColor(isSend ? R.color.light_green_A200 : R.color.color_accent);
        int backgroundRes = isSend ? R.drawable.chat_bubble_send_blue : R.drawable.chat_bubble_recv;
        int maxEms = 22 - (settingsManager.chatFontSize / 2) + (isSend ? 2 : 0);

        final TextMessageViewHolder textMessageViewHolder = (TextMessageViewHolder) viewHolder;
        textMessageViewHolder.mTextView.setBackgroundResource(backgroundRes);
        textMessageViewHolder.mTextView.setText(textMessage.getMessage());
        textMessageViewHolder.mTextView.setTextColor(textColor);
        textMessageViewHolder.mTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, settingsManager.chatFontSize);
        textMessageViewHolder.mTextView.setMaxEms(maxEms);
        textMessageViewHolder.mTextView.setLinkTextColor(linkTextColor);
        fixTextView(textMessageViewHolder.mTextView);
    }

    /**
     * For handling link in TextView
     * @param tv TextView of text message
     */
    private void fixTextView(TextView tv) {
        try {
            SpannableString current = (SpannableString) tv.getText();
            URLSpan[] spans =
                    current.getSpans(0, current.length(), URLSpan.class);

            for (URLSpan span : spans) {
                int start = current.getSpanStart(span);
                int end = current.getSpanEnd(span);

                current.removeSpan(span);
                current.setSpan(new DefensiveURLSpan(span.getURL()), start, end, 0);
            }
        } catch (ClassCastException e) {

        }
    }

    public class DefensiveURLSpan extends URLSpan {
        private String mUrl;

        public DefensiveURLSpan(String url) {
            super(url);
            mUrl = url;
        }

        @Override
        public void onClick(View widget) {
            Log.i("Clicked link: " + mUrl);
            listener.onOpenLink(mUrl);
        }
    }

    private void configureImageMessageViewHolder(final RecyclerView.ViewHolder viewHolder, int position) {
        final ImageMessage imageMessage = (ImageMessage) messages.get(position);
        final ImageMessageViewHolder imageMessageViewHolder = (ImageMessageViewHolder) viewHolder;
        imageMessageViewHolder.container.setBackgroundResource(imageMessage.isFromMe()
                ? R.drawable.chat_bubble_send_blue : R.drawable.chat_bubble_recv);

        Picasso.with(mContext)
                .load(imageMessage.getThumbnailURL())
//                .transform(new RoundedTransformation(5, 0))
                .error(R.drawable.ic_broken_image_72dp)
                .noPlaceholder()
                .noFade()
                .into(imageMessageViewHolder.imageView, new Callback() {
                            @Override
                            public void onSuccess() {
                                imageMessageViewHolder.progressBar.setVisibility(View.GONE);
                            }

                            @Override
                            public void onError() {
                                imageMessageViewHolder.progressBar.setVisibility(View.GONE);
                            }
                        }
                );
        imageMessageViewHolder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(mContext, FullScreenViewActivity.class);
                i.putExtra(ImageMessage.class.getName(), imageMessage);
                mContext.startActivity(i);
            }
        });
    }

    private void configureImageUploadViewHolder(RecyclerView.ViewHolder viewHolder, int postion) {
        final ImageUploadViewHolder imageUploadViewHolder = (ImageUploadViewHolder) viewHolder;
        final UploadImage uploadImage = (UploadImage) messages.get(postion);
        uploadImage.addListener(new UploadImage.OnUploadImageInteractionListener() {
            @Override
            public void onRotateCompleted(String path) {
                imageUploadViewHolder.preLoadProgressBar.setVisibility(View.GONE);
                imageUploadViewHolder.progressBar.setVisibility(View.VISIBLE);
                imageUploadViewHolder.previewImage.setImageBitmap(BitmapFactory.decodeFile(path));
            }

            @Override
            public void onProgressUpdate(int percentage) {
                imageUploadViewHolder.progressBar.setProgress(percentage);
            }

            @Override
            public void onCanceled() {
//                imageUploadViewHolder.buttonCancel.setVisibility(View.GONE);
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onCompleted(String result) {
            }
        });
        imageUploadViewHolder.previewImage.setImageDrawable(null);
        imageUploadViewHolder.buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage.cancel();
            }
        });
    }

    public void setOnChatFragmentInteractionListener(ChatRoomFragment.OnChatFragmentInteractionListener listener) {
        this.listener = listener;
    }

    public void addItem(Object message) {
        messages.add(0, message);
        notifyItemInserted(0);
    }

    public void addItemToTop(Object obj) {
        messages.add(obj);
        notifyItemInserted(messages.size() - 1);
    }

    public void addItemToTop(List<Object> messages) {
        this.messages.addAll(messages);
        notifyDataSetChanged();
    }

    public void removeItem(Object obj) {
        int position = messages.indexOf(obj);
        messages.remove(obj);
        notifyItemRemoved(position);
    }

    public void updateMessageSent(final ChatMessage chatMessage) {
        new AsyncTask<Void, Void, Integer>() {

            @Override
            protected Integer doInBackground(Void... params) {
                for(int i=0; i<messages.size(); i++) {
                    Object obj = messages.get(i);
                    if(obj instanceof ChatMessage) {
                        ChatMessage message = (ChatMessage) obj;
                        if(message.getTimestamp().equals(chatMessage.getTimestamp()) &&
                                message.getBody().equals(chatMessage.getBody())) {
                            message.setNeedPush(chatMessage.needPush());
                            return i;
                        }
                    }
                }
                return -1;
            }

            @Override
            protected void onPostExecute(Integer position) {
                super.onPostExecute(position);
                if(position >= 0)
                    notifyItemChanged(position);
            }
        }.execute();
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

    public static class ChatMessageViewHolder extends RecyclerView.ViewHolder {

        public LinearLayout mChatMessageContainer;
        public TextView mChatMessageDate;
        public TextView mChatMessageTime;
        public ImageView mAvatarImage;
        public TextView mFromName;
        public ImageView mSendingStatus;

        private View mMessageTimeContainer;
        private LinearLayout mMessageContentContainer;

        public ChatMessageViewHolder(View view) {
            super(view);
            mChatMessageContainer = (LinearLayout) view.findViewById(R.id.message_container);
            mChatMessageTime = (TextView) view.findViewById(R.id.message_time);
            mChatMessageDate = (TextView) view.findViewById(R.id.message_date);
            mFromName = (TextView) view.findViewById(R.id.message_from_name);
            mAvatarImage = (ImageView) view.findViewById(R.id.main_user_avatar);
            mSendingStatus = (ImageView) view.findViewById(R.id.message_sending_status);
            mMessageContentContainer = (LinearLayout) view.findViewById(R.id.message_content_container);
            mMessageTimeContainer = view.findViewById(R.id.message_time_container);
        }

        public void setMessageTimeGravity(int gravity) {
            mMessageContentContainer.removeView(mMessageTimeContainer);
            if(gravity == Gravity.LEFT)
                mMessageContentContainer.addView(mMessageTimeContainer, 0);
            else
                mMessageContentContainer.addView(mMessageTimeContainer);
        }
    }

    public static class TextMessageViewHolder extends ChatMessageViewHolder {

        public TextView mTextView;

        public TextMessageViewHolder(View view) {
            super(view);
            View layout = view.findViewById(R.id.message_text_layout);
            layout.setVisibility(View.VISIBLE);
            mTextView = (TextView) layout.findViewById(R.id.textview);
        }
    }

    public static class ImageMessageViewHolder extends ChatMessageViewHolder {

        public View container;
        public ProgressBar progressBar;
        public ImageView imageView;

        public ImageMessageViewHolder(View view) {
            super(view);
            View layout = view.findViewById(R.id.message_image_layout);
            layout.setVisibility(View.VISIBLE);
            container = layout.findViewById(R.id.message_image_container);
            progressBar = (ProgressBar) layout.findViewById(R.id.progress_bar);
            imageView = (ImageView) layout.findViewById(R.id.message_image);
        }

    }
    
    public static class ImageUploadViewHolder extends RecyclerView.ViewHolder {

        public LinearLayout container;
        public ImageView previewImage;
        public ProgressBar progressBar;
        public ProgressBar preLoadProgressBar;
        public ImageButton buttonCancel;

        public ImageUploadViewHolder(View view) {
            super(view);
            container = (LinearLayout) view.findViewById(R.id.message_container);
            progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
            preLoadProgressBar = (ProgressBar) view.findViewById(R.id.pre_load_progress_bar);
            previewImage = (ImageView) view.findViewById(R.id.preview_image);
            buttonCancel = (ImageButton) view.findViewById(R.id.btn_cancel);
        }

    }

}