package com.limelight.testvideosdk;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.util.SparseArray;
import com.limelight.testvideosdk.ChannelFragment.ChannelCallback;
import com.limelight.testvideosdk.ChannelGroupFragment.ChannelGroupCallback;
import com.limelight.testvideosdk.MediaFragment.MediaCallback;
import com.limelight.testvideosdk.SpecificChannelFragment.SpecificChannelCallback;
import com.limelight.testvideosdk.SpecificChannelGroupFragment.SpecificChannelGroupCallback;
import com.limelight.videosdk.Constants;
import com.limelight.videosdk.Constants.PlayerState;
import com.limelight.videosdk.IPlayerCallback;
import com.limelight.videosdk.IPlayerControl;

public class PlayerActivity1 extends FragmentActivity implements IPlayerCallback, OnPageChangeListener,ActionBar.TabListener {

    private static final int READ_REQUEST_CODE = 50;
    private ViewPager mViewPager;
    private Uri mUri = null;
    private IPlayerControl mControl;
    private PlayersFragment mPlayerFragment = null;
    SparseArray<Fragment> refer = new SparseArray<Fragment>();
    PlayerTestAdapter mPlayerTestAdapter;

    ChannelFragment channelFragment;
    MediaFragment mediaFragment;
    SpecificChannelGroupFragment channelFragment1;
    SpecificChannelFragment mediaFragment1;
    String groupId = null;
    String channelId = null;
    String mediaId = null;

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

    ChannelGroupCallback callback = new ChannelGroupCallback() {
        @Override
        public void callback(String id) {
            groupId = id;
            if(channelFragment1 != null)
            channelFragment1.restartLoader(id);
            mViewPager.setCurrentItem(3);
        }
    };
    ChannelCallback chanelCallback = new ChannelCallback() {
        @Override
        public void callback(String id) {
            channelId = id;
            if(mediaFragment1 != null)
                mediaFragment1.restartLoader(id);
            mViewPager.setCurrentItem(5);
        }
    };
    SpecificChannelGroupCallback chanelCallback1 = new SpecificChannelGroupCallback() {
        @Override
        public void callback(String id) {
            channelId = id;
            if(mediaFragment1 != null)
                mediaFragment1.restartLoader(id);
            mViewPager.setCurrentItem(5);
        }
    };
    
    MediaCallback mediaCallback = new MediaCallback() {
        @Override
        public void callback(String id) {
            channelId = id;
            if(mPlayerFragment != null){
                mPlayerFragment.setEditText(id);
                mPlayerFragment.play();
            }
            mViewPager.setCurrentItem(6);
        }
    };
    SpecificChannelCallback mediaCallback1 = new SpecificChannelCallback() {
        @Override
        public void callback(String id) {
            channelId = id;
            if(mPlayerFragment != null){
                mPlayerFragment.setEditText(id);
                mPlayerFragment.play();
            }
            mViewPager.setCurrentItem(6);
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

        @Override
        public Fragment getItem(int i) {
            switch (i) {
            case 0:
                SettingsFragment settingFragment = new SettingsFragment();
                return settingFragment;
            case 1:
                ChannelGroupFragment channelGroupFragment = new ChannelGroupFragment(callback);
                return channelGroupFragment;
            case 2:
                channelFragment = new ChannelFragment(chanelCallback);
                return channelFragment;
            case 3:
                channelFragment1 = new SpecificChannelGroupFragment(chanelCallback1,groupId);
                return channelFragment1;
            case 4:
                mediaFragment = new MediaFragment(mediaCallback);
                return mediaFragment;
            case 5:
                mediaFragment1 = new SpecificChannelFragment(mediaCallback1,channelId);
                return mediaFragment1;
            case 6:
                mPlayerFragment = new PlayersFragment();
                refer.put(0, mPlayerFragment);
                return mPlayerFragment;
            default:
                return null;
            }
        }

        @Override
        public int getCount() {
            return count;
        }

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
    @Override
    public void playerAttached(IPlayerControl control) {
        mControl = control;
        if (mPlayerFragment == null)
            mPlayerFragment = (PlayersFragment) refer.get(0);
        mPlayerFragment.setControl(mControl);
        mPlayerFragment.showProgress(false,null);
//        mPlayerFragment.setEditText("28ed28ffc8e7438783732dc19fae6bbc");
    }

    @Override
    public void playerMessage(int what, int extra,String msg) {
        if (mPlayerFragment == null) {
            mPlayerFragment = (PlayersFragment) refer.get(0);
        }
        if(what == Constants.Message.status.ordinal()){
            mPlayerFragment.showProgress(true,msg);
        }
        else if(what == Constants.Message.progress.ordinal()){
            mPlayerFragment.showProgress(false,null);
            mPlayerFragment.showProgressDialog(extra);
        }
        else{
            mPlayerFragment.hide();
            mPlayerFragment.showProgress(false,null);
            mPlayerFragment.showMessage(msg);
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode,
            Intent resultData) {

        if (requestCode == READ_REQUEST_CODE
                && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                mUri = resultData.getData();
                if (mPlayerFragment == null) {
                    mPlayerFragment = (PlayersFragment) refer.get(0);
                }
                mPlayerFragment.setEditText(mUri.toString());
                mPlayerFragment.show();
                if (mControl != null) {
//                    mControl.setVideoUri(mUri);
//                    mControl.play(null);
                    mControl.play(mUri.toString());
                } else
                    Log.e(getLocalClassName(), "Control is null");
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
    @Override
    public void onPageScrollStateChanged(int page) {
        if (mPlayerFragment == null) {
            mPlayerFragment = (PlayersFragment) refer.get(0);
        }
        if (mPlayerFragment != null)
        mPlayerFragment.hideController();
        if(page == 7){
            if(mPlayerFragment.getPlayerState() != PlayerState.stopped.ordinal())//stopped
            mPlayerFragment.show();
        }else{
            if(mPlayerFragment.getPlayerState() != PlayerState.stopped.ordinal())//stopped
                mControl.pause();
        }
    }

    @Override
    public void onPageScrolled(int page, float arg1, int arg2) {
        if (mPlayerFragment == null) {
            mPlayerFragment = (PlayersFragment) refer.get(0);
        }
        if (mPlayerFragment != null)
        mPlayerFragment.hideController();
        if(page == 7){
            if(mPlayerFragment.getPlayerState() != PlayerState.stopped.ordinal())//stopped
                mPlayerFragment.show();
        }else{
            if(mPlayerFragment.getPlayerState() != PlayerState.stopped.ordinal())//stopped
                mControl.pause();
        }
    }

    @Override
    public void onPageSelected(int page) {
        if (mPlayerFragment == null) {
            mPlayerFragment = (PlayersFragment) refer.get(0);
        }
        if (mPlayerFragment != null)
        mPlayerFragment.hideController();
        if(page == 7){
            if(mPlayerFragment.getPlayerState() != PlayerState.stopped.ordinal())//stopped
            mPlayerFragment.show();
        }else{
            if(mPlayerFragment.getPlayerState() != PlayerState.stopped.ordinal())//stopped
                mControl.pause();
        }
        getActionBar().setSelectedNavigationItem(page);
    }

    @Override
    public void playerPrepared(IPlayerControl control) {
        mControl = control;
        if (mPlayerFragment == null)
            mPlayerFragment = (PlayersFragment) refer.get(0);
        mPlayerFragment.setControl(mControl);
        mPlayerFragment.showProgress(false,null);
        mPlayerFragment.show();
        mControl.play(null);
        mPlayerFragment.showKeyboard(false);
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