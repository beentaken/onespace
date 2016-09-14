package com.sesame.onespace.managers;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.securepreferences.SecurePreferences;
import com.sesame.onespace.activities.LoginActivity;
import com.sesame.onespace.service.MessageService;

/**
 * Created by chongos on 8/4/15 AD.
 */
public class UserAccountManager {

    private static UserAccountManager instance;

    // Shared Preferences
    private SharedPreferences pref;

    // Editor for Shared preferences
    private Editor editor;

    // Context
    private Context context;

    // Sharedpref file name
    private static final String PREF_NAME = "OneSpacePref";

    // Login status
    private static final String IS_LOGIN = "IsLoggedIn";

    // Use username and password for create as a new account
    private static final String IS_CREATE_ACCOUNT = "IsCreateAccount";

    // User ID
    public static final String KEY_USERID = "UserID";

    // XMPP Server
    public static final String KEY_XMPP_SERVER = "XmppServer";

    // CMPP BOSH
    public static final String KEY_XMPP_BOSH = "XmppBosh";

    // User Login
    public static final String KEY_USERNAME = "LoginUsername";

    // Password Login
    public static final String KEY_PASSWORD = "LoginPassword";

    public static synchronized UserAccountManager getInstance(Context context) {
        if (instance == null)
            instance = new UserAccountManager(context);
        return instance;
    }

    // Constructor
    private UserAccountManager(Context context){
        this.context = context;
        pref = new SecurePreferences(context, "Android-OneSpace-SeSaMe", PREF_NAME);
        editor = pref.edit();
    }


    public synchronized void storeXmppServer(String xmppServer) {
        SettingsManager.connectionSettingsObsolete = true;
        editor.putString(KEY_XMPP_SERVER, xmppServer);
        editor.commit();
    }

    public synchronized void storeXmppBosh(String xmppBosh) {
        SettingsManager.connectionSettingsObsolete = true;
        editor.putString(KEY_XMPP_SERVER, xmppBosh);
        editor.commit();
    }

    public synchronized void storeUsername(String username) {
        SettingsManager.connectionSettingsObsolete = true;
        editor.putString(KEY_USERNAME, username);
        editor.commit();
    }

    public synchronized void storePassword(String password) {
        SettingsManager.connectionSettingsObsolete = true;
        editor.putString(KEY_PASSWORD, password);
        editor.commit();
    }

    public synchronized void storeUserID(String userid) {
        SettingsManager.connectionSettingsObsolete = true;
        editor.putString(KEY_USERID, userid);
        editor.commit();
    }

    public String getUserID() {
        return pref.getString(KEY_USERID, "");
    }

    public String getXmppServer() {
        return pref.getString(KEY_XMPP_SERVER, "");
    }

    public String getXmppBosh() {
        return pref.getString(KEY_XMPP_BOSH, "");
    }

    public String getUsername() {
        return pref.getString(KEY_USERNAME, "");
    }

    public String getPassword() {
        return pref.getString(KEY_PASSWORD, "");
    }

    public void logoutUser() {
        SettingsManager.connectionSettingsObsolete = true;
        editor.clear();
        editor.commit();
    }

    public synchronized void setAsCreateNewAccount(boolean isCreateAccount) {
        SettingsManager.connectionSettingsObsolete = true;
        editor.putBoolean(IS_CREATE_ACCOUNT, isCreateAccount);
        editor.commit();
    }

    public boolean isCreateNewAccount() {
        return pref.getBoolean(IS_CREATE_ACCOUNT, false);
    }

    public synchronized void setLoggedIn() {
        editor.putBoolean(IS_LOGIN, true);
        editor.putBoolean(IS_CREATE_ACCOUNT, false);
        editor.commit();
    }

    public boolean isLoggedIn(){
        return pref.getBoolean(IS_LOGIN, false);
    }


}
