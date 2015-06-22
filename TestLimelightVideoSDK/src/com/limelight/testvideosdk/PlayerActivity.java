package com.limelight.testvideosdk;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.util.SparseArray;
import android.webkit.URLUtil;
import com.limelight.testvideosdk.ChannelFragment.ChannelCallback;
import com.limelight.testvideosdk.ChannelGroupFragment.ChannelGroupCallback;
import com.limelight.testvideosdk.MediaFragment.MediaCallback;
import com.limelight.testvideosdk.SpecificChannelFragment.SpecificChannelCallback;
import com.limelight.testvideosdk.SpecificChannelGroupFragment.SpecificChannelGroupCallback;
import com.limelight.videosdk.Constants;
import com.limelight.videosdk.Constants.PlayerState;
import com.limelight.videosdk.ContentService;
import com.limelight.videosdk.IPlayerCallback;
import com.limelight.videosdk.IPlayerControl;
import com.limelight.videosdk.model.Media;

/**
 * The main activity class
 *
 */
public class PlayerActivity extends FragmentActivity implements IPlayerCallback, OnPageChangeListener,ActionBar.TabListener {

    private static final int READ_REQUEST_CODE = 50;
    public ViewPager mViewPager;
    private Uri mUri = null;
    private IPlayerControl mControl;
    private PlayersFragment mPlayerFragment = null;
    //cashing the player fragment
    private SparseArray<Fragment> mRefer = new SparseArray<Fragment>();
    private PlayerTestAdapter mPlayerTestAdapter;

    private ChannelFragment mChannelFragment;
    private MediaFragment mMediaFragment;
    private SpecificChannelGroupFragment mSpecificChannelGroupFragment;
    private SpecificChannelFragment mSpecificChannelFragment;
    private String mGroupId = null;
    private String mChannelId = null;

    /**
     * (non-Javadoc)
     * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_main1);
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        mPlayerTestAdapter = new PlayerTestAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setOnPageChangeListener(this);
        mViewPager.setAdapter(mPlayerTestAdapter);
        mViewPager.setOffscreenPageLimit(7);
        actionBar.addTab(actionBar.newTab().setText(mPlayerTestAdapter.getPageTitle(0)).setTabListener(this));
        actionBar.addTab(actionBar.newTab().setText(mPlayerTestAdapter.getPageTitle(1)).setTabListener(this));
        actionBar.addTab(actionBar.newTab().setText(mPlayerTestAdapter.getPageTitle(2)).setTabListener(this));
        actionBar.addTab(actionBar.newTab().setText(mPlayerTestAdapter.getPageTitle(3)).setTabListener(this));
        actionBar.addTab(actionBar.newTab().setText(mPlayerTestAdapter.getPageTitle(4)).setTabListener(this));
        actionBar.addTab(actionBar.newTab().setText(mPlayerTestAdapter.getPageTitle(5)).setTabListener(this));
        actionBar.addTab(actionBar.newTab().setText(mPlayerTestAdapter.getPageTitle(6)).setTabListener(this));
    }

    /**
     * Implementing ChannelGroupCallback
     */
    ChannelGroupCallback channelGroupCallback = new ChannelGroupCallback() {
        @Override
        public void callback(String id) {
            mGroupId = id;
            if(mSpecificChannelGroupFragment != null)
                mSpecificChannelGroupFragment.restartLoader(id);
            mViewPager.setCurrentItem(3);
        }
    };

    /**
     * Implementing ChannelCallback
     */
    ChannelCallback channelCallback = new ChannelCallback() {
        @Override
        public void callback(String id) {
            mChannelId = id;
            if(mSpecificChannelFragment != null)
                mSpecificChannelFragment.restartLoader(id);
            mViewPager.setCurrentItem(5);
        }

        @Override
        public void playChannel(String channelId) {
            if(mPlayerFragment != null){
                mPlayerFragment.playChannel(channelId);
            }
            mViewPager.setCurrentItem(6);
        }
    };

    /**
     * Implementing SpecificChannelGroupCallback
     */
    SpecificChannelGroupCallback specificChannelGroupCallback = new SpecificChannelGroupCallback() {
        @Override
        public void callback(String id) {
            mChannelId = id;
            if(mSpecificChannelFragment != null)
                mSpecificChannelFragment.restartLoader(id);
            mViewPager.setCurrentItem(5);
        }

        @Override
        public void playChannel(String channelId) {
            if(mPlayerFragment != null){
                mPlayerFragment.playChannel(channelId);
            }
            mViewPager.setCurrentItem(6);
        }
    };

    /**
     * Implementing MediaCallback
     */
    MediaCallback mediaCallback = new MediaCallback() {
        @Override
        public void callback(String id, ContentService svc) {
            mViewPager.setCurrentItem(6);
            mChannelId = id;
            if(mPlayerFragment != null){
                mPlayerFragment.setEditText(id);
                mPlayerFragment.play(svc);
            }
            
        }

        @Override
        public void addToPlaylist(Media media) {
            if(mPlayerFragment != null){
                mPlayerFragment.addToPlaylist(media);
            }
        }

        @Override
        public void removeFromPlaylist(Media media) {
            if(mPlayerFragment != null){
                mPlayerFragment.removeFromPlaylist(media);
            }
        }
    };

    /**
     * Implementing SpecificChannelCallback
     */
    SpecificChannelCallback specificChannelCallback = new SpecificChannelCallback() {
        @Override
        public void callback(String id, ContentService svc) {
            mChannelId = id;
            if(mPlayerFragment != null){
                mPlayerFragment.setEditText(id);
                mPlayerFragment.play(svc);
            }
            mViewPager.setCurrentItem(6);
        }

        @Override
        public void addToPlaylist(Media media) {
            if(mPlayerFragment != null){
                mPlayerFragment.addToPlaylist(media);
            }
        }

        @Override
        public void removeFromPlaylist(Media media) {
            if(mPlayerFragment != null){
                mPlayerFragment.removeFromPlaylist(media);
            }
        }
    };

    public class PlayerTestAdapter extends FragmentPagerAdapter {
        int count = 7;
        public PlayerTestAdapter(FragmentManager fm) {
            super(fm);
        }

        public void setCount(int val) {
            count = val;
        }

        /**
         * (non-Javadoc)
         * @see android.support.v4.app.FragmentPagerAdapter#getItem(int)
         * Creating fragments and returning the objects.
         */
        @Override
        public Fragment getItem(int i) {
            switch (i) {
            case 0:
                SettingsFragment settingFragment = new SettingsFragment();
                return settingFragment;
            case 1:
                ChannelGroupFragment channelGroupFragment = new ChannelGroupFragment(channelGroupCallback);
                return channelGroupFragment;
            case 2:
                mChannelFragment = new ChannelFragment(channelCallback);
                return mChannelFragment;
            case 3:
                mSpecificChannelGroupFragment = new SpecificChannelGroupFragment(specificChannelGroupCallback,mGroupId);
                return mSpecificChannelGroupFragment;
            case 4:
                mMediaFragment = new MediaFragment(mediaCallback);
                return mMediaFragment;
            case 5:
                mSpecificChannelFragment = new SpecificChannelFragment(specificChannelCallback,mChannelId);
                return mSpecificChannelFragment;
            case 6:
                mPlayerFragment = new PlayersFragment();
                mRefer.put(0, mPlayerFragment);
                return mPlayerFragment;
            default:
                return null;
            }
        }

        @Override
        public int getCount() {
            return count;
        }

        /**
         * (non-Javadoc)
         * @see android.support.v4.view.PagerAdapter#getPageTitle(int)
         * returning the page title
         */
        @Override
        public CharSequence getPageTitle(int position) {
            String title = "";
            switch (position) {
            case 0:
                title = getResources().getString(R.string.settings);
                break;
            case 1:
                title = getResources().getString(R.string.channel_groups);
                break;
            case 2:
                title = getResources().getString(R.string.all_channels);
                break;
            case 3:
                title = getResources().getString(R.string.specific_channels);
                break;
            case 4:
                title = getResources().getString(R.string.all_media);
                break;
            case 5:
                title = getResources().getString(R.string.specific_media);
                break;
            case 6:
                title = getResources().getString(R.string.player);
                break;
            }
            return title;
        }
    }

    /**
     * (non-Javadoc)
     * @see com.limelight.videosdk.IPlayerCallback#playerAttached(com.limelight.videosdk.IPlayerControl)
     */
    @Override
    public void playerAttached(IPlayerControl control) {
        mControl = control;
        //sometimes the mPlayerFragment can  be null as it gets destroyed by the system, so using the cashed object
        if (mPlayerFragment == null){
            mPlayerFragment = (PlayersFragment) mRefer.get(0);
        }
        if (mPlayerFragment != null){
            mPlayerFragment.setControl(mControl);
            mPlayerFragment.showProgress(false,null);
        }
        //mPlayerFragment.setEditText("28ed28ffc8e7438783732dc19fae6bbc");
    }

    /**
     * (non-Javadoc)
     * @see com.limelight.videosdk.IPlayerCallback#playerMessage(int, int, java.lang.String)
     */
    @Override
    public void playerMessage(int messageType,int value,String message) {
        if (mPlayerFragment == null) {
            mPlayerFragment = (PlayersFragment) mRefer.get(0);
        }
        if(mPlayerFragment != null){
            if(messageType == Constants.Message.status.ordinal()){
                if(value == Constants.PlayerState.completed.ordinal()){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mPlayerFragment.playCompleted();
                        }
                    });
                }else{
                    mPlayerFragment.showProgress(true,message);
                }
            }
            else if(messageType == Constants.Message.error.ordinal()){
                mPlayerFragment.hide();
                mPlayerFragment.showProgress(false,null);
                mPlayerFragment.showMessage(message);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mPlayerFragment.playCompleted();
                    }
                });
            }
            else if(messageType == Constants.Message.progress.ordinal()){
                mPlayerFragment.hide();
                mPlayerFragment.showProgress(false,null);
                mPlayerFragment.showProgressDialog(value);
            }
            else{
                mPlayerFragment.hide();
                mPlayerFragment.showProgress(false,null);
                mPlayerFragment.showMessage(message);
            }
        }
    }

    /**
     * (non-Javadoc)
     * @see com.limelight.videosdk.IPlayerCallback#playerPrepared(com.limelight.videosdk.IPlayerControl)
     */
    @Override
    public void playerPrepared(IPlayerControl control) {
        mControl = control;
        if (mPlayerFragment == null)
            mPlayerFragment = (PlayersFragment) mRefer.get(0);
        if (mPlayerFragment != null) {
            mPlayerFragment.setControl(mControl);
            mPlayerFragment.showProgress(false,null);
            mPlayerFragment.show();
            mPlayerFragment.showKeyboard(false);
        }
    }

    /**
     * (non-Javadoc)
     * @see android.support.v4.app.FragmentActivity#onActivityResult(int, int, android.content.Intent)
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {

        if (requestCode == READ_REQUEST_CODE
                && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                mUri = resultData.getData();
                if (mPlayerFragment == null) {
                    mPlayerFragment = (PlayersFragment) mRefer.get(0);
                }
                if(mPlayerFragment!= null){
                    if(URLUtil.isValidUrl(mUri.toString()) && URLUtil.isContentUrl(mUri.toString())){
                        String[] proj = { MediaStore.Video.VideoColumns.DISPLAY_NAME };
                        Cursor cursor = getContentResolver().query(mUri, proj, null, null, null);
                        if (cursor != null && cursor.getCount() != 0) {
                            cursor.moveToFirst();
                            mPlayerFragment.setLocalfileName(cursor.getString(0));
                        }
                        if (cursor != null) {
                            cursor.close();
                        }
                    }
                        mPlayerFragment.setEditText(mUri.toString());
                    mPlayerFragment.show();
                }
                if (mControl != null) {
                    //mControl.setVideoUri(mUri);
                    //mControl.play(null);
                    mControl.play(mUri.toString(), null);//local  file playback
                } else {
                    Log.e(getLocalClassName(), "Control is null");
                }
            }
        }
    }

    /**
     * (non-Javadoc)
     * @see android.support.v4.app.FragmentActivity#onConfigurationChanged(android.content.res.Configuration)
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onPageScrollStateChanged(int page) {}

    @Override
    public void onPageScrolled(int page, float arg1, int arg2) {}

    /**
     * (non-Javadoc)
     * @see android.support.v4.view.ViewPager.OnPageChangeListener#onPageSelected(int)
     * Stopping the player when moved away from player fragment.
     */
    @Override
    public void onPageSelected(int page) {
        if (mPlayerFragment == null) {
            mPlayerFragment = (PlayersFragment) mRefer.get(0);
        }
        if (mPlayerFragment != null){
            mPlayerFragment.hideController();
            //page is for player
            if(page == 7){
                if(mPlayerFragment.getPlayerState() != PlayerState.stopped.ordinal())//stopped
                    mPlayerFragment.show();
            }else{
                if(mPlayerFragment.getPlayerState() != PlayerState.stopped.ordinal())//stopped
                    mControl.pause();
            }
        }
        getActionBar().setSelectedNavigationItem(page);
    }

    @Override
    public void onTabReselected(Tab tab, FragmentTransaction ft) {
    }

    @Override
    public void onTabSelected(Tab tab, FragmentTransaction ft) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
    }
}
