package com.sesame.onespace.managers;

import android.app.backup.BackupManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

import com.sesame.onespace.R;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by chongos on 10/22/15 AD.
 */
public class SettingsManager {

    private static SettingsManager sSettingsManager = null;
    private UserAccountManager userAccountManager;
    private SharedPreferences mSharedPreferences;
    private Context mContext;
    private ArrayList<SharedPreferences.OnSharedPreferenceChangeListener> preferenceChangeListeners;
    private SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            importPreferences();
            onPreferencesUpdated(key);
            for(SharedPreferences.OnSharedPreferenceChangeListener listener : preferenceChangeListeners) {
                listener.onSharedPreferenceChanged(sharedPreferences, key);
            }
        }
    };
    public static boolean connectionSettingsObsolete;
    public String onespaceServer;
    public String onespacePort;
    public String xmppServer;
    public int xmppPort;
    public String xmppServiceName;
    public String xmppRecource;

    public boolean startOnBoot;

    public boolean notification;
    public boolean notificationVibrate;
    public boolean notificationLED;
    public boolean notificationSound;
    public String notificationRingtone;

    public int chatFontSize;
    public int chatHistoryMessageLimit;
    public boolean chatSendWithEnter;

    private SettingsManager(Context context) {
        mContext = context;
        preferenceChangeListeners = new ArrayList<>();
        userAccountManager = UserAccountManager.getInstance(mContext);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        mSharedPreferences.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
        importPreferences();
    }

    public void destroy() {
        userAccountManager.logoutUser();
        mSharedPreferences.unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
    }

    public static SettingsManager getSettingsManager(Context context) {
        if (sSettingsManager == null) {
            sSettingsManager = new SettingsManager(context);
        }
        return sSettingsManager;
    }

    public SharedPreferences getSharedPreference() {
        return mSharedPreferences;
    }

    public SharedPreferences.Editor getEditor() {
        return mSharedPreferences.edit();
    }

    public String saveStringSetting(String key, String value) {
        getEditor().putString(key, value).commit();
        return value;
    }

    public boolean saveBooleanSetting(String key, boolean value) {
        getEditor().putBoolean(key, value).commit();
        return value;
    }

    public String getStringPreference(String key, String def) {
        if(key.equals(mContext.getString(R.string.pref_chat_history_message_limit)))
            return String.valueOf(mSharedPreferences.getInt(mContext.getString(R.string.pref_chat_history_message_limit), 30));
        return mSharedPreferences.getString(key, def);
    }

    public boolean getBooleanPreference(String key, boolean def) {
        return mSharedPreferences.getBoolean(key, def);
    }

    public Map<String, ?> getAllSharedPreferences() {
        return mSharedPreferences.getAll();
    }

    public UserAccountManager getUserAccountManager() {
        return userAccountManager;
    }

    public boolean sharedPreferencesContains(String key) {
        return mSharedPreferences.contains(key);
    }

    public void registerOnSharedPreferenceChanged(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        preferenceChangeListeners.add(listener);
    }

    public void unregisterOnSharedPreferenceChanged(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        preferenceChangeListeners.remove(listener);
    }

    public void onPreferencesUpdated(String key) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            BackupManager.dataChanged(mContext.getPackageName());
        }

        if (key.equals(mContext.getString(R.string.pref_advanced_xmpp_server))
                || key.equals(mContext.getString(R.string.pref_advanced_xmpp_resource))) {
            connectionSettingsObsolete = true;
        }
    }



    private void importPreferences() {
        onespaceServer = mSharedPreferences.getString(mContext.getString(R.string.pref_advanced_onespace_server), "172.29.33.45");
        onespacePort = mSharedPreferences.getString(mContext.getString(R.string.pref_advanced_onespace_port), "11090");
        //onespaceServer = mSharedPreferences.getString("pref_advanced_onespace_server", "ubicomp-web.d1.comp.nus.edu.sg/app/onespace/api/v1/");
        //onespacePort = mSharedPreferences.getString("pref_advanced_onespace_port", "");
        //onespaceServer = mSharedPreferences.getString("pref_advanced_onespace_server", "172.29.32.195");
        //onespacePort = mSharedPreferences.getString("pref_advanced_onespace_port", "11090");
        xmppServer = mSharedPreferences.getString(mContext.getString(R.string.pref_advanced_xmpp_server), "172.29.33.45");
        //xmppServer = mSharedPreferences.getString(mContext.getString(R.string.pref_advanced_xmpp_server), "ubicomp-web.d1.comp.nus.edu.sg");
        //xmppServer = mSharedPreferences.getString(mContext.getString(R.string.pref_advanced_xmpp_server), "137.132.82.220");
        xmppPort = mSharedPreferences.getInt(mContext.getString(R.string.pref_advanced_xmpp_port), 5222);
        xmppServiceName = mSharedPreferences.getString(mContext.getString(R.string.pref_advanced_xmpp_service_name), "172.29.33.45");
        xmppRecource = mSharedPreferences.getString(mContext.getString(R.string.pref_advanced_xmpp_resource), "conference");

        notification = mSharedPreferences.getBoolean(mContext.getString(R.string.pref_notification), true);
        notificationVibrate = mSharedPreferences.getBoolean(mContext.getString(R.string.pref_notification_vibrate), true);
        notificationLED = mSharedPreferences.getBoolean(mContext.getString(R.string.pref_notification_led), true);
        notificationSound = mSharedPreferences.getBoolean(mContext.getString(R.string.pref_notification_sound), true);
        notificationRingtone = mSharedPreferences.getString(mContext.getString(R.string.pref_notification_tone), "default ringtone");

        chatFontSize = Integer.parseInt(mSharedPreferences.getString(mContext.getString(R.string.pref_chat_font_size), "16"));
        chatSendWithEnter = mSharedPreferences.getBoolean(mContext.getString(R.string.pref_chat_send_with_enter), false);
        chatHistoryMessageLimit = mSharedPreferences.getInt(mContext.getString(R.string.pref_chat_history_message_limit), 30);
    }

    public String getOnespaceServerURL() {
        if (onespacePort == "")
            return "http://" + onespaceServer;
        else
            return "http://" + onespaceServer + ":" + onespacePort;
    }

}
