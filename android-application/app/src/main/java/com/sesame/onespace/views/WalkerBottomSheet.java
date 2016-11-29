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
import com.sesame.onespace.models.map.Walker;
import com.sesame.onespace.service.MessageService;

/**
 * Created by chongos on 11/6/15 AD.
 */
public class WalkerBottomSheet implements DialogInterface.OnClickListener {

    private Context context;
    private Walker walker;
    private BottomSheet bottomSheet;

    public WalkerBottomSheet(Context context, Walker walker) {
        this.context = context;
        this.walker = walker;
        this.bottomSheet = new BottomSheet.Builder((Activity) context)
                .title(walker.getUserName())
                .sheet(R.menu.menu_marker_walker)
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
                Chat chat = ChatHistoryManager.getInstance(context).createPrivateChat(walker);
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
