package com.sesame.onespace.activities;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.RingtonePreference;
import android.preference.SwitchPreference;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.sesame.onespace.R;
import com.sesame.onespace.databases.Database;
import com.sesame.onespace.fragments.SettingsListFragment;
import com.sesame.onespace.managers.LogoutManager;
import com.sesame.onespace.managers.SettingsManager;
import com.sesame.onespace.managers.UserAccountManager;
import com.sesame.onespace.utils.Log;

import java.util.ArrayList;
import java.util.List;


public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String KEY_SETTING_ITEM = "setting_item";
    public static final int FRAGMENT_ACCOUNT = 1;
    public static final int FRAGMENT_NOTIFICATON = 2;
    public static final int FRAGMENT_CHAT = 3;
    public static final int FRAGMENT_MAP = 4;
    public static final int FRAGMENT_ADVANCE = 5;
    public static final int FRAGMENT_ABOUT = 6;
    public static final int FRAGMENT_HELP = 7;

    private static Context context;
    private static List<String> fragments = new ArrayList<String>();
    private static SettingsManager settingsManager;
    private static ArrayList<ListenedChangedPreferenceFragment> listenedChangedPreferenceFragments = new ArrayList<>();
    private Toolbar mToolBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = getApplicationContext();
        ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
        View content = root.getChildAt(0);
        LinearLayout toolbarContainer = (LinearLayout) View.inflate(this, R.layout.fragment_settings, null);

        root.removeAllViews();
        toolbarContainer.addView(content);
        root.addView(toolbarContainer);

        mToolBar = (Toolbar) toolbarContainer.findViewById(R.id.toolbar);
        mToolBar.setNavigationIcon(R.drawable.ic_arrow_back);
        mToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        settingsManager = SettingsManager.getSettingsManager(context);

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            SettingsListFragment.SettingItem item = extras.getParcelable(KEY_SETTING_ITEM);
            if(item == null)
                return;

            PreferenceFragment fragment = null;
            switch (item.getFragmentID()) {
                case FRAGMENT_ACCOUNT:
                    fragment = new AccountsPreferenceFragment();
                    break;
                case FRAGMENT_NOTIFICATON:
                    fragment = new NotificationPreferenceFragment();
                    break;
                case FRAGMENT_CHAT:
                    fragment = new ChatPreferenceFragment();
                    break;
                case FRAGMENT_MAP:
                    fragment = new MapPreferenceFragment();
                    break;
                case FRAGMENT_ADVANCE:
                    fragment = new AdvancedPreferenceFragment();
                    break;
                case FRAGMENT_ABOUT:
                    fragment = new AboutPreferenceFragment();
                    break;
                case FRAGMENT_HELP:
                    fragment = new HelpPreferenceFragment();
                    break;
            }

            if(fragment != null) {
                mToolBar.setTitle(item.getTitle());
                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commit();
            }
        }
    }

    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.setting_headers, target);
        fragments.clear();
        for (Header header : target) {
            fragments.add(header.fragment);
        }
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return fragments.contains(fragmentName);
    }

    @Override
    protected void onResume() {
        super.onResume();
        settingsManager.registerOnSharedPreferenceChanged(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        settingsManager.unregisterOnSharedPreferenceChanged(this);
    }

    public static void regisOnFragmentPreferenceChangedListener(ListenedChangedPreferenceFragment listener) {
        listenedChangedPreferenceFragments.add(listener);
    }

    public static void unregisOnFragmentPreferenceChangedListener(ListenedChangedPreferenceFragment listener) {
        listenedChangedPreferenceFragments.remove(listener);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences preference, String key) {
        for (ListenedChangedPreferenceFragment listener : listenedChangedPreferenceFragments) {
            listener.onPreferenceChanged(key);
        }
    }

    public static class ListenedChangedPreferenceFragment extends PreferenceFragment {

        private String id;

        public void setId(String id) {
            this.id = id;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            regisOnFragmentPreferenceChangedListener(this);
        }

        @Override
        public void onDestroy() {
            unregisOnFragmentPreferenceChangedListener(this);
            super.onDestroy();
        }

        public void onPreferenceChanged(String key) {
            String fragmentID = key.split("_")[1];
            if(fragmentID.equals(id))
                setPreferenceSummary(findPreference(key), key);
        }

        private void setPreferenceSummary(Preference preference, String key) {
            if (preference instanceof RingtonePreference) {
                String tone = settingsManager.getStringPreference(key, "default ringtone");
                Ringtone ringtone = RingtoneManager.getRingtone(preference.getContext(), Uri.parse(tone));
                if (ringtone == null) {
                    preference.setSummary(null);
                } else {
                    String name = ringtone.getTitle(preference.getContext());
                    preference.setSummary(name);
                }
            } else if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                listPreference.setSummary(listPreference.getEntry().toString());
            } else if (preference instanceof CheckBoxPreference) {

            } else if (preference instanceof SwitchPreference) {

            } else {
                preference.setSummary(settingsManager.getStringPreference(key, ""));
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class AccountsPreferenceFragment extends ListenedChangedPreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_accounts);
            setId("account");

            UserAccountManager accountManager = settingsManager.getUserAccountManager();
            findPreference(getString(R.string.pref_account_userid)).setSummary(accountManager.getUserID());
            findPreference(getString(R.string.pref_account_username)).setSummary(accountManager.getUsername());
            findPreference(getString(R.string.pref_account_logout)).setOnPreferenceClickListener(
                    new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            logoutConfirm();
                            return false;
                        }
                    });
        }

        private void logoutConfirm() {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getString(R.string.logout));
            builder.setMessage(getString(R.string.alert_confirm_logout));
            builder.setPositiveButton(getString(R.string.confirm_yes), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    LogoutManager.getInstance(getActivity()).logout();
                }
            });
            builder.setNegativeButton(getString(R.string.confirm_no), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.show();

        }

    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class NotificationPreferenceFragment extends ListenedChangedPreferenceFragment
            implements Preference.OnPreferenceChangeListener {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_notification);
            setId("notification");

            Preference ringtonePreference = findPreference(getString(R.string.pref_notification_tone));
            ringtonePreference.setOnPreferenceChangeListener(this);
            super.setPreferenceSummary(ringtonePreference, ringtonePreference.getKey());
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String tone = newValue.toString();
            settingsManager.saveStringSetting(preference.getKey(), tone);
            super.setPreferenceSummary(preference, preference.getKey());
            return false;
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class MapPreferenceFragment extends ListenedChangedPreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_map);
            setId("map");

            findPreference(getString(R.string.pref_map_google_location_setting)).setOnPreferenceClickListener(
                    new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            Intent callGPSSettingIntent = new Intent(
                                    android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(callGPSSettingIntent);
                            return false;
                        }
                    });
        }
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class ChatPreferenceFragment extends ListenedChangedPreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_chat);
            setId("chat");

            Preference fontSizePreference = findPreference(getString(R.string.pref_chat_font_size));
            super.setPreferenceSummary(fontSizePreference, fontSizePreference.getKey());

            findPreference(getString(R.string.pref_chat_clear_history)).setOnPreferenceClickListener(
                    new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setTitle(context.getString(R.string.pref_chat_clear_history_title));
                            builder.setMessage(getString(R.string.alert_confirm_clear_chat_history));
                            builder.setPositiveButton(getString(R.string.confirm_yes), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    Database.Cleaner.getCleaner(context).cleanChatHistory();
                                }
                            });
                            builder.setNegativeButton(getString(R.string.confirm_no), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
                            builder.show();
                            return false;
                        }
                    });
        }

    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class AdvancedPreferenceFragment extends ListenedChangedPreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_advanced);
            setId("advanced");

            findPreference(getString(R.string.pref_advanced_xmpp_server))
                    .setSummary(settingsManager.xmppServer);
            findPreference(getString(R.string.pref_advanced_xmpp_resource))
                    .setSummary(settingsManager.xmppRecource);
            findPreference(getString(R.string.pref_advanced_onespace_server))
                    .setSummary(settingsManager.onespaceServer);
            findPreference(getString(R.string.pref_advanced_onespace_port))
                    .setSummary(settingsManager.onespacePort);
        }
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class AboutPreferenceFragment extends ListenedChangedPreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_about);
            setId("about");
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class HelpPreferenceFragment extends ListenedChangedPreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_help);
            setId("help");
        }
    }
}
