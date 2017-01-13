package com.sesame.onespace.activities;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.sesame.onespace.R;
import com.sesame.onespace.constant.MapMarkerIconSelector;
import com.sesame.onespace.managers.SettingsManager;
import com.sesame.onespace.managers.location.UserLocationManager;
import com.sesame.onespace.network.CornerMarkerLoader;
import com.sesame.onespace.managers.map.LocationPreferencesManager;
import com.sesame.onespace.network.MapMarkerLoader;
import com.sesame.onespace.network.PlaceMarkerLoader;
import com.sesame.onespace.managers.UserAccountManager;
import com.sesame.onespace.network.SurferMarkerLoader;
import com.sesame.onespace.network.WalkerMarkerLoader;
import com.sesame.onespace.models.chat.Chat;
import com.sesame.onespace.models.map.Corner;
import com.sesame.onespace.models.map.FilterMarkerNode;
import com.sesame.onespace.models.map.Place;
import com.sesame.onespace.models.map.Surfer;
import com.sesame.onespace.models.map.Walker;
import com.sesame.onespace.network.OneSpaceApi;
import com.sesame.onespace.service.MessageService;
import com.sesame.onespace.utils.Log;
import com.sesame.onespace.views.CornerBottomSheet;
import com.sesame.onespace.views.OneSpaceMap;
import com.sesame.onespace.views.PlaceBottomSheet;
import com.sesame.onespace.views.SurferBottomSheet;
import com.sesame.onespace.views.WalkerBottomSheet;
import com.sesame.onespace.views.adapters.MapMarkersFilterListAdapter;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.squareup.otto.Subscribe;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.nlopez.smartlocation.SmartLocation;
import retrofit.GsonConverterFactory;
import retrofit.RxJavaCallAdapterFactory;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by chongos on 7/31/15 AD.
 */

// Modified code by Thianchai on 11/10/16
public class MapActivity
        extends AppCompatActivity
        implements OnMapReadyCallback,
                   GoogleMap.OnCameraChangeListener,
                   ClusterManager.OnClusterItemClickListener,
                   SlidingUpPanelLayout.PanelSlideListener,
                   View.OnClickListener {

    //Thianchai (I delete OnLocationUpdatedListener) : I think it is not necessary to use it.
    //OnLocationUpdatedListener
    //**

    //===========================================================================================================//
    //  ATTRIBUTE                                                                                   ATTRIBUTE
    //===========================================================================================================//

    public static final String KEY_CREATE_CORNER = "create_corner_here";

    private static final int SITE_LIMIT = 100;
    private static final int WALKER_LIMIT = 10;
    private static final int SURFER_LIMIT = 10;
    private static final int CORNER_LIMIT = 10;

    private Receiver mReceiver;
    private SettingsManager mSettingManager;
    private LocationPreferencesManager mPreferencesManager;
    private OneSpaceApi.Service mOneSpaceApiService;

    private SlidingUpPanelLayout mSlidingUpPanelLayout;
    private CoordinatorLayout mCoordinatorLayout;
    private DrawerLayout mDrawerLayout;
    private Toolbar mToolbar;
    private OneSpaceMap mOneSpaceMap;
    private FloatingActionButton mCurrentLocationFab;
    private FloatingActionButton mOpenCornerBarFab;
    private MapMarkersFilterListAdapter mExpListAdapter;
    private ExpandableListView mExpListView;
    private TextView mCornerLocation;
    private FloatingActionButton mCreateCornerButton;
    private RecyclerView mCornerListView;

    private SurferMarkerLoader mSurferLoader;
    private WalkerMarkerLoader mWalkerLoader;
    private PlaceMarkerLoader mPlaceLoader;
    private CornerMarkerLoader mCornerLoader;
    private FilterMarkerNode mRootFilterMarkerNode;

    private String userID;
    private String username;

    //Thianchai (I delete this) : I think it is not necessary to use it because I use UserLocationManager instead.
//    private double latitude = 0;
//    private double longitude = 0;
    //**

    //Thianchai (I add this)
    private GPSBroadcastReceiver broadcastReceiver = new GPSBroadcastReceiver();
    private Context context;
    //**

    private boolean locationAvailable;

    //===========================================================================================================//
    //  ACTIVITY LIFECYCLE                                                                          ACTIVITY LIFECYCLE
    //===========================================================================================================//

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        initInstances();

        mSettingManager = SettingsManager.getSettingsManager(getApplicationContext());
        MapMarkerLoader.Bus.getInstance().register(this);

        //Thianchai (I add this)
        this.init();
        //**

    }

    @Override
    protected void onStart() {
        super.onStart();
        mReceiver = new Receiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(MessageService.ACTION_XMPP_GROUP_JOIN);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (SmartLocation.with(this).location().state().locationServicesEnabled()) {

            //Thianchai (I delete this)
            //SmartLocation.with(this)
            //       .location()
            //       .config(LocationParams.BEST_EFFORT)
            //       .start(this);
            //**

            mCurrentLocationFab.setVisibility(View.VISIBLE);
            mOpenCornerBarFab.setVisibility(View.VISIBLE);
            locationAvailable = true;
        } else {
            locationServiceUnavailable();
            locationAvailable = false;
        }

        if(locationAvailable) {
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                if (extras.getBoolean(KEY_CREATE_CORNER)) {
                    mOpenCornerBarFab.setVisibility(View.GONE);
                    mCurrentLocationFab.setVisibility(View.GONE);
                    mCreateCornerButton.setVisibility(View.VISIBLE);
                    mOpenCornerBarFab.performClick();
                    collapseMap();
                }

            }
        }

        //Thianchai (I add this)
        mWalkerLoader.startThread();
        //**

        //Thianchai (I add this)
        mSurferLoader.startThread();
        //**

        //Thianchai (I add this)
        registerReceiver(this.broadcastReceiver, new IntentFilter("GPSTrackerService"));
        //**

    }

    @Override
    public void onBackPressed() {
        if(mDrawerLayout != null && mDrawerLayout.isDrawerOpen(GravityCompat.END))
            mDrawerLayout.closeDrawers();
        else if(mSlidingUpPanelLayout != null
                && (mSlidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED
                || mSlidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED))
            mSlidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        else if(mSlidingUpPanelLayout != null
                && mSlidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED)
            mSlidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
        else
            super.onBackPressed();
    }

    //Thianchai (I add this)
    @Override
    protected void onPause(){
        super.onPause();

        //Thianchai (I add this)
        mWalkerLoader.stopThread();
        //**

        //Thianchai (I add this)
        mSurferLoader.stopThread();
        //**

        //Thianchai (I add this)
        unregisterReceiver(this.broadcastReceiver);
        //**

    }
    //**

    @Override
    protected void onStop() {
        super.onStop();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);

        //Thianchai (I delete this)
//        SmartLocation.with(this)
//              .location()
//              .stop();
        //**

        mPreferencesManager.saveFilterCategoryStates(mRootFilterMarkerNode);
        mPreferencesManager.saveLocation(new LatLng(UserLocationManager.getLatitude(), UserLocationManager.getLongitude()));  //Thianchai (I modified this)
    }

    @Override
    protected void onDestroy() {
        MapMarkerLoader.Bus.getInstance().unregister(this);
        super.onDestroy();
    }

    //===========================================================================================================//
    //  METHOD BY chongos                                                                           METHOD BY chongos
    //===========================================================================================================//

    private void initInstances() {

        userID = UserAccountManager.getInstance(getApplicationContext()).getUserID();
        username = UserAccountManager.getInstance(getApplicationContext()).getUsername();
        mPreferencesManager = LocationPreferencesManager.getPreferencesManager(getApplicationContext());
        mOneSpaceApiService = new OneSpaceApi.Builder(getApplicationContext())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        mSlidingUpPanelLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mCurrentLocationFab = (FloatingActionButton) findViewById(R.id.fab_current_location);
        mOpenCornerBarFab = (FloatingActionButton) findViewById(R.id.fab_open_corner_bar);
        mExpListView = (ExpandableListView) findViewById(R.id.exp_list_view);
        mCornerLocation = (TextView) findViewById(R.id.corner_location);
        mCornerListView = (RecyclerView) findViewById(R.id.corner_list);
        mCreateCornerButton = (FloatingActionButton) findViewById(R.id.fab_create_corner);

        setSupportActionBar(mToolbar);
        mToolbar.setTitle(getTitle());
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mCurrentLocationFab.setOnClickListener(this);
        mOpenCornerBarFab.setOnClickListener(this);
        mCreateCornerButton.setOnClickListener(this);

        mSlidingUpPanelLayout.setPanelSlideListener(this);
        mSlidingUpPanelLayout.setScrollableView(mCornerListView);

        LatLng location = mPreferencesManager.getLocation();

        //Thianchai (I delete this)
        //latitude = location.latitude;
        //longitude = location.longitude;
        //**

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        try {
            mOneSpaceMap = new OneSpaceMap(getApplicationContext(), mapFragment.getMap());
            mOneSpaceMap.addOnCameraChangeListener(this);
            mOneSpaceMap.setOnClusterItemClick(this);
        } catch(NullPointerException e) {
            displayAlertDialog("Error", getString(R.string.error_missing_google_play_services),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            finish();
                        }
                    });
        }

        initFilter();

    }

    private void initFilter() {
        MapMarkerIconSelector mapMarkerIconSelector = MapMarkerIconSelector.getInstance(getApplicationContext());

        FilterMarkerNode surferFilter = new FilterMarkerNode(getString(R.string.display_surfers),
                R.drawable.ic_pin_surfer, R.drawable.ic_pin_surfer_grey);
        FilterMarkerNode walkerFilter = new FilterMarkerNode(getString(R.string.display_walkers),
                R.drawable.ic_pin_walker, R.drawable.ic_pin_walker_grey);
        FilterMarkerNode cornerFilter = new FilterMarkerNode(getString(R.string.display_corners),
                R.drawable.ic_expand_more_blue, R.drawable.ic_pin_corner_grey);
        cornerFilter.addSubCategory(new FilterMarkerNode(getString(R.string.display_corners_my),
                mapMarkerIconSelector.getCornerMarker(true), R.drawable.ic_pin_corner_red_dot_grey));
        cornerFilter.addSubCategory(new FilterMarkerNode(getString(R.string.display_corners_other),
                mapMarkerIconSelector.getCornerMarker(false), R.drawable.ic_pin_corner_grey_sub));
        FilterMarkerNode placeFilter = new FilterMarkerNode(getString(R.string.display_places),
                R.drawable.ic_expand_more_blue, R.drawable.ic_place_grey);
        placeFilter.addSubCategory(new FilterMarkerNode(getString(R.string.display_places_accommodation),
                mapMarkerIconSelector.getPlaceMarker(getString(R.string.display_places_accommodation)), R.drawable.ic_place_null));
        placeFilter.addSubCategory(new FilterMarkerNode(getString(R.string.display_places_amusement),
                mapMarkerIconSelector.getPlaceMarker(getString(R.string.display_places_amusement)), R.drawable.ic_place_null));
        placeFilter.addSubCategory(new FilterMarkerNode(getString(R.string.display_places_education),
                mapMarkerIconSelector.getPlaceMarker(getString(R.string.display_places_education)), R.drawable.ic_place_null));
        placeFilter.addSubCategory(new FilterMarkerNode(getString(R.string.display_places_establishment),
                mapMarkerIconSelector.getPlaceMarker(getString(R.string.display_places_establishment)), R.drawable.ic_place_null));
        placeFilter.addSubCategory(new FilterMarkerNode(getString(R.string.display_places_finance),
                mapMarkerIconSelector.getPlaceMarker(getString(R.string.display_places_finance)), R.drawable.ic_place_null));
        placeFilter.addSubCategory(new FilterMarkerNode(getString(R.string.display_places_food),
                mapMarkerIconSelector.getPlaceMarker(getString(R.string.display_places_food)), R.drawable.ic_place_null));
        placeFilter.addSubCategory(new FilterMarkerNode(getString(R.string.display_places_government),
                mapMarkerIconSelector.getPlaceMarker(getString(R.string.display_places_government)), R.drawable.ic_place_null));
        placeFilter.addSubCategory(new FilterMarkerNode(getString(R.string.display_places_health_and_wellness),
                mapMarkerIconSelector.getPlaceMarker(getString(R.string.display_places_health_and_wellness)), R.drawable.ic_place_null));
        placeFilter.addSubCategory(new FilterMarkerNode(getString(R.string.display_places_place_of_worship),
                mapMarkerIconSelector.getPlaceMarker(getString(R.string.display_places_place_of_worship)), R.drawable.ic_place_null));
        placeFilter.addSubCategory(new FilterMarkerNode(getString(R.string.display_places_recreation),
                mapMarkerIconSelector.getPlaceMarker(getString(R.string.display_places_recreation)), R.drawable.ic_place_null));
        placeFilter.addSubCategory(new FilterMarkerNode(getString(R.string.display_places_service),
                mapMarkerIconSelector.getPlaceMarker(getString(R.string.display_places_service)), R.drawable.ic_place_null));
        placeFilter.addSubCategory(new FilterMarkerNode(getString(R.string.display_places_shopping),
                mapMarkerIconSelector.getPlaceMarker(getString(R.string.display_places_shopping)), R.drawable.ic_place_null));
        placeFilter.addSubCategory(new FilterMarkerNode(getString(R.string.display_places_transport),
                mapMarkerIconSelector.getPlaceMarker(getString(R.string.display_places_transport)), R.drawable.ic_place_null));

        mRootFilterMarkerNode = new FilterMarkerNode();
        mRootFilterMarkerNode.addSubCategory(surferFilter);
        mRootFilterMarkerNode.addSubCategory(walkerFilter);
        mRootFilterMarkerNode.addSubCategory(cornerFilter);
        mRootFilterMarkerNode.addSubCategory(placeFilter);
        mPreferencesManager.loadFilterCategoryStates(mRootFilterMarkerNode);

        mExpListAdapter = new MapMarkersFilterListAdapter(this, mExpListView, mRootFilterMarkerNode);
        mExpListView.setAdapter(mExpListAdapter);
        mExpListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {

            @Override
            public boolean onGroupClick(ExpandableListView parent, View v,
                                        int groupPosition, long id) {
                FilterMarkerNode cat = mRootFilterMarkerNode.getSubCategory(groupPosition);
                cat.setSelected(!cat.isSelected());
                mExpListAdapter.notifyDataSetChanged();
                return false;
            }
        });
        mExpListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                FilterMarkerNode subcat = mRootFilterMarkerNode.getSubCategory(groupPosition)
                        .getSubCategory(childPosition);
                subcat.setSelected(!subcat.isSelected());
                mExpListAdapter.notifyDataSetChanged();
                return false;
            }
        });

        for(int i=0; i< mRootFilterMarkerNode.getSubCategorySize(); i++) {
            if(mRootFilterMarkerNode.getSubCategory(i).isSelected())
                mExpListView.expandGroup(i);
            else
                mExpListView.collapseGroup(i);
        }

        mPlaceLoader = new PlaceMarkerLoader(MapActivity.this, mOneSpaceApiService, MapMarkerLoader.Bus.getInstance());
        mPlaceLoader.setFilter(placeFilter);

        mWalkerLoader = new WalkerMarkerLoader(MapActivity.this, mOneSpaceApiService, MapMarkerLoader.Bus.getInstance());
        mWalkerLoader.setFilter(walkerFilter);
        mWalkerLoader.setUsername(username);

        mSurferLoader = new SurferMarkerLoader(MapActivity.this, mOneSpaceApiService, MapMarkerLoader.Bus.getInstance());
        mSurferLoader.setFilter(surferFilter);

        mCornerLoader = new CornerMarkerLoader(MapActivity.this, mOneSpaceApiService, MapMarkerLoader.Bus.getInstance());
        mCornerLoader.setUserID(userID);
        mCornerLoader.setRecyclerView(mCornerListView);
        mCornerLoader.setFilter(cornerFilter);

        //Thianchai (I add this)
        WalkerMarkerLoader.setIsFilter(walkerFilter.isSelected());
        SurferMarkerLoader.setIsFilter(surferFilter.isSelected());
        //**

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.open_filter:
                if (mDrawerLayout.isDrawerOpen(GravityCompat.END))
                    mDrawerLayout.closeDrawers();
                else
                    mDrawerLayout.openDrawer(GravityCompat.END);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        map.setMyLocationEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(UserLocationManager.getLatitude(), UserLocationManager.getLongitude()), 11f));  //Thianchai (I modified this)
    }

    //Thianchai (I delete this)
//    @Override
//    public void onLocationUpdated(Location location) {
//
//
//        latitude = location.getLatitude();
//        longitude = location.getLongitude();
//
//        mCornerLocation.setText(getLocationAsString());
//        putLocationToServer(location);
//
//
//    }
    //**

    @Override
    public void onCameraChange(CameraPosition position) {
        LatLngBounds latLngBounds = mOneSpaceMap.getProjection().getVisibleRegion().latLngBounds;
        mPlaceLoader.fetch(latLngBounds, SITE_LIMIT);
        mWalkerLoader.fetch(latLngBounds, WALKER_LIMIT);
        mSurferLoader.fetch(latLngBounds, SURFER_LIMIT);
        mCornerLoader.fetch(latLngBounds, CORNER_LIMIT);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_create_corner:
                createCorner();
                break;
            case R.id.fab_open_corner_bar:
                mSlidingUpPanelLayout.setAnchorPoint(0.55f);
                mSlidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
                break;
            case R.id.fab_current_location:
                mOneSpaceMap.animateCamera(CameraUpdateFactory.newLatLng(
                        new LatLng(UserLocationManager.getLatitude(), UserLocationManager.getLongitude())));  //Thianchai (I modified this)
                break;
        }
    }

    @Override
    public boolean onClusterItemClick(ClusterItem clusterItem) {
        if (clusterItem instanceof Place)
            new PlaceBottomSheet(this, (Place) clusterItem).show();
        else if(clusterItem instanceof Walker)
            new WalkerBottomSheet(this, (Walker) clusterItem).show();
        else if(clusterItem instanceof Surfer)
            new SurferBottomSheet(this, (Surfer) clusterItem).show();
        else if(clusterItem instanceof Corner)
            new CornerBottomSheet(this, (Corner) clusterItem)
                    .setOnCornerDeletedListener(new CornerBottomSheet.OnCornerBottomSheetInteractionListener() {
                        @Override
                        public void onJoinGroup(Corner corner) {
                            if (mCornerLoader != null)
                                mCornerLoader.onOpenChat(corner);
                        }

                        @Override
                        public void onDeleted(Corner corner) {
                            if (mCornerLoader != null)
                                mCornerLoader.delete(corner);
                        }
                    })
                    .show();
        return false;
    }

    @Subscribe
    public void onAddMarkerResult(MapMarkerLoader.AddMarkerResultEvent event) {
        mOneSpaceMap.addAll(event.getResult());
    }

    @Subscribe
    public void onRemoveMarkerResult(MapMarkerLoader.RemoveMarkerResultEvent event) {
        mOneSpaceMap.remove(event.getResult());
    }

    @Override
    public void onPanelSlide(View panel, float slideOffset) {
        mCurrentLocationFab.setVisibility(slideOffset >= 0 ? View.GONE : View.VISIBLE);

        if(slideOffset > 0.172) {
            mOpenCornerBarFab.setVisibility(View.GONE);
            mCreateCornerButton.setVisibility(View.VISIBLE);
        } else {
            mOpenCornerBarFab.setVisibility(View.VISIBLE);
            mCreateCornerButton.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPanelExpanded(View panel) {
        mCurrentLocationFab.setVisibility(View.GONE);
        mOpenCornerBarFab.setVisibility(View.GONE);
        mCreateCornerButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPanelCollapsed(View panel) {
        expandMap();
        mCurrentLocationFab.setVisibility(View.GONE);
        mOpenCornerBarFab.setVisibility(View.VISIBLE);
        mCreateCornerButton.setVisibility(View.GONE);
    }

    @Override
    public void onPanelAnchored(View panel) {
        collapseMap();
        mCurrentLocationFab.setVisibility(View.GONE);
        mOpenCornerBarFab.setVisibility(View.GONE);
        mCreateCornerButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPanelHidden(View panel) {
        mCurrentLocationFab.setVisibility(View.VISIBLE);
        mOpenCornerBarFab.setVisibility(View.VISIBLE);
        mCreateCornerButton.setVisibility(View.GONE);
    }

    private void collapseMap() {
        if (mOneSpaceMap != null) {
            mOneSpaceMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(UserLocationManager.getLatitude(), UserLocationManager.getLongitude()), 18f), 1000, null);  //Thianchai (I modified this)
        }
    }

    private void expandMap() {
        if (mOneSpaceMap != null) {
            mOneSpaceMap.animateCamera(CameraUpdateFactory.zoomTo(15f), 1000, null);
        }
    }

    /**
     * Display popup dialog by using custom layout (dialog_create_corner.xml)
     * for create a Corner with Name and Description.
     *
     * This method will send
     * - Name of Corner
     * - Description of Corner
     * - and other param
     * to Server (http://<OneSpace Host>/corners/add/?...) for getting the RoomJID from server
     * and join to the group chat
     */
    private void createCorner() {
        final Dialog dialog = new Dialog(this, android.R.style.Theme_DeviceDefault_Light_Dialog_MinWidth);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_create_corner);
        dialog.setCancelable(false);

        TextView cornerLocation = (TextView) dialog.findViewById(R.id.corner_location);
        final TextInputLayout cornerName = (TextInputLayout) dialog.findViewById(R.id.corner_name_text_input);
        final TextInputLayout cornerDescription = (TextInputLayout) dialog.findViewById(R.id.corner_description_text_input);
        Button createButton = (Button) dialog.findViewById(R.id.corner_add_button);
        ImageButton closeButton = (ImageButton) dialog.findViewById(R.id.close_button);

        cornerLocation.setText(getLocationAsString());
        createButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String name = cornerName.getEditText().getText().toString().trim();
                String description = cornerDescription.getEditText().getText().toString().trim();

                cornerName.setError(null);
                cornerDescription.setError(null);

                if(name.length() <= 0)
                    cornerName.setError("Please enter name of Corner.");
                else {
                    Observable<String> observable = new OneSpaceApi.Builder(getApplicationContext())
                            .addConverterFactory(GsonConverterFactory.create())
                            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                            .build()
                            .addCorner(userID, username,
                                    username + "@" + mSettingManager.xmppServer,
                                    mSettingManager.xmppRecource, name, description,
                                    UserLocationManager.getLatitude(), UserLocationManager.getLongitude());  //Thianchai (I modified this)

                    observable.observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Subscriber<String>() {

                                @Override
                                public void onCompleted() {
                                    Log.i("Create a Corner completed.");
                                    mCornerLoader.notifyMyCornerChanged();
                                    dialog.cancel();
                                }

                                @Override
                                public void onError(Throwable e) {
                                    e.printStackTrace();
                                }

                                @Override
                                public void onNext(String s) {
                                    Log.i("Create corner : " + s);
                                    try {
                                        JSONObject jsonObject = new JSONObject(s);
                                        String name = jsonObject.getString("name");
                                        String roomjid = jsonObject.getString("roomjid");

                                        mReceiver.addCornerJID(roomjid);
                                        Intent intent = new Intent(MessageService.ACTION_XMPP_GROUP_JOIN, null,
                                                getApplicationContext(), MessageService.class);
                                        intent.putExtra(MessageService.KEY_BUNDLE_GROUP_ROOM_JID, roomjid.split("@")[0]);
                                        intent.putExtra(MessageService.KEY_BUNDLE_GROUP_NAME, name);
                                        MessageService.sendToServiceHandler(intent);
                                    } catch (JSONException e) {
                                        try {
                                            JSONObject jsonObject = new JSONObject(s);
                                            String errorCode = jsonObject.getString("errorcode");
                                            String errorMsg = jsonObject.getString("errormsg");
                                            displayAlertDialog("Create corner with error",
                                                    "ErrorCode : " + errorCode + "\n" + errorMsg, new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {

                                                        }
                                                    });
                                        } catch (JSONException je) {

                                        }
                                    }

                                }

                            });
                }
            }
        });

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });

        dialog.show();
    }

    /**
     * This method used for put location to server
     * @param location Location object that contain Latitude and Longitude
     */
    private void putLocationToServer(Location location) {
        Observable<String> observable = new OneSpaceApi.Builder(getApplicationContext())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build()
                .updateGeoLocationRx(userID,
                        location.getLatitude(),
                        location.getLongitude());

        observable.subscribe(new Subscriber<String>() {
            @Override
            public void onCompleted() {
                android.util.Log.i("PutLocation", "completed");
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

    private void locationServiceUnavailable() {
        mCurrentLocationFab.setVisibility(View.GONE);
        mOpenCornerBarFab.setVisibility(View.GONE);
        Snackbar.make(mCoordinatorLayout, getString(R.string.error_location_unavailable), Snackbar.LENGTH_INDEFINITE)
                .setAction("Go to Setting", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent callGPSSettingIntent = new Intent(
                                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(callGPSSettingIntent);
                    }
                })
                .show();
    }

    private void displayAlertDialog(final String title, final String message, final DialogInterface.OnClickListener listener) {
        runOnUiThread(new Runnable() {
            public void run() {
                new AlertDialog.Builder(MapActivity.this)
                        .setTitle(title)
                        .setMessage(message)
                        .setCancelable(true)
                        .setNeutralButton(android.R.string.ok, listener)
                        .create()
                        .show();
            }
        });
    }

    private String getLocationAsString() {
        return UserLocationManager.getLatitude() + ", " + UserLocationManager.getLongitude(); //Thianchai (I modified this)
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 0){

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                mapFragment.getMapAsync(this);

            }
            else{

                Toast.makeText(this, "test", Toast.LENGTH_LONG);

            }

        }

    }


    private class Receiver extends BroadcastReceiver {

        private List<String> listCornerJID = new ArrayList<>();

        public void addCornerJID(String jid) {
            listCornerJID.add(jid);
        }

        @Override
        public void onReceive(Context context, final Intent intent) {
            if (intent.getAction().equals(MessageService.ACTION_XMPP_GROUP_JOIN)) {
                Chat chat = intent.getParcelableExtra(MessageService.KEY_BUNDLE_CHAT);
                boolean status = intent.getBooleanExtra(MessageService.KEY_BUNDLE_GROUP_JOIN_STATUS, false);
                if (chat != null) {
                    if(status) {
                        if (!listCornerJID.contains(chat.getId()))
                            startChatActivity(intent);
                        else {
                            displayConfirmDialog(intent);
                            listCornerJID.remove(chat.getId());
                        }
                    } else {
                        displayAlertDialog("Group Join Fail", "Please try again.",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                    }
                }
            }
        }

        private void startChatActivity(Intent intent) {
            intent.setClass(getApplicationContext(), MainActivity.class);
            intent.putExtra("from_map", true);
            startActivity(intent);
        }

        private void displayConfirmDialog(final Intent intent) {
            runOnUiThread(new Runnable() {
                public void run() {
                    new AlertDialog.Builder(MapActivity.this)
                            .setTitle("Create Completed")
                            .setMessage("Create a corner completed.\nDo you wanna start chatting now?")
                            .setPositiveButton(getString(R.string.confirm_yes), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    startChatActivity(intent);
                                }
                            })
                            .setNegativeButton(getString(R.string.confirm_no), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            })
                            .show();
                }
            });

        }
    }

    //===========================================================================================================//
    //  METHOD BY Thianchai                                                                         METHOD BY Thianchai
    //===========================================================================================================//

    private void init(){

        this.context = getApplicationContext();

        mCornerLocation.setText(UserLocationManager.getLatitude() + ", " + UserLocationManager.getLongitude());

    }

    //===========================================================================================================//
    //  PRIVATE CLASS                                                                               PRIVATE CLASS
    //===========================================================================================================//

    private final class GPSBroadcastReceiver
            extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            Bundle b = intent.getExtras();

            UserLocationManager.setLatitude(b.getDouble("latitude", 0));
            UserLocationManager.setLongitude(b.getDouble("longitude", 0));

            mCornerLocation.setText(UserLocationManager.getLatitude() + ", " + UserLocationManager.getLongitude());

        }

    }

}