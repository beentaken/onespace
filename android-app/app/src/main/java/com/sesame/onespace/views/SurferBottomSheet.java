package com.sesame.onespace.views;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import com.cocosw.bottomsheet.BottomSheet;
import com.sesame.onespace.R;
import com.sesame.onespace.activities.MainActivity;
import com.sesame.onespace.managers.chat.ChatHistoryManager;
import com.sesame.onespace.models.chat.Chat;
import com.sesame.onespace.models.map.Surfer;
import com.sesame.onespace.service.MessageService;

/**
 * Created by chongos on 11/6/15 AD.
 */
public class SurferBottomSheet implements DialogInterface.OnClickListener {

    private Context context;
    private Surfer surfer;
    private BottomSheet bottomSheet;

    public SurferBottomSheet(Context context, Surfer surfer) {
        this.context = context;
        this.surfer = surfer;
        this.bottomSheet = new BottomSheet.Builder((Activity) context)
                .title(surfer.getUserName())
                .sheet(R.menu.menu_marker_surfer)
                .listener(this)
                .build();
    }

    public void show() {
        this.bottomSheet.show();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case R.id.open_privatechat:
                Chat chat = ChatHistoryManager.getInstance(context).createPrivateChat(surfer);
                openChatActivity(chat);
                break;
        }
    }

    private void openChatActivity(Chat chat) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(MessageService.KEY_BUNDLE_CHAT, chat);
        intent.putExtra("from_map", true);
        context.startActivity(intent);
    }

}