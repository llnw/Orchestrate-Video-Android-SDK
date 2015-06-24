package com.example.llsdktest2;


import com.limelight.videosdk.Constants;
import com.limelight.videosdk.ContentService;
import com.limelight.videosdk.IPlayerCallback;
import com.limelight.videosdk.IPlayerControl;
import com.limelight.videosdk.LoggerUtil;
import com.limelight.videosdk.Constants.PlayerState;
import com.limelight.videosdk.utility.Setting;
import com.example.llsdktest2.MediaOfChannel.MediaOfChannelCallback;
import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.SparseArray;
import android.os.Bundle;


public class MainActivity extends FragmentActivity implements IPlayerCallback, OnPageChangeListener,ActionBar.TabListener {

    private String mChannelID = "0c31d9b516624fe89c380204236d655a";
    
    private PlayersFragment mPlayersFragment = null;
    private MediaOfChannel mMediaOfChannelFragment = null;
    private IPlayerControl mControl;
    
    
    private SparseArray<Fragment> mRefer = new SparseArray<Fragment>();
    
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a {@link FragmentPagerAdapter}
     * derivative, which will keep every loaded fragment in memory. If this
     * becomes too memory intensive, it may be best to switch to a
     * {@link android.support.v13.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //configuring SDK
            Setting.configureLimelightSettings("https://staging-api.lvp.llnw.net/rest", "https://staging-wlp.lvp.llnw.net/license", "limelight");
            LoggerUtil.setLogLevelByString("Debug",this);            
            Setting.SetAnalyticsEndPoint("https://staging-mcs.lvp.llnw.net/r/MetricsCollectionService/recordMetricsEvent");

            
        setContentView(R.layout.activity_main);

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setOnPageChangeListener(this);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(2);
        
        actionBar.addTab(actionBar.newTab().setText(mSectionsPagerAdapter.getPageTitle(0)).setTabListener(this));
        actionBar.addTab(actionBar.newTab().setText(mSectionsPagerAdapter.getPageTitle(1)).setTabListener(this));

    }

    MediaOfChannelCallback mediaOfChannelCallback = new MediaOfChannelCallback() {
        @Override
        public void playMediaId(String id, ContentService svc) {
            if(mPlayersFragment != null){
                mPlayersFragment.play(svc,id);
            }
            mViewPager.setCurrentItem(1);
        }
    };
    
    @Override
    public void onTabSelected(ActionBar.Tab tab,
            FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab,
            FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab,
            FragmentTransaction fragmentTransaction) {
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private int mPageCount = 2;
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public void setCount(int val) {
            mPageCount = val;
        }
        
        @Override
        public Fragment getItem(int position) {
            Fragment retFragment = null;

            if(position ==0){
                mMediaOfChannelFragment = new MediaOfChannel(mediaOfChannelCallback,mChannelID);
                retFragment = mMediaOfChannelFragment;
            }else if(position ==1){
                mPlayersFragment = new PlayersFragment();
                mRefer.put(0, mPlayersFragment);
                retFragment = mPlayersFragment;
                }
            return retFragment;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            String title = "";
            switch (position) {
            case 0:
                title = getResources().getString(R.string.title_media_of_channel);
                break;
            case 1:
                title = getResources().getString(R.string.title_player);
                break;
            }
            return title;
            
        }
    }



    @Override
    public void onPageScrollStateChanged(int arg0) {
        
    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
        
    }

    @Override
    public void onPageSelected(int page) {
        if (mPlayersFragment == null) {
            mPlayersFragment = (PlayersFragment) mRefer.get(0);
        }
        if (mPlayersFragment != null){
            mPlayersFragment.hideController();
            //page is for player
            if(page == 1){
                if(mPlayersFragment.getPlayerState() != PlayerState.stopped.ordinal())//stopped
                    mPlayersFragment.show();
            }else{
                if(mPlayersFragment.getPlayerState() != PlayerState.stopped.ordinal())//stopped
                    mControl.pause();
            }
        }
        getActionBar().setSelectedNavigationItem(page);
    }

    @Override
    public void playerAttached(IPlayerControl control) {
        mControl = control;
        //sometimes the mPlayerFragment can  be null as it gets destroyed by the system, so using the cashed object
        if (mPlayersFragment == null){
            mPlayersFragment = (PlayersFragment) mRefer.get(0);
        }
        if (mPlayersFragment != null){
            mPlayersFragment.setControl(mControl);
            mPlayersFragment.showProgress(false,null);
        }
    }

    @Override
    public void playerMessage(int messageType,int value,String message) {
        if (mPlayersFragment == null) {
            mPlayersFragment = (PlayersFragment) mRefer.get(0);
        }
        if(mPlayersFragment != null){
            if(messageType == Constants.Message.status.ordinal()){
                if(value == Constants.PlayerState.completed.ordinal()){

                }else{
                    mPlayersFragment.showProgress(true,message);
                }
            }
            else if(messageType == Constants.Message.error.ordinal()){
                mPlayersFragment.hide();
                mPlayersFragment.showProgress(false,null);
                mPlayersFragment.showMessage(message);
            }
            else if(messageType == Constants.Message.progress.ordinal()){
                mPlayersFragment.hide();
                mPlayersFragment.showProgress(false,null);
                mPlayersFragment.showProgressDialog(value);
            }
            else{
                mPlayersFragment.hide();
                mPlayersFragment.showProgress(false,null);
                mPlayersFragment.showMessage(message);
            }
        }
    }

    @Override
    public void playerPrepared(IPlayerControl control) {

        mControl = control;
        if (mPlayersFragment == null)
            mPlayersFragment = (PlayersFragment) mRefer.get(0);
        if (mPlayersFragment != null) {
            mPlayersFragment.setControl(mControl);
            mPlayersFragment.showProgress(false,null);
            mPlayersFragment.show();
            mPlayersFragment.showKeyboard(false);
        }
    }

}
