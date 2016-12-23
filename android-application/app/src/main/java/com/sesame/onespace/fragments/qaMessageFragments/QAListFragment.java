package com.sesame.onespace.fragments.qaMessageFragments;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sesame.onespace.R;
import com.sesame.onespace.activities.dialogActivities.QAMessageDialogActivity;
import com.sesame.onespace.databases.qaMessageDatabases.QAMessageHelper;
import com.sesame.onespace.models.qaMessage.QAMessage;
import com.sesame.onespace.views.DividerItemDecoration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.sesame.onespace.R.id.date;

/**
 * Created by Thian on 20/12/2559.
 */

public class QAListFragment
        extends Fragment {

    //===========================================================================================================//
    //  ATTRIBUTE                                                                                   ATTRIBUTE
    //===========================================================================================================//

    private QAMessageHelper qaMessageHelper;
    private ArrayList<QAMessage> list;

    private RecyclerView recyclerView;
    private QAListAdapter adapter;
    private List<QAListItem> items;

    private Handler handler;
    private Boolean bThread;

    private View view;

    //===========================================================================================================//
    //  CONSTRUCTOR                                                                                 CONSTRUCTOR
    //===========================================================================================================//

    public QAListFragment(){



    }

    public static QAListFragment newInstance() {

        QAListFragment fragment = new QAListFragment();
        return fragment;

    }

    //===========================================================================================================//
    //  ON ACTION                                                                                   ON ACTION
    //===========================================================================================================//

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_qa_main, container, false);

        QAListFragment.this.qaMessageHelper = new QAMessageHelper(QAListFragment.this.getContext());

        if (QAListFragment.this.qaMessageHelper.getCount() != 0){

            QAListFragment.this.list = QAListFragment.this.qaMessageHelper.getAllQAMessages();

        }
        else{

            QAListFragment.this.list = new ArrayList<QAMessage>();

        }

        this.items = new ArrayList<>();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity().getBaseContext());

        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        adapter = new QAListAdapter(getContext(), items);
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), 86, 10));

        this.handler = new Handler();

        return view;
    }

    @Override
    public void onResume(){
        QAListFragment.super.onResume();

        if (QAListFragment.this.qaMessageHelper.getCount() != 0){

            LinearLayout rl1 = (LinearLayout) view.findViewById(R.id.empty_chat_state_view);
            rl1.setVisibility(View.GONE);

            QAListFragment.this.list = QAListFragment.this.qaMessageHelper.getAllQAMessages();

        }
        else{

            LinearLayout rl1 = (LinearLayout) view.findViewById(R.id.empty_chat_state_view);
            rl1.setVisibility(View.VISIBLE);
            QAListFragment.this.list = new ArrayList<QAMessage>();

        }

        Collections.reverse(QAListFragment.this.list);

        this.items.clear();

        int index = 0;

        for (QAMessage qaMessage : QAListFragment.this.list){

            items.add(new QAListItem(R.drawable.ic_qa_message_row, qaMessage));

            index = index + 1;

        }

        adapter.notifyDataSetChanged();

        NotificationManager notifManager= (NotificationManager) QAListFragment.this.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notifManager.cancelAll();

        this.bThread = true;

        Thread thread = new Thread(){

            @Override
            public void run(){

                while (bThread == true){

                    handler.post(new Runnable() {
                        @Override
                        public void run() {

                            if (QAListFragment.this.qaMessageHelper.getCount() != 0){

                                LinearLayout rl1 = (LinearLayout) view.findViewById(R.id.empty_chat_state_view);
                                rl1.setVisibility(View.GONE);

                                QAListFragment.this.list = QAListFragment.this.qaMessageHelper.getAllQAMessages();

                            }
                            else{

                                LinearLayout rl1 = (LinearLayout) view.findViewById(R.id.empty_chat_state_view);
                                rl1.setVisibility(View.VISIBLE);
                                QAListFragment.this.list = new ArrayList<QAMessage>();

                            }

                            Collections.reverse(QAListFragment.this.list);

                            items.clear();

                            int index = 0;

                            for (QAMessage qaMessage : QAListFragment.this.list){

                                items.add(new QAListItem(R.drawable.ic_qa_message_row, qaMessage));

                                index = index + 1;

                            }

                            adapter.notifyDataSetChanged();

                            NotificationManager notifManager= (NotificationManager) QAListFragment.this.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
                            notifManager.cancelAll();

                        }
                    });

                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }

            }

        };

        thread.start();

    }

    @Override
    public void onPause(){
        super.onPause();

        this.bThread = false;

    }

    private class QAListAdapter extends RecyclerView.Adapter<QAListFragment.ViewHolder> {

        private List<QAListItem> items;
        private Context mContext;

        public QAListAdapter(Context context, List<QAListItem> settingItems) {
            this.items = settingItems;
            this.mContext = context;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_qa_main_menu, null);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int i) {

            final QAListItem item = items.get(i);

            viewHolder.title.setText(item.getQuestionStr());
            viewHolder.icon.setImageResource(item.getIcon());
            viewHolder.date.setText(item.getDate());
            viewHolder.rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent dialogIntent = new Intent(mContext, QAMessageDialogActivity.class);
                    dialogIntent.putExtra("id", item.getId());
                    dialogIntent.putExtra("msgFrom", item.getMsgFrom());
                    dialogIntent.putExtra("questionId", item.getQuestionID());
                    dialogIntent.putExtra("questionStr", item.getQuestionStr());
                    dialogIntent.putStringArrayListExtra("answerIdList", item.getAnswerIDList());
                    dialogIntent.putStringArrayListExtra("answerStrList", item.getAnswerStrList());
                    dialogIntent.putExtra("date", item.getDate());
                    dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    dialogIntent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                    QAListFragment.this.startActivity(dialogIntent);

                }
            });

        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        CardView rootView;
        TextView title;
        ImageView icon;
        TextView date;

        ViewHolder(View itemView) {
            super(itemView);
            rootView = (CardView)itemView.findViewById(R.id.root_view);
            title = (TextView)itemView.findViewById(R.id.title);
            icon = (ImageView)itemView.findViewById(R.id.icon);
            date = (TextView)itemView.findViewById(R.id.date);
        }
    }

    public static class QAListItem implements Parcelable {

        private int icon;

        private Integer id;
        private String msgFrom;
        private String questionID;
        private String questionStr;
        private ArrayList<String> answerIDList;
        private ArrayList<String> answerStrList;
        private String date;

        private QAListItem(Bundle bundle) {

            icon = bundle.getInt("icon");

            id = bundle.getInt("id");
            msgFrom = bundle.getString("msgFrom");
            questionID = bundle.getString("questionID");
            questionStr = bundle.getString("questionStr");
            answerIDList = bundle.getStringArrayList("answerIDList");
            answerStrList = bundle.getStringArrayList("answerStrList");
            date = bundle.getString("date");

        }

        public QAListItem(int icon, QAMessage qaMessage) {

            this.icon = icon;

            this.id = qaMessage.getId();
            this.msgFrom = qaMessage.getMsgFrom();
            this.questionID = qaMessage.getQuestionID();
            this.questionStr = qaMessage.getQuestionStr();
            this.answerIDList = qaMessage.getAnswerIDList();
            this.answerStrList = qaMessage.getAnswerStrList();
            this.date = qaMessage.getDate();

        }

        public static final Creator<QAListItem> CREATOR = new Creator<QAListItem>() {
            @Override
            public QAListItem createFromParcel(Parcel in) {
                return new QAListItem(in.readBundle());
            }

            @Override
            public QAListItem[] newArray(int size) {
                return new QAListItem[size];
            }
        };

        public int getIcon() {
            return icon;
        }

        public int getId(){

            return this.id;

        }

        public String getMsgFrom(){

            return this.msgFrom;

        }

        public String getQuestionID(){

            return this.questionID;

        }

        public String getQuestionStr(){

            return this.questionStr;

        }

        public ArrayList<String> getAnswerIDList(){

            return this.answerIDList;

        }

        public ArrayList<String> getAnswerStrList(){

            return this.answerStrList;

        }

        public String getDate(){

            return this.date;

        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            Bundle bundle = new Bundle();
            bundle.putInt("icon", icon);
            bundle.putString("msgFrom", msgFrom);
            bundle.putString("questionID", questionID);
            bundle.putString("questionStr", questionStr);
            bundle.putStringArrayList("answerIDList", answerIDList);
            bundle.putStringArrayList("answerStrList", answerStrList);
            bundle.putString("date", date);
            dest.writeBundle(bundle);
        }

    }

}
