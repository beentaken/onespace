package com.sesame.onespace.dialogs.mapDialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.sesame.onespace.R;

/**
 * Created by Thian on 19/12/2559.
 */

public final class SelectDashboardDialogBuilder extends AlertDialog.Builder {

    private final Context context;
    private TextView title;

    public SelectDashboardDialogBuilder(Context context, int theme) {
        super(context, theme);
        this.context = context;

        View customTitle = View.inflate(this.context, R.layout.alert_dialog_select_dashboard, null);
        this.title = (TextView) customTitle.findViewById(R.id.alertTitle);
        setCustomTitle(customTitle);

    }

    @Override
    public SelectDashboardDialogBuilder setTitle(int textResId) {
        this.title.setText(textResId);
        return this;
    }
    @Override
    public SelectDashboardDialogBuilder setTitle(CharSequence text) {
        this.title.setText(text);
        return this;
    }

}
