package com.sesame.onespace.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.sesame.onespace.R;
import com.sesame.onespace.fragments.LoginFragment;
import com.sesame.onespace.fragments.SignUpFragment;
import com.sesame.onespace.managers.SettingsManager;
import com.sesame.onespace.managers.UserAccountManager;
import com.sesame.onespace.network.OneSpaceApi;
import com.sesame.onespace.service.MessageService;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit.GsonConverterFactory;
import retrofit.RxJavaCallAdapterFactory;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;


/**
 * Created by chongos on 8/4/15 AD.
 */
public class LoginActivity extends AppCompatActivity implements
        LoginFragment.OnLoginFragmentInteractionListener,
        SignUpFragment.OnSignupFragmentInteractionListener {

    public static final String ACTION_LOGIN_STATUS = "connect_and_auth";
    public static final String KEY_LOGIN_STATUS = "connect_auth_status";
    public static final String KEY_LOGIN_MESSAGE = "connect_auth_message";
    public static final int CONNECTED = 1;
    public static final int AUTHENED = 2;
    public static final int SIGNED_UP = 3;
    public static final int CONNECT_FAIL = 99;
    public static final int AUTHEN_FAIL = 98;
    public static final int SIGNUP_FAIL = 97;

    private LoginFragment loginFragment;
    private SignUpFragment signUpFragment;
    private SettingsManager settingsManager;
    private UserAccountManager userAccountManager;
    private Subscription getUserIDSubscription;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            int status = intent.getIntExtra(KEY_LOGIN_STATUS, 0);
            String message = intent.getStringExtra(KEY_LOGIN_MESSAGE);
            if (status == AUTHEN_FAIL) {
                if(loginFragment.isVisible()) {
                    loginFragment.showProgress(false);
                    loginFragment.showSnackbar("Login Fail, Please try again.");
                }
            } else if (status == AUTHENED) {
                getUserID();
            } else if (status == SIGNUP_FAIL) {
                if(signUpFragment.isVisible()) {
                    signUpFragment.showProgress(false);
                    signUpFragment.showSnackbar("Create account fail.\n" + message);
                }
            } else if (status == CONNECT_FAIL) {
                if(loginFragment.isVisible()) {
                    loginFragment.showProgress(false);
                    loginFragment.showSnackbar(getString(R.string.error_connection_fail));
                } else if(signUpFragment.isVisible()) {
                    signUpFragment.showProgress(false);
                    signUpFragment.showSnackbar(getString(R.string.error_connection_fail));
                }
                stopMessageService();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settingsManager = SettingsManager.getSettingsManager(getApplicationContext());
        userAccountManager = settingsManager.getUserAccountManager();

        loginFragment = LoginFragment.newInstance();
        signUpFragment = SignUpFragment.newInstance();

        IntentFilter intentFiler = new IntentFilter(ACTION_LOGIN_STATUS);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFiler);

        onOpenLogin();
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

    private void setFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(android.R.id.content, fragment);
        transaction.commit();
    }

    private void startLogin(final String username, final String password, final boolean isCreateAccount) {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                if(loginFragment.isVisible())
                    loginFragment.showProgress(true);
                else if(signUpFragment.isVisible())
                    signUpFragment.showProgress(true);
            }

            @Override
            protected Void doInBackground(Void... params) {
                userAccountManager.storeUsername(username);
                userAccountManager.storePassword(password);
                userAccountManager.setAsCreateNewAccount(isCreateAccount);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if(isNetworkConnected())
                    startMessageService();
                else {
                    String errorMsg = getString(R.string.error_no_internet_connection);
                    String actionStr = "Go to Setting";
                    View.OnClickListener onClickListener = new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent callGPSSettingIntent = new Intent(
                                    Settings.ACTION_WIFI_SETTINGS);
                            startActivity(callGPSSettingIntent);
                        }
                    };

                    if(loginFragment.isVisible()) {
                        loginFragment.showProgress(false);
                        loginFragment.showSnackbar(errorMsg, actionStr, onClickListener);
                    } else if(signUpFragment.isVisible()) {
                        signUpFragment.showProgress(false);
                        signUpFragment.showSnackbar(errorMsg, actionStr, onClickListener);
                    }
                }
            }
        }.execute();

    }

    private boolean isNetworkConnected() {
        try{
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null &&
                    activeNetwork.isConnectedOrConnecting();
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void startMessageService() {
        Intent intent = new Intent(getApplicationContext(), MessageService.class);
        intent.setAction(MessageService.ACTION_CONNECT);
        startService(intent);
    }

    private void stopMessageService() {
        if(MessageService.isRunning)
            stopService(new Intent(getApplicationContext(), MessageService.class));
    }

    private void getUserID() {
        OneSpaceApi.Service service = new OneSpaceApi.Builder(getApplicationContext())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();

        Observable<String> observable = service.loginRx(
                userAccountManager.getUsername(),
                userAccountManager.getPassword(),
                userAccountManager.getUsername() + "@" + settingsManager.xmppServiceName,
                settingsManager.xmppRecource);

        getUserIDSubscription = observable.observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {

                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        if(loginFragment.isVisible()) {
                            loginFragment.showProgress(false);
                            loginFragment.showSnackbar(getString(R.string.error_server_unavailble));
                        } else if(signUpFragment.isVisible()) {
                            signUpFragment.showProgress(false);
                            signUpFragment.showSnackbar(getString(R.string.error_server_unavailble));
                        }
                        stopMessageService();
                    }

                    @Override
                    public void onNext(String userid) {
                        if (!userid.equals("")) {
                            try {
                                JSONObject jsonObject = new JSONObject(userid);
                                userAccountManager.storeUserID(jsonObject.getString("id"));
                                userAccountManager.setLoggedIn();
                                Intent i = new Intent(LoginActivity.this, MainActivity.class);
                                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(i);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View f = getCurrentFocus();
        if(f != null && f.getWindowToken() != null && EditText.class.isAssignableFrom(f.getClass())) {
            imm.hideSoftInputFromWindow(f.getWindowToken(), 0);
        } else {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }
    }

    @Override
    public void onLogin(String username, String password) {
        hideKeyboard();
        startLogin(username, password, false);
    }

    @Override
    public void onOpenSignup() {
        setFragment(signUpFragment);
    }

    @Override
    public void onOpenLogin() {
        setFragment(loginFragment);
    }

    @Override
    public void onSignup(String username, String password) {
        hideKeyboard();
        startLogin(username, password, true);
    }

    @Override
    public void onCancel() {
        stopMessageService();
        if(getUserIDSubscription != null)
            getUserIDSubscription.unsubscribe();
    }

}
