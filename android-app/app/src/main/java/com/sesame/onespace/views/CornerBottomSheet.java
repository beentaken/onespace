package com.sesame.onespace.views;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.cocosw.bottomsheet.BottomSheet;
import com.sesame.onespace.R;
import com.sesame.onespace.models.map.Corner;

/**
 * Created by chongos on 11/17/15 AD.
 */
public class CornerBottomSheet implements DialogInterface.OnClickListener {

    private Context context;
    private Corner corner;
    private BottomSheet bottomSheet;
    private OnCornerBottomSheetInteractionListener listener;

    public CornerBottomSheet(Context context, Corner corner) {
        this.context = context;
        this.corner = corner;
        this.bottomSheet = new BottomSheet.Builder((Activity) context)
                .title(corner.getName())
                .sheet(corner.isMine()
                        ? R.menu.menu_marker_my_corner : R.menu.menu_marker_other_corner)
                .listener(this)
                .build();
    }

    public CornerBottomSheet setOnCornerDeletedListener(OnCornerBottomSheetInteractionListener listener) {
        this.listener = listener;
        return this;
    }

    public void show() {
        this.bottomSheet.show();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case R.id.join_groupchat:
                joinGroup();
                break;
            case R.id.delete_corner:
                deleteCorner();
                break;
            case R.id.get_corner_info:
                getCornerInfo();
                break;
        }
    }

    private void joinGroup() {
        if(listener != null)
            listener.onJoinGroup(corner);
    }

    private void deleteCorner() {
        if(listener != null)
            listener.onDeleted(corner);
    }

    private void getCornerInfo() {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_corner_info);
        dialog.setCancelable(true);

        String cornerLocation = corner.getLat() + ", " + corner.getLng();
        String cornerCreated = corner.getCreated().replace("T", " ").replace(".000Z", "");
        ((TextView) dialog.findViewById(R.id.popup_corner_info_name)).setText(corner.getName());
        ((TextView) dialog.findViewById(R.id.popup_corner_info_description)).setText(corner.getDescription());
        ((TextView) dialog.findViewById(R.id.popup_corner_info_location)).setText(cornerLocation);
        ((TextView) dialog.findViewById(R.id.popup_corner_info_creator)).setText(corner.getCreatorJid().split("@")[0]);
        ((TextView) dialog.findViewById(R.id.popup_corner_info_created_time)).setText(cornerCreated);
        dialog.findViewById(R.id.popup_corner_info_close_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });
        dialog.show();

    }

    public interface OnCornerBottomSheetInteractionListener {
        void onJoinGroup(Corner corner);
        void onDeleted(Corner corner);
    }

}