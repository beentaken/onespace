package com.sesame.onespace.service.xmpp;

import android.content.Context;

import com.sesame.onespace.databases.KeyValueHelper;
import com.sesame.onespace.utils.Log;

/**
 * Created by chongos on 10/23/15 AD.
 */
public class XmppStatus {

    private static XmppStatus sXmppStatus;
    private KeyValueHelper mKeyValueHelper;

    private XmppStatus(Context context) {
        mKeyValueHelper = KeyValueHelper.getInstance(context);
    }

    public static XmppStatus getInstance(Context ctx) {
        if (sXmppStatus == null) {
            sXmppStatus = new XmppStatus(ctx);
        }
        return sXmppStatus;
    }

    public int getLastKnowState() {
        int res = XmppManager.DISCONNECTED;
        String value = mKeyValueHelper.getValue(KeyValueHelper.KEY_XMPP_STATUS);
        if (value != null) {
            try {
                res = Integer.parseInt(value);
            } catch(NumberFormatException e) {
                Log.e("XmppStatus unable to parse integer", e);
            }
        }
        return res;
    }

    public void setState(int status) {
        String value = Integer.toString(status);
        mKeyValueHelper.addKey(KeyValueHelper.KEY_XMPP_STATUS, value);
    }

}
