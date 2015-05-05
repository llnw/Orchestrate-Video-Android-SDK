package com.limelight.testvideosdk;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.util.SparseArray;
import com.limelight.videosdk.Constants;
import com.limelight.videosdk.IPlayerCallback;
import com.limelight.videosdk.IPlayerControl;
import com.limelight.videosdk.Constants.PlayerState;

public class PlayerActivity extends FragmentActivity implements
        IPlayerCallback, OnPageChangeListener {

    private static final int READ_REQUEST_CODE = 50;
    private ViewPager mViewPager;
    private Uri mUri = null;
    private IPlayerControl mControl;
    private PlayersFragment mPlayerFragment = null;
    private SparseArray<Fragment> mRefer = new SparseArray<Fragment>();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_main);
        PlayerTestAdapter mPlayerTestAdapter = new PlayerTestAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        PagerTabStrip strip = (PagerTabStrip)findViewById(R.id.pager_title_strip);
        strip.setDrawFullUnderline(true);
        strip.setTabIndicatorColor(Color.BLUE);
        mViewPager.setAdapter(mPlayerTestAdapter);
        mViewPager.setOnPageChangeListener(this);
        mViewPager.setOffscreenPageLimit(2);
    }

    public class PlayerTestAdapter extends FragmentPagerAdapter {
        public PlayerTestAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
            case 0:
                mPlayerFragment = new PlayersFragment();
                mRefer.put(0, mPlayerFragment);
                return mPlayerFragment;
            case 1:
                SettingsFragment settingFragment1 = new SettingsFragment();
                return settingFragment1;
            default:
                return null;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            String title = "Default";
            switch (position) {
            case 0:
                title = getResources().getString(R.string.player);
                break;
            case 1:
                title = getResources().getString(R.string.settings);
                break;
            }
            return title;
        }
    }

    @Override
    public void playerAttached(IPlayerControl control) {
        mControl = control;
        if (mPlayerFragment == null)
            mPlayerFragment = (PlayersFragment) mRefer.get(0);
        mPlayerFragment.setControl(mControl);
        mPlayerFragment.showProgress(false,null);
        mPlayerFragment.setEditText("28ed28ffc8e7438783732dc19fae6bbc");
    }

    @Override
    public void playerMessage(int what, int extra,String msg) {
        if(what == Constants.Message.status.ordinal()){
            mPlayerFragment.showProgress(true,msg);
        }
        else if(what == Constants.Message.progress.ordinal()){
            mPlayerFragment.showProgressDialog(extra);
        }
        else{
            if (mPlayerFragment == null) {
                mPlayerFragment = (PlayersFragment) mRefer.get(0);
            }
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
                    mPlayerFragment = (PlayersFragment) mRefer.get(0);
                }
                mPlayerFragment.setEditText(mUri.toString());
                mPlayerFragment.show();
                if (mControl != null) {
//                    mControl.setVideoUri(mUri);
//                    mControl.play(null);
                    mControl.play(mUri.toString(), null);
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
            mPlayerFragment = (PlayersFragment) mRefer.get(0);
        }
        mPlayerFragment.hideController();
        if(page == 0){
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
            mPlayerFragment = (PlayersFragment) mRefer.get(0);
        }
        mPlayerFragment.hideController();
        if(page == 0){
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
            mPlayerFragment = (PlayersFragment) mRefer.get(0);
        }
        mPlayerFragment.hideController();
        if(page == 0){
            if(mPlayerFragment.getPlayerState() != PlayerState.stopped.ordinal())//stopped
            mPlayerFragment.show();
        }else{
            if(mPlayerFragment.getPlayerState() != PlayerState.stopped.ordinal())//stopped
                mControl.pause();
        }
    }

    /*@Override
    public void fetchedEncodings(ArrayList<String> encoding) {
        mPlayerFragment.showEncodingDialog(encoding);
    }*/

    @Override
    public void playerPrepared(IPlayerControl control) {
        mControl = control;
        if (mPlayerFragment == null)
            mPlayerFragment = (PlayersFragment) mRefer.get(0);
        mPlayerFragment.setControl(mControl);
        mPlayerFragment.showProgress(false,null);
        mPlayerFragment.show();
    }
}
