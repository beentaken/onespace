package com.sesame.onespace.fragments.qaMessageFragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sesame.onespace.R;
import com.sesame.onespace.activities.dialogActivities.QAChoiceDialogActivity;
import com.sesame.onespace.activities.dialogActivities.QAImageDialogActivity;
import com.sesame.onespace.databases.qaMessageDatabases.QAMessageHelper;
import com.sesame.onespace.models.qaMessage.QAMessage;
import com.sesame.onespace.views.DividerItemDecoration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Thian on 20/12/2559.
 */

public final class QAMainFragment
        extends Fragment {

    //===========================================================================================================//
    //  ATTRIBUTE                                                                                   ATTRIBUTE
    //===========================================================================================================//

    //view ---------------------------------------------------
    private View view;

    //Database -----------------------------------------------
    private QAMessageHelper qaMessageHelper;
    private ArrayList<QAMessage> list;

    //RecyclerView -------------------------------------------
    private ArrayList<QAListItem> items;
    private QAListAdapter adapter;
    private RecyclerView recyclerView;

    //Thread -------------------------------------------------
    private Handler handler;
    private Boolean bThread;

    //===========================================================================================================//
    //  CONSTRUCTOR                                                                                 CONSTRUCTOR
    //===========================================================================================================//

    public QAMainFragment(){



    }

    public static QAMainFragment newInstance() {

        QAMainFragment fragment = new QAMainFragment();
        return fragment;

    }

    //===========================================================================================================//
    //  ON ACTION                                                                                   ON ACTION
    //===========================================================================================================//

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        QAMainFragment.this.view = inflater.inflate(R.layout.fragment_qa_main, container, false);

        QAMainFragment.this.qaMessageHelper = new QAMessageHelper(QAMainFragment.this.getContext());

        if (QAMainFragment.this.qaMessageHelper.getCount() != 0){

            QAMainFragment.this.list = QAMainFragment.this.qaMessageHelper.getAllQAMessages();

        }
        else{

            QAMainFragment.this.list = new ArrayList<QAMessage>();

        }

        this.items = new ArrayList<QAListItem>();

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
        QAMainFragment.super.onResume();

        LinearLayout linearLayout = (LinearLayout) QAMainFragment.this.view.findViewById(R.id.empty_chat_state_view);
        if (QAMainFragment.this.qaMessageHelper.getCount() != 0){

            linearLayout.setVisibility(View.GONE);
            QAMainFragment.this.list = QAMainFragment.this.qaMessageHelper.getAllQAMessages();

        }
        else{

            linearLayout.setVisibility(View.VISIBLE);
            QAMainFragment.this.list = new ArrayList<QAMessage>();

        }

        Collections.reverse(QAMainFragment.this.list);

        QAMainFragment.this.items.clear();

        int index = 0;

        for (QAMessage qaMessage : QAMainFragment.this.list){

            QAMainFragment.this.items.add(new QAListItem(R.drawable.ic_qa_message_row, qaMessage));

            index = index + 1;

        }

        QAMainFragment.this.adapter.notifyDataSetChanged();

        QAMainFragment.this.bThread = true;

        Thread thread = new Thread(){

            @Override
            public void run(){

                while (QAMainFragment.this.bThread == true){

                    QAMainFragment.this.handler.post(new Runnable() {

                        @Override
                        public void run() {

                            LinearLayout linearLayout = (LinearLayout) QAMainFragment.this.view.findViewById(R.id.empty_chat_state_view);
                            if (QAMainFragment.this.qaMessageHelper.getCount() != 0){

                                linearLayout.setVisibility(View.GONE);
                                QAMainFragment.this.list = QAMainFragment.this.qaMessageHelper.getAllQAMessages();

                            }
                            else{

                                linearLayout.setVisibility(View.VISIBLE);
                                QAMainFragment.this.list = new ArrayList<QAMessage>();

                            }

                            Collections.reverse(QAMainFragment.this.list);

                            QAMainFragment.this.items.clear();

                            int index = 0;

                            for (QAMessage qaMessage : QAMainFragment.this.list){

                                QAMainFragment.this.items.add(new QAListItem(R.drawable.ic_qa_message_row, qaMessage));

                                index = index + 1;

                            }

                            QAMainFragment.this.adapter.notifyDataSetChanged();

                        }
                    });

                    try {
                        Thread.sleep(1000);
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

        QAMainFragment.this.bThread = false;

    }

    //===========================================================================================================//
    //  ADAPTER                                                                                     ADAPTER
    //===========================================================================================================//

    private class QAListAdapter
            extends RecyclerView.Adapter<QAMainFragment.ViewHolder> {

        private List<QAListItem> items;
        private Context mContext;

        public QAListAdapter(Context context, List<QAListItem> settingItems) {

            QAListAdapter.this.items = settingItems;
            QAListAdapter.this.mContext = context;

        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_qa_main, null);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int i) {

            final QAListItem item = QAListAdapter.this.items.get(i);

            viewHolder.sender.setText(item.getMsgFrom().split("@")[0] + " send message.");
            viewHolder.title.setText(item.getQuestionStr());
            viewHolder.icon.setImageResource(item.getIcon());
            viewHolder.date.setText(item.getDate());
            viewHolder.rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent dialogIntent = null;

                    if (item.getType().equals("text")){

                        dialogIntent = new Intent(QAListAdapter.this.mContext, QAChoiceDialogActivity.class);

                    }

                    if (item.getType().equals("image")){

                        dialogIntent = new Intent(QAListAdapter.this.mContext, QAImageDialogActivity.class);

                    }

                    dialogIntent.putExtra("id", item.getId());
                    dialogIntent.putExtra("msgFrom", item.getMsgFrom());
                    dialogIntent.putExtra("type", item.getType());
                    dialogIntent.putExtra("questionId", item.getQuestionID());
                    dialogIntent.putExtra("questionStr", item.getQuestionStr());
                    dialogIntent.putStringArrayListExtra("answerIdList", item.getAnswerIDList());
                    dialogIntent.putStringArrayListExtra("answerStrList", item.getAnswerStrList());
                    dialogIntent.putExtra("date", item.getDate());
                    dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    dialogIntent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                    QAMainFragment.this.startActivity(dialogIntent);

                }
            });

        }

        @Override
        public int getItemCount() {
            return QAListAdapter.this.items.size();
        }

    }

    //===========================================================================================================//
    //  VIEW HOLDER                                                                                 VIEW HOLDER
    //===========================================================================================================//

    private static class ViewHolder
            extends RecyclerView.ViewHolder {

        CardView rootView;
        TextView sender;
        TextView title;
        ImageView icon;
        TextView date;

        public ViewHolder(View itemView) {
            super(itemView);

            ViewHolder.this.rootView = (CardView)itemView.findViewById(R.id.root_view);
            ViewHolder.this.sender = (TextView)itemView.findViewById(R.id.sender);
            ViewHolder.this.title = (TextView)itemView.findViewById(R.id.title);
            ViewHolder.this.icon = (ImageView)itemView.findViewById(R.id.icon);
            ViewHolder.this.date = (TextView)itemView.findViewById(R.id.date);

        }

    }

    //===========================================================================================================//
    //  ITEM                                                                                        ITEM
    //===========================================================================================================//

    private static class QAListItem
            implements Parcelable {

        private Integer icon;

        private Integer id;
        private String msgFrom;
        private String type;
        private String questionID;
        private String questionStr;
        private ArrayList<String> answerIDList;
        private ArrayList<String> answerStrList;
        private String date;

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

        private QAListItem(Bundle bundle) {

            QAListItem.this.icon = bundle.getInt("icon");

            QAListItem.this.id = bundle.getInt("id");
            QAListItem.this.msgFrom = bundle.getString("msgFrom");
            QAListItem.this.type = bundle.getString("type");
            QAListItem.this.questionID = bundle.getString("questionID");
            QAListItem.this.questionStr = bundle.getString("questionStr");
            QAListItem.this.answerIDList = bundle.getStringArrayList("answerIDList");
            QAListItem.this.answerStrList = bundle.getStringArrayList("answerStrList");
            QAListItem.this.date = bundle.getString("date");

        }

        public QAListItem(Integer icon, QAMessage qaMessage) {

            QAListItem.this.icon = icon;

            QAListItem.this.id = qaMessage.getId();
            QAListItem.this.msgFrom = qaMessage.getMsgFrom();
            QAListItem.this.type = qaMessage.getType();
            QAListItem.this.questionID = qaMessage.getQuestionID();
            QAListItem.this.questionStr = qaMessage.getQuestionStr();
            QAListItem.this.answerIDList = qaMessage.getAnswerIDList();
            QAListItem.this.answerStrList = qaMessage.getAnswerStrList();
            QAListItem.this.date = qaMessage.getDate();

        }

        public int getIcon() {
            return QAListItem.this.icon;
        }

        public int getId(){

            return QAListItem.this.id;

        }

        public String getMsgFrom(){

            return QAListItem.this.msgFrom;

        }

        public String getType(){

            return QAListItem.this.type;

        }

        public String getQuestionID(){

            return QAListItem.this.questionID;

        }

        public String getQuestionStr(){

            return QAListItem.this.questionStr;

        }

        public ArrayList<String> getAnswerIDList(){

            return QAListItem.this.answerIDList;

        }

        public ArrayList<String> getAnswerStrList(){

            return QAListItem.this.answerStrList;

        }

        public String getDate(){

            return QAListItem.this.date;

        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            Bundle bundle = new Bundle();
            bundle.putInt("icon", QAListItem.this.icon);
            bundle.putString("msgFrom", QAListItem.this.msgFrom);
            bundle.putString("type", QAListItem.this.type);
            bundle.putString("questionID", QAListItem.this.questionID);
            bundle.putString("questionStr", QAListItem.this.questionStr);
            bundle.putStringArrayList("answerIDList", QAListItem.this.answerIDList);
            bundle.putStringArrayList("answerStrList", QAListItem.this.answerStrList);
            bundle.putString("date", QAListItem.this.date);
            dest.writeBundle(bundle);
        }

    }

    //===========================================================================================================//
    //  NOTE                                                                                        NOTE
    //===========================================================================================================//

}
