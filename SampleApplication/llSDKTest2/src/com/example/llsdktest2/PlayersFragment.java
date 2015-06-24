package com.example.llsdktest2;


import com.limelight.videosdk.ContentService;
import com.limelight.videosdk.IPlayerControl;
import com.limelight.videosdk.PlayerSupportFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

public class PlayersFragment extends Fragment {

    private PlayerSupportFragment mPlayer;
    private ProgressDialog mProgress = null;
    private ProgressDialog mProgressDialog = null;
    private IPlayerControl mControl;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_player, container,false);

        mProgress = new ProgressDialog(getActivity(), ProgressDialog.STYLE_SPINNER);
        mProgress.setCanceledOnTouchOutside(false);
        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setMessage("Downloading. Please Wait...");
        mProgressDialog.setCanceledOnTouchOutside(false);        
        showKeyboard(false);

        mPlayer = new PlayerSupportFragment();
        getChildFragmentManager().beginTransaction().add(R.id.videoLayout, mPlayer).addToBackStack(null).commitAllowingStateLoss();
        return rootView;
    }

    public void play(ContentService svc, String mediaId) {
        if (mControl != null) {
            mControl.stop();
            hide();
            hideController();
            mControl.play(mediaId, svc);//used with delivery
        } else
            Log.e(getActivity().getLocalClassName(), "Control is null");
    }

    public void setControl(IPlayerControl control) {
        mControl = control;
    }

    public void showProgress(final boolean show, final String strMsg){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(mProgress != null)
                    if(show){
                        mProgress.setMessage(strMsg);
                        mProgress.show();
                    }
                    else{
                        mProgress.hide();
                    }
            }
        });
    }

    public void showProgressDialog(final int percent){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(mProgressDialog != null){
                    if(!mProgressDialog.isShowing() && percent==0){
                        mProgressDialog.show();
                    }
                    if(percent != 100){
                        mProgressDialog.setProgress(percent);
                    }else{
                        mProgressDialog.dismiss();
                    }
                }
            }
        });
    }

    public void showMessage(final String msg){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast toast = new Toast(getActivity());
                toast.setGravity(Gravity.TOP, 0, 300);
                toast.setDuration(Toast.LENGTH_LONG);
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View view = inflater.inflate(R.layout.toast, null);
                TextView tv = (TextView)view.findViewById(R.id.text);
                tv.setText(msg);
                toast.setView(view);
                toast.show();
            }
        });
    }

    public void show() {
        getChildFragmentManager().beginTransaction().show(mPlayer).addToBackStack(null).commitAllowingStateLoss();
    }

    public void hide() {
        getChildFragmentManager().beginTransaction().hide(mPlayer).addToBackStack(null).commitAllowingStateLoss();
    }

    public void hideController(){
        mPlayer.hideMediaController();
    }

    public int getPlayerState(){
        return mPlayer.getCurrentPlayState();
    }

    public void showKeyboard(boolean show) {   
        // Check if no view has focus:
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if(show){
                inputManager.showSoftInput(view, InputMethodManager.HIDE_NOT_ALWAYS);
            }else{
                inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                view.clearFocus();
            }
        }
    }
}
