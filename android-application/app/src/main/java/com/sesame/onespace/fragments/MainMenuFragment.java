package com.sesame.onespace.fragments;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sesame.onespace.R;
import com.sesame.onespace.fragments.dashboardFragments.DashboardMainFragment;
import com.sesame.onespace.fragments.qaMessageFragments.QAMainFragment;
import com.sesame.onespace.managers.service.OnespaceNotificationManager;
import com.sesame.onespace.models.chat.Chat;
import com.sesame.onespace.service.MessageService;
import com.sesame.onespace.utils.DrawableUtil;
import com.sesame.onespace.utils.Log;
import com.sesame.onespace.views.MenuCoverLocationView;

import java.util.ArrayList;
import java.util.List;

// Modified code by Thianchai
    //1. Q&A Message tab
    //2. Dashboard tab

public class MainMenuFragment extends Fragment implements View.OnClickListener, AppBarLayout.OnOffsetChangedListener,
        ChatListFragment.OnChatListInteractionListener {

    public static final String KEY_NAME = "title";

    private static final float PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR  = 0.9f;
    private static final float PERCENTAGE_TO_HIDE_TITLE_DETAILS = 0.3f;
    private static final int ALPHA_ANIMATIONS_DURATION = 200;

    private boolean mIsTheTitleVisible = false;
    private boolean mIsTheTitleContainerVisible = true;

    private OnNewMenuFragmentInteractionListener mListener;
    private Toolbar mToolbar;
    private CollapsingToolbarLayout mCollapsing;;
    private CoordinatorLayout rootView;
    private LinearLayout mTitleContainer;
    private AppBarLayout mAppBarLayout;
    private ImageView mImageparallax;
    private FrameLayout mFrameParallax;
    private TextView mUsername;
    private TextView mUsernameToolbar;
    private ImageView mAvatar;
    private MenuCoverLocationView mMenuCoverLocationView;

    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private ChatListFragment chatListFragment;
    private TabMapFragment tabMapFragment;
    private QAMainFragment qaMainFragment; //add by Thianchai
    private DashboardMainFragment dashboardMainFragment;  //add by Thianchai

    private String name;

    //Thianchai (I add this)
    private static Boolean bFocusQA = false;
    //**

    public static MainMenuFragment newInstance(String name) {
        MainMenuFragment fragment = new MainMenuFragment();
        Bundle bundle = new Bundle();
        bundle.putString(KEY_NAME, name);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if(bundle != null) {
            name = bundle.getString(KEY_NAME);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_main_menu, container, false);
        rootView = (CoordinatorLayout) view.findViewById(R.id.main_root_layout);
        mCollapsing = (CollapsingToolbarLayout) view.findViewById(R.id.main_collapsing);
        mTitleContainer = (LinearLayout) view.findViewById(R.id.main_linearlayout_user_info);
        mAppBarLayout = (AppBarLayout) view.findViewById(R.id.main_appbar);
        mImageparallax = (ImageView) view.findViewById(R.id.main_imageview_placeholder);
        mFrameParallax = (FrameLayout) view.findViewById(R.id.main_framelayout_user_info);
        mUsername = (TextView) view.findViewById(R.id.main_toolbar_username);
        mUsernameToolbar = (TextView) view.findViewById(R.id.main_username);
        mAvatar = (ImageView) view.findViewById(R.id.main_user_avatar);
        mMenuCoverLocationView = new MenuCoverLocationView(getContext(), view);
        mMenuCoverLocationView.setOnClickCoverListener(true, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onCreateUserCorner();
            }
        });
        mMenuCoverLocationView.setOnClickCoverListener(false, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callGPSSettingIntent = new Intent(
                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(callGPSSettingIntent);
            }
        });

        mToolbar = (Toolbar) view.findViewById(R.id.main_toolbar);
        mToolbar.setTitle("");
        mAppBarLayout.addOnOffsetChangedListener(this);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(mToolbar);

        chatListFragment = ChatListFragment.newInstance(MessageService.getConnectionStatus());
        tabMapFragment = TabMapFragment.newInstance();
        qaMainFragment = QAMainFragment.newInstance(); //add by Thianchai
        dashboardMainFragment = DashboardMainFragment.newInstance();  //add by Thianchai

        mViewPager = (ViewPager) view.findViewById(R.id.pager);
        setupViewPager(mViewPager);

        mTabLayout = (TabLayout) view.findViewById(R.id.tab_layout);
        mTabLayout.setupWithViewPager(mViewPager);
        setupTabIcons(mTabLayout);

        setUsername(name);
        startAlphaAnimation(mUsername, 0, View.INVISIBLE);
        initParallaxValues();

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_empty, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (OnNewMenuFragmentInteractionListener) context;
            Log.i(">>>>>>>>>>>>>>>> MainMenuFragment.onAttach(): try block, all OK");
        } catch (ClassCastException e) {
            Log.i(">>>>>>>>>>>>>>>> MainMenuFragment.onAttach(): catch block (so mListener is NULL)");
            throw new ClassCastException(context.toString()
                    + " must implement OnMenuFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void initParallaxValues() {
        CollapsingToolbarLayout.LayoutParams petDetailsLp =
                (CollapsingToolbarLayout.LayoutParams) mImageparallax.getLayoutParams();

        CollapsingToolbarLayout.LayoutParams petBackgroundLp =
                (CollapsingToolbarLayout.LayoutParams) mFrameParallax.getLayoutParams();

        petDetailsLp.setParallaxMultiplier(0.9f);
        petBackgroundLp.setParallaxMultiplier(0.3f);

        mImageparallax.setLayoutParams(petDetailsLp);
        mFrameParallax.setLayoutParams(petBackgroundLp);
    }

    public void setUsername(String name) {
        mUsername.setText(name);
        mUsernameToolbar.setText(name);
        mAvatar.setImageDrawable(DrawableUtil.getTextDrawable(getContext(), name));
    }

    public void setLocation(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        mMenuCoverLocationView.setLocation(this.roundToDecimal(latitude,5) + ", " + this.roundToDecimal(longitude,5));
    }

    public void setLocationAddress(String location,Address address) { //edit by Thianchai (add paramiter String location) (old code by chongos)
        String street = address.getAddressLine(0);
        String country = address.getCountryName();

        //edit by thianchai (old code by chongos)
        //String location = address.getLatitude() + ", " + address.getLongitude();
        //
        String addressStr = street + "\n" + country;

        mMenuCoverLocationView.setLocationAddress(location, addressStr);
    }

    public void locationServiceAvailable(boolean available) {
        mMenuCoverLocationView.setLocationAvailable(available);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

        }
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int offset) {
        int maxScroll = appBarLayout.getTotalScrollRange();
        float percentage = (float) Math.abs(offset) / (float) maxScroll;
        handleAlphaOnTitle(percentage);
        handleToolbarTitleVisibility(percentage);
    }

    private void handleToolbarTitleVisibility(float percentage) {
        if (percentage >= PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR) {
            if(!mIsTheTitleVisible) {
                startAlphaAnimation(mUsername, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                mIsTheTitleVisible = true;
            }
        } else {
            if (mIsTheTitleVisible) {
                startAlphaAnimation(mUsername, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                mIsTheTitleVisible = false;
            }
        }
    }

    private void handleAlphaOnTitle(float percentage) {
        if (percentage >= PERCENTAGE_TO_HIDE_TITLE_DETAILS) {
            if(mIsTheTitleContainerVisible) {
                startAlphaAnimation(mTitleContainer, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                mIsTheTitleContainerVisible = false;
            }
        } else {
            if (!mIsTheTitleContainerVisible) {
                startAlphaAnimation(mTitleContainer, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                mIsTheTitleContainerVisible = true;
            }
        }
    }

    public static void startAlphaAnimation (View v, long duration, int visibility) {
        AlphaAnimation alphaAnimation = (visibility == View.VISIBLE)
                ? new AlphaAnimation(0f, 1f)
                : new AlphaAnimation(1f, 0f);

        alphaAnimation.setDuration(duration);
        alphaAnimation.setFillAfter(true);
        v.startAnimation(alphaAnimation);
    }


    @TargetApi(Build.VERSION_CODES.M)
    private void setupViewPager(final ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager());
        adapter.addFragment(tabMapFragment, "Map");
        adapter.addFragment(chatListFragment, "Chat");
        adapter.addFragment(qaMainFragment, "Q&A Message"); //add by Thianchai
        adapter.addFragment(dashboardMainFragment, "Dashborad");  //add by Thianchai
        adapter.addFragment(SettingsListFragment.newInstance(), "Settings");
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(1, true);
        viewPager.setOnScrollChangeListener(new View.OnScrollChangeListener() {

            private int count = 0;
            private Handler handler = new Handler();
            private Runnable runnable = new Runnable() {

                @Override
                public void run() {
                    if(count < 3) {  //change 3 -> 5 by Thianchai
                        count++;
                        tabMapFragment.setText(tabMapFragment.getText()+".");
                        handler.postDelayed(runnable, 400);
                    } else {
                        mListener.onOpenMap();
                        viewPager.setCurrentItem(1, true);
                        tabMapFragment.setText("Open Map");
                        count = 0;
                    }
                }
            };


            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {

                if (mTabLayout.getSelectedTabPosition() == 2){
                    bFocusQA = true;
                    OnespaceNotificationManager.getSettingsManager(getContext()).cancelNotifications("ONESPACE_NOTIFICATION_GROUP_KEY__QA_MESSAGE");
                }
                else{
                    bFocusQA = false;
                }

                if(viewPager.getCurrentItem() <= 1) {
                    float width = viewPager.getWidth();
                    float alpha = (scrollX / width) * -1;
                    tabMapFragment.setAlpha(alpha);
                    if(viewPager.getCurrentItem() == 0 && alpha == 1) {
                        handler.postDelayed(runnable, 100);
                    } else {
                        handler.removeCallbacks(runnable);
                        tabMapFragment.setText("Open Map");
                        count = 0;
                    }
                }
            }
        });
    }

    private void setupTabIcons(TabLayout tablayout) {
        TextView tabMap = (TextView) LayoutInflater.from(getContext()).inflate(R.layout.tab_menu_icon_and_text, null);
        tabMap.setText("Map");
        tabMap.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_tab_maps, 0, 0);
        tablayout.getTabAt(0).setCustomView(tabMap);

        TextView tabChat = (TextView) LayoutInflater.from(getContext()).inflate(R.layout.tab_menu_icon_and_text, null);
        tabChat.setText("Chats");
        tabChat.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_tab_chats, 0, 0);
        tablayout.getTabAt(1).setCustomView(tabChat);

        //wrote by Thianchai
        TextView qaTabView = (TextView) LayoutInflater.from(getContext()).inflate(R.layout.tab_menu_icon_and_text, null);
        qaTabView.setText("Q&A");
        qaTabView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_tab_qa_new, 0, 0);
        tablayout.getTabAt(2).setCustomView(qaTabView);

        TextView dashboardTabView = (TextView) LayoutInflater.from(getContext()).inflate(R.layout.tab_menu_icon_and_text, null);
        dashboardTabView.setText("DB");
        dashboardTabView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_tab_dashboard, 0, 0);
        tablayout.getTabAt(3).setCustomView(dashboardTabView);
        //

        TextView tabSetting = (TextView) LayoutInflater.from(getContext()).inflate(R.layout.tab_menu_icon_and_text, null);
        tabSetting.setText("Set");
        tabSetting.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_tab_settings, 0, 0);
        tablayout.getTabAt(4).setCustomView(tabSetting);

    }

    public void setConncetionStatus(int conncetionStatus) {
        chatListFragment.setConncetionStatus(conncetionStatus);
    }


    public void removeChat(Chat chat) {
        chatListFragment.removeChat(chat);
    }

    public void updateChat(Chat chat) {
        chatListFragment.updateChat(chat);
    }

    @Override
    public void onOpenChat(Chat chat) {
        mListener.onOpenChat(chat);
    }

    @Override
    public void onRemoveChat(Chat chat) {
        mListener.onRemoveChat(chat);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {

        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            super.destroyItem(container, position, object);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }


    public static double roundToDecimal(double coord, int decimals) {
        return (double) Math.round(coord * Math.pow(10, decimals)) / Math.pow(10, decimals);
    }


    public interface OnNewMenuFragmentInteractionListener {
        void onOpenMap();
        void onOpenChat(Chat chat);
        void onRemoveChat(Chat chat);
        void onCreateUserCorner();
    }

    //===========================================================================================================//
    //  FOCUS Q&A                                                                                   FOCUS Q&A
    //===========================================================================================================//

    public static Boolean getbFocusQA(){

        return bFocusQA;

    }

    public ViewPager getViewPager() {
        return this.mViewPager;
    }

}
