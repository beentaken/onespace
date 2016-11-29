package com.sesame.onespace.activities;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.customtabs.CustomTabsCallback;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsServiceConnection;
import android.support.customtabs.CustomTabsSession;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.google.android.gms.maps.model.LatLng;
import com.sesame.onespace.R;
import com.sesame.onespace.fragments.ChatRoomFragment;
import com.sesame.onespace.fragments.ChatListFragment;
import com.sesame.onespace.fragments.MainMenuFragment;
import com.sesame.onespace.managers.UserAccountManager;
import com.sesame.onespace.managers.location.UserLocationManager;
import com.sesame.onespace.managers.chat.ChatHistoryManager;
import com.sesame.onespace.managers.map.LocationPreferencesManager;
import com.sesame.onespace.managers.service.GPSServiceManager;
import com.sesame.onespace.models.chat.Chat;
import com.sesame.onespace.models.chat.ChatMessage;
import com.sesame.onespace.network.OneSpaceApi;
import com.sesame.onespace.service.MessageService;
import com.sesame.onespace.service.xmpp.Tools;
import com.sesame.onespace.utils.Log;

import retrofit.GsonConverterFactory;
import retrofit.RxJavaCallAdapterFactory;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by chongos on 7/31/15 AD.
 *
 */

// Modified code by Thianchai on 16/10/16

public class MainActivity extends AppCompatActivity implements
        MainMenuFragment.OnNewMenuFragmentInteractionListener,
        ChatListFragment.OnChatListInteractionListener,
        ChatRoomFragment.OnChatFragmentInteractionListener {

    //Thianchai (I delete this listener) : I think it is not necessary to use it.
    //OnLocationUpdatedListener
    //**

    //===========================================================================================================//
    //  ATTRIBUTE                                                                                   ATTRIBUTE
    //===========================================================================================================//

    private MainMenuFragment mainMenuFragment;
    private LocationPreferencesManager preferencesManager;
    private UserAccountManager userAccountManager;
    private BroadcastReceiver mReceiver;
    private CustomTabsServiceConnection customTabsConnection;
    private CustomTabsSession customTabsSession;

    private ChatRoomFragment chatRoomFragment;

    private Chat currentSelectedChat;
    private int connectionStatus;

    //Thianchai (I delete this) : I think it is not necessary to use it because I use UserLocationManager instead.
    //private double latitude = 0;
    //private double longitude = 0;
    //**

    //Thianchai (I add this)
    private Context context;
    //**

    //Thianchai (I add this)
    private GPSBroadcastReceiver broadcastReceiver = new GPSBroadcastReceiver();
    private GPSBroadcastReceiver broadcastReceiver2 =  new GPSBroadcastReceiver();
    //**

    private boolean fromMap;

    //===========================================================================================================//
    //  ACTIVITY LIFECYCLE                                                                          ACTIVITY LIFECYCLE
    //===========================================================================================================//

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userAccountManager = UserAccountManager.getInstance(getApplicationContext());
        preferencesManager = LocationPreferencesManager.getPreferencesManager(getApplicationContext());
        mainMenuFragment = MainMenuFragment.newInstance(userAccountManager.getUsername());

        connectCustomTabsService();
        connectionStatus = MessageService.getConnectionStatus();

        setFragment(mainMenuFragment, false);
        checkIntent(getIntent());

        //Thianchai (I add this)
        this.myInit();
        //**

        //Thianchai (I add this)
        registerReceiver(this.broadcastReceiver2, new IntentFilter("GPSTrackerService"));
        //**

        //Thianchai (I add this)
        if (GPSServiceManager.isGPSServiceRunning(this.context)){

            Intent intent = new Intent();
            intent.setAction("GPSTrackerService2");
            intent.putExtra("want location again!!!", "yes");
            sendBroadcast(intent);

        }
        //**

    }

    @Override
    protected void onStart() {
        super.onStart();

        //Thianchai (I deleate this) : I think it is not necessary to use it.
//        if(SmartLocation.with(this).location().state().locationServicesEnabled()) {
//            SmartLocation.with(this)
//                    .location()
//                    .config(LocationParams.BEST_EFFORT)
//                    .start(this);
//            if(mainMenuFragment != null && mainMenuFragment.isVisible())
//                mainMenuFragment.locationServiceAvailable(true);
//        } else if(mainMenuFragment != null) {
//            mainMenuFragment.locationServiceAvailable(false);
//        }
        //**

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals(MessageService.ACTION_XMPP_MESSAGE_RECEIVED)) {
                    ChatMessage chatMessage = intent.getParcelableExtra(MessageService.KEY_BUNDLE_CHAT_MESSAGE);
                    String chatID = chatMessage.getChatID();
                    if (chatRoomFragment != null && chatRoomFragment.isVisible()) {
                        if(chatID.equals(currentSelectedChat.getId()))
                            chatRoomFragment.addMessage(chatMessage);
                    } else {
                        updateChatList(chatID);
                    }
                } else if(intent.getAction().equals(MessageService.ACTION_XMPP_CONNECTION_CHANGED)) {
                    connectionStatus = intent.getIntExtra(MessageService.KEY_BUNDLE_NEW_STATE, -1);
                    if(chatRoomFragment != null && chatRoomFragment.isVisible())
                        chatRoomFragment.setConncetionStatus(connectionStatus);
                    if(mainMenuFragment != null && mainMenuFragment.isVisible())
                        mainMenuFragment.setConncetionStatus(connectionStatus);
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(MessageService.ACTION_XMPP_CONNECTION_CHANGED);
        filter.addAction(MessageService.ACTION_XMPP_MESSAGE_RECEIVED);
        filter.addAction(MessageService.ACTION_XMPP_MESSAGE_SENT);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, filter);

    }

    @Override
    protected void onResume(){
        super.onResume();

        //Thianchai (I add this)
        registerReceiver(this.broadcastReceiver, new IntentFilter("GPSTrackerService"));
        //**

        //Thianchai (I add this)
        if (!(GPSServiceManager.isGPSServiceRunning(this.context))){

            GPSServiceManager.startGPSService(this.context, userAccountManager.getUserID());

        }
        //**

    }

    @Override
    public void onBackPressed() {
        if(chatRoomFragment != null && chatRoomFragment.isVisible()) {
            hideKeyboard();
            chatRoomFragment = null;
            if(fromMap) {
                onOpenMap();
                fromMap = false;
            }
        }
        super.onBackPressed();
    }

    @Override
    protected void onStop() {
        super.onStop();

        //Thianchai (I add this)
        unregisterReceiver(this.broadcastReceiver);
        //**

        //Thianchai (I delete this)
        //SmartLocation.with(this).location().stop();
        //**

        //Thianchai (I delete this)
//        if(Geocoder.isPresent())
//            SmartLocation.with(this).geocoding().stop();
        //**

        preferencesManager.saveLocation(new LatLng(UserLocationManager.getLatitude(), UserLocationManager.getLongitude()));
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //Thianchai (I add this)
        unregisterReceiver(this.broadcastReceiver2);
        //**

        if (customTabsConnection != null)
            unbindService(customTabsConnection);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        checkIntent(intent);
    }

    //===========================================================================================================//
    //  METHOD BY chongos                                                                           METHOD BY chongos
    //===========================================================================================================//

    private void checkIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        if(extras != null) {
            extras.setClassLoader(Chat.class.getClassLoader());
            currentSelectedChat = extras.getParcelable(MessageService.KEY_BUNDLE_CHAT);
            fromMap = extras.getBoolean("from_map", false);
        }
        if(currentSelectedChat != null) {
            onOpenChat(currentSelectedChat);
        }
    }

    private void setFragment(Fragment fragment, boolean addToBackStack) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        // Set Animation
        if(fragment instanceof ChatRoomFragment)
            fragmentTransaction.setCustomAnimations(R.anim.slide_in_from_right, 0, R.anim.zoom_in, R.anim.slide_out_to_right);

        if(addToBackStack)
            fragmentTransaction.addToBackStack(fragment.getClass().getName());

        fragmentTransaction.add(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }

    /**
     * This method used for put location to server
     * @param location location object that contain Latitude and Longitude
     */
    private void putLocationToServer(Location location) {
        String userid = userAccountManager.getUserID();
        Observable<String> observable = new OneSpaceApi.Builder(getApplicationContext())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build()
                .updateGeoLocationRx(userid,
        location.getLatitude(),
        location.getLongitude());

        observable.subscribe(new Subscriber<String>() {
            @Override
            public void onCompleted() {
                Log.i("PutLocation completed");
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onNext(String s) {

            }
        });
    }

    /**
     * For binding CustomChromeTabService
     */
    public void connectCustomTabsService() {
        String chromePackageName = "com.android.chrome";
        customTabsConnection = new CustomTabsServiceConnection() {
            @Override
            public void onServiceDisconnected(ComponentName name) {
                // Disconnect Custom Tabs Service
            }

            @Override
            public void onCustomTabsServiceConnected(ComponentName componentName, CustomTabsClient customTabsClient) {
                // Custom Tabs Service connected
                createCustomTabsSession(customTabsClient);
            }
        };
        CustomTabsClient.bindCustomTabsService(this, chromePackageName, customTabsConnection);
    }

    /**
     * This method used for create CustomChromeTab Session
     * @param customTabsClient CustomChromeTab Object
     */
    public void createCustomTabsSession(CustomTabsClient customTabsClient) {
        customTabsSession = customTabsClient.newSession(new CustomTabsCallback() {
            @Override
            public void onNavigationEvent(int navigationEvent, Bundle extras) {

            }
        });
    }

    //Thianchai (I delete this)
//    @Override
//    public void onLocationUpdated(Location location) {
//
        // old code by chongos
//        latitude = location.getLatitude();
//        longitude = location.getLongitude();
//
//        Log.i("Location updated: " + latitude + ", " + longitude);
//        putLocationToServer(location);
//
//        if(mainMenuFragment != null) {
//            mainMenuFragment.locationServiceAvailable(true);
//            mainMenuFragment.setLocation(location);
//
//            SmartLocation.with(this).geocoding()
//                    .reverse(location, new OnReverseGeocodingListener() {
//
//                        @Override
//                        public void onAddressResolved(Location location, List<Address> list) {
//                            if (list.size() > 0)
//                                mainMenuFragment.setLocationAddress(list.get(0));
//                        }
//                    });
//        }


        //wrote by Thianchai
//        latitude = location.getLatitude();
//        longitude = location.getLongitude();
//        putLocationToServer(location);

//        if(mainMenuFragment != null) {
//
//            mainMenuFragment.locationServiceAvailable(true);
//            mainMenuFragment.setLocation(location);
//
//            SmartLocation.with(this).geocoding()
//                    .reverse(location, new OnReverseGeocodingListener() {
//
//                        @Override
//                        public void onAddressResolved(Location location, List<Address> list) {
//                            if (list.size() > 0)
//                                mainMenuFragment.setLocationAddress(latitude + ", " + longitude, list.get(0));
//                        }
//                    });
//
//        }
//
//        try{
//
//            MapActivity.updateCornerLocation();
//
//        }catch (Exception e){
//
//
//
//        }
//
//
//    }
    //**

    @Override
    public void onOpenMap() {
        startActivity(MapActivity.class);
    }

    @Override
    public void onCreateUserCorner() {
        Intent intent = new Intent(getApplicationContext(), MapActivity.class);
        intent.putExtra(MapActivity.KEY_CREATE_CORNER, true);
        startActivity(intent);
    }

    @Override
    public void onOpenChat(Chat chat) {
        currentSelectedChat = chat;
        if(chatRoomFragment != null && chatRoomFragment.isVisible())
            getSupportFragmentManager().popBackStack();
        chatRoomFragment = ChatRoomFragment.newInstance(chat, connectionStatus);
        setFragment(chatRoomFragment, true);
    }

    @Override
    public void onRemoveChat(final Chat chat) {
        if(chat.getType() == Chat.Type.GROUP)
            onLeaveGroup(chat);
        else {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    ChatHistoryManager.getInstance(getApplicationContext()).deleteChat(chat);
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);
                    mainMenuFragment.removeChat(chat);
                }
            }.execute();
        }
    }

    @Override
    public void onSendMessage(ChatMessage message) {
        Tools.sendToService(getApplicationContext(),
                MessageService.ACTION_XMPP_MESSAGE_SEND, message);
    }

    @Override
    public void onLeaveGroup(Chat chat) {
        Tools.sendToService(getApplicationContext(),
                MessageService.ACTION_XMPP_GROUP_LEAVE,
                chat);
        if(mainMenuFragment != null)
            mainMenuFragment.removeChat(chat);
    }

    @Override
    public void onOpenLink(String url) {
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder(customTabsSession);
        builder.setShowTitle(true);
        builder.setToolbarColor(getResources().getColor(R.color.color_primary));
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.launchUrl(this, Uri.parse(url));
    }

    @Override
    public void onChatUpdated(String chatID) {
        if(mainMenuFragment != null)
            updateChatList(chatID);
    }

    /**
     * Get a new Chat Object from ChatHistoryManager in background
     * and update to MainMenuFragment
     * @param chatID
     */
    private void updateChatList(final String chatID) {
        new AsyncTask<Void, Void, Chat>() {

            @Override
            protected Chat doInBackground(Void... params) {
                return ChatHistoryManager.getInstance(getApplicationContext()).getChat(chatID);
            }

            @Override
            protected void onPostExecute(Chat chat) {
                super.onPostExecute(chat);
                mainMenuFragment.updateChat(chat);
            }

        }.execute();
    }

    private void startActivity(Class toClass) {
        Intent i = new Intent(MainActivity.this, toClass);
        startActivity(i);
    }

    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).
                    hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    //===========================================================================================================//
    //  METHOD BY Thianchai                                                                         METHOD BY Thianchai
    //===========================================================================================================//

    private void myInit(){

        this.context = getApplicationContext();

        this.myInitJSONConnect();

    }

    private void myInitJSONConnect(){

        //ThreadPolicy
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

    }

    //===========================================================================================================//
    //  PRIVATE CLASS                                                                               PRIVATE CLASS
    //===========================================================================================================//

    private class GPSBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            Bundle b = intent.getExtras();

            UserLocationManager.setLatitude(b.getDouble("latitude", 0));
            UserLocationManager.setLongitude(b.getDouble("longitude", 0));

            mainMenuFragment.locationServiceAvailable(true);

            mainMenuFragment.locationServiceAvailable(true);
            mainMenuFragment.setLocation(UserLocationManager.getLocation());

            try{

                mainMenuFragment.setLocationAddress(UserLocationManager.getLatitude() + ", " + UserLocationManager.getLongitude(), UserLocationManager.getAddress(context));

            }catch (Exception e){



            }

        }

    }

}
