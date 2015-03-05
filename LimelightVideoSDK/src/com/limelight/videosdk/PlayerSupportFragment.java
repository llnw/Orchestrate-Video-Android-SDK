package com.limelight.videosdk;

import java.util.ArrayList;
import org.apache.log4j.Logger;
import com.limelight.videosdk.Constants.PlayerState;
import com.limelight.videosdk.ContentService.EncodingsCallback;
import com.limelight.videosdk.model.Delivery;
import com.limelight.videosdk.model.Encoding;
import com.limelight.videosdk.model.PrimaryUse;
import com.limelight.videosdk.utility.Downloader;
import com.limelight.videosdk.utility.Setting;
import com.limelight.videosdk.utility.Downloader.DownLoadCallback;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.VideoView;

/**
 * This class is the customized fragment which also contains native android
 * player.
 * <p>
 * It also includes media controller object which is the UI controller for the
 * player. Media Controller has UI controls like seek bar, play button, pause
 * button,next button, previous button.Currently it is using default media
 * controller, if needed we will extend the media controller class for
 * customizing the UI control.
 * <p>
 * It also has PlayerControl as an inner final class. This is the implementation
 * of IPlayerControl. This is the control to the Player. It is supplied to the
 * Activity which requested for the Player.
 * <p>
 * It also has IPlayerCallback which holds the IPlayerCallback implementation
 * sent from activity. It is used to interact from Player to activity which
 * requested for Player.<br>
 * Player will be available as an embed-able component in customer application
 * container view. It is made embed-able by extending the fragment.
 * <p>
 * PlayerSupportFragment gets the list of channels groups, channels,media and 
 * encodings by calling ContentService methods.
 * <P>
 * PlayerSupportFragment also has a countdown timer.When player crosses a certain 
 * time in fetching the media data, this timer kicks its callback methods to terminate the player preparation and send message to developer application.

 * Since the Player has to be an embed-able component, Player can not be an
 * activity.It has to be either fragment or a layout so that it can embed in an
 * activity inside the customer application. Here fragment has been chosen
 * because of its flexibility in usage.
 * <p>
 * Sample code for developers to use Player:<br>
 * PlayerSupportFragment player = new PlayerSupportFragment();<br>
 * getFragmentManager().beginTransaction().add(R.id.container,
 * player).commit();// Embeds the player into container layout.<br>
 * player.setPlayerCallback(IPlayerCallback);<br>
 * 
 * @author kanchan
 * 
 */
public class PlayerSupportFragment extends Fragment implements OnErrorListener,OnPreparedListener, OnCompletionListener{
    private static final String TAG = PlayerSupportFragment.class.getSimpleName();
    private VideoView mPlayerView;
    private Uri mUri;
    private IPlayerCallback mPlayerCallback;
    private Logger mLogger = null;
    private int mPosition = 0;
    private RelativeLayout mPlayerLayout;
    private PlayerControl mPlayerControl;
    private MediaController mediaController;
    private PlayerState mPlayerState;
    private CountDownTimer mTimer;
    private CountDownTimer mBufferingTimer;
    private Downloader mWidevineDownloader = null;
    private String mDownloadingUrl = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        /*
         * When VideoView calls setAnchorView method or setMediaController, it
         * will use the VideoView's parent as the anchor.
         */
        /*
         * Here a layout is used as a parent for video view. So that the video
         * view layout parameters can be adjusted or modified in the layout.Like
         * its position is in center.
         */
        mPlayerLayout = new RelativeLayout(getActivity());
        mPlayerLayout.setBackgroundColor(Color.GRAY);
        mPlayerLayout.setGravity(Gravity.CENTER);
        mPlayerView = new VideoView(getActivity());
        mediaController = new MediaController(getActivity());
        mediaController.setAnchorView(mPlayerView);
        mPlayerView.setMediaController(mediaController);
        mPlayerLayout.addView(mPlayerView);
        mPlayerView.setOnErrorListener(this);
        mPlayerView.setOnCompletionListener(this);
        mLogger = LoggerUtil.getLogger(getActivity(),LoggerUtil.sLoggerName);
        Setting.initDeviceID(getActivity());
        mPlayerControl = new PlayerControl();
        return mPlayerLayout;
    }

    /**
     * Player control bar normally disappears after 3 seconds.
     * So this Method hides the player control bar instantly.
     */
    public void hideMediaController() {
        mediaController.hide();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mPlayerCallback == null) {
            try {
                mPlayerCallback = (IPlayerCallback) getActivity();
            } catch (ClassCastException e) {
                if (mLogger != null) {
                    mLogger.error("Activity Not Started");
                    mLogger.error(e.toString());
                }
                return;
            }
        }
        mPlayerCallback.playerAttached(mPlayerControl);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mPlayerView != null) {
            if(mPlayerState != PlayerState.stopped){
                mPlayerState = mPlayerView.isPlaying()?PlayerState.playing:PlayerState.paused;
                mPlayerView.pause();
                mPosition = mPlayerView.getCurrentPosition();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mPlayerView != null) {
            if(mPlayerState!= PlayerState.stopped){
                mPlayerView.seekTo(mPosition);
                if (mPlayerState == PlayerState.playing) {
                    mPlayerView.start();
                }
            }
        }
    }

    /**
     * To set the {@link IPlayerCallback} implementation.
     * @param listener
     */
    public void setPlayerCallback(IPlayerCallback listener) {
        mPlayerCallback = listener;
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        if (mLogger != null) {
            mLogger.error("Error in media player");
        }
        Log.i(getClass().getName(), "Error In Media Player" + what + ":"+ extra);
        reset();
        if (mPlayerCallback != null)
            mPlayerCallback.playerMessage(Constants.Message.error.ordinal(), what,"Error In Media Player");
        return true;
    }

    /**
     * Returns the current state of the player
     * @return int {@link PlayerState}
     */
    public int getCurrentPlayState() {
        if (mPlayerState != PlayerState.stopped) {
            if (mPlayerView.isPlaying())
                mPlayerState = PlayerState.playing;
            else
                mPlayerState = PlayerState.paused;
        }
        return mPlayerState.ordinal();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPlayerView = null;
        mPlayerLayout = null;
        mLogger = null;
        mPlayerCallback = null;
    }

    /**
     * Implementation of {@link IPlayerControl}
     * 
     * @author kanchan
     * 
     */
    private class PlayerControl implements IPlayerControl {

        //options are : media id,remote url or local url
        public void play(final String media) {
            /*if URL is valid, it can be direct remote URL ,encoding remote URL or Local content URL
            if it is encoding URL fetch URL from encoding URL map
            if direct remote URL then play
            if local content URL  then play.*/
            if(URLUtil.isValidUrl(media)){
                if(URLUtil.isContentUrl(media)){
                    try {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setVideoPath(media);
                                play();
                            }
                        });
                    } catch (Exception ex) {
                        Log.e(TAG, "Error During Playback: " + ex.getMessage());
                        reset();
                        if (mPlayerCallback != null)
                            mPlayerCallback.playerMessage(Constants.Message.error.ordinal(), 0,"Error During Playback");
                    }
                }
                //encoding remote URL
                else{
                    final Encoding encoding = ContentService.getEncodingFromUrl(media);
                    if(encoding != null){
                        if (encoding.primaryUse.equals(PrimaryUse.WidevineOffline)) {
                            downloadWidevine(encoding.mEncodingUrl.toString(),encoding.mMediaID,null);
                        } else if (encoding.primaryUse.equals(PrimaryUse.Widevine)) {
                            requestRights(encoding.mEncodingUrl.toString(), encoding.mMediaID);
                        }else {
                            try {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        setVideoUri(Uri.parse(encoding.mEncodingUrl.toString()));
                                        //play();
                                    }
                                });
                            } catch (Exception ex) {
                                Log.e(TAG, "Error During Playback: " + ex.getMessage());
                                reset();
                                if (mPlayerCallback != null)
                                    mPlayerCallback.playerMessage(Constants.Message.error.ordinal(), 0,"Error During Playback");
                            }
                        }
                    }
                    //direct remote URL
                    else{
                        try {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    setVideoPath(media);
                                }
                            });
                        } catch (Exception ex) {
                            Log.e(TAG, "Error During Playback: " + ex.getMessage());
                            reset();
                            if (mPlayerCallback != null)
                                mPlayerCallback.playerMessage(Constants.Message.error.ordinal(), 0,"Error During Playback");
                        }
                    }
                }
            }
            //it may be 3GP or just media Id
            //if media ID, fetch encodings and find suitable delivery and play
            else if(media!= null && media.trim().length()>0){
                String scheme = Uri.parse(media).getScheme();
                if(scheme!= null && scheme.equalsIgnoreCase("RTSP")){
                    setVideoPath(media);
                }else{
                    final ContentService contentService = new ContentService(getActivity());
                    contentService.getAllEncodingsForMediaId(media, new EncodingsCallback() {

                        @Override
                        public void onError(Throwable throwable) {
                            if (mPlayerCallback != null)
                                mPlayerCallback.playerMessage(Constants.Message.error.ordinal(), 0,throwable.getMessage());
                        }

                        @Override
                        public void onSuccess(ArrayList<Encoding> encodingList) {
                            final Delivery delivery = contentService.getDeliveryForMedia(encodingList);
                            if(delivery!= null){
                            if (delivery.mDownloadable) {
                                downloadWidevine(delivery.mRemoteURL.toString(),delivery.mMediaId,null);
                            } else if (delivery.mProtected) {
                                requestRights(delivery.mRemoteURL.toString(),delivery.mMediaId);
                            }else {
                                try {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            setVideoUri(Uri.parse(delivery.mRemoteURL.toString()));
//                                            play();
                                        }
                                    });
                                } catch (Exception ex) {
                                    Log.e(TAG, "Error During Playback: " + ex.getMessage());
                                    reset();
                                    if (mPlayerCallback != null)
                                        mPlayerCallback.playerMessage(Constants.Message.error.ordinal(), 0,"Error During Playback");
                                }
                            }
                            }else{
                                if (mPlayerCallback != null)
                                    mPlayerCallback.playerMessage(Constants.Message.error.ordinal(), 0,"No Proper Delivery Found");
                            
                            }
                        }
                    });
                }
            }else{
                play();
            }
        }

        private void play() {
            if (mUri == null) {
                if (mLogger != null) {
                    mLogger.warn("Please Set The Uri");
                }
                if (mPlayerCallback != null)
                    mPlayerCallback.playerMessage(Constants.Message.error.ordinal(), 0,"Invalid URI");
                return;
            }
            if (mPlayerView != null) {
                mPlayerView.start();
                mPlayerState = PlayerState.playing;
                if (mLogger != null) {
                    mLogger.info("Player started");
                    mLogger.error("Player started");
                    mLogger.fatal("Player started");
                    mLogger.debug("Player started");
                    mLogger.warn("Player started");
                }
            } else {
                if (mLogger != null) {
                    mLogger.error("Player Not Initilized");
                }
            }
        }

        public void pause() {
            if (mUri == null) {
                if (mLogger != null) {
                    mLogger.warn("Please Set The Uri");
                }
                return;
            }
            if (mPlayerView != null) {
                mPlayerView.pause();
                mPlayerState = PlayerState.paused;
            } else {
                if (mLogger != null) {
                    mLogger.error("Player Not Initilized");
                }
            }
        }

        public void stop() {
            if (mPlayerView != null){
                /*mPlayerView.stopPlayback();
                mPlayerState = PlayerState.stopped;
                mPlayerView.setVideoURI(null);
                mUri = null;*/
                reset();
            }
            else {
                if (mLogger != null) {
                    mLogger.error("Player Not Initilized");
                }
            }
        }

        public void next() {
            if (mPlayerView != null)
                mPlayerView.start();
            else {
                if (mLogger != null) {
                    mLogger.error("Player Not Initilized");
                }
            }
        }

        public void previous() {
            if (mPlayerView != null)
                mPlayerView.start();
            else {
                if (mLogger != null) {
                    mLogger.error("Player Not Initilized");
                }
            }
        }

        private void setVideoUri(Uri uri) {
            mPosition = 0;
            if (uri != null) {
                mUri = uri;
                if (mPlayerView != null) {
                    if(mPlayerState!= PlayerState.stopped){
                        mPlayerView.stopPlayback();
                        mPlayerState = PlayerState.stopped;
                    }
                    try {
                        mPlayerView.setVideoURI(mUri);
                        mPlayerView.setOnPreparedListener(PlayerSupportFragment.this);
                        startTimerForPrepare();
                    } catch (IllegalStateException e) {
                        reset();
                        if (mPlayerCallback != null)
                            mPlayerCallback.playerMessage(Constants.Message.error.ordinal(), 0,"Media Player Error");
                    }
                } else {
                    if (mLogger != null) {
                        mLogger.error("Player Not Initilized");
                    }
                }
            } else {
                if (mLogger != null) {
                    mLogger.info("Please Check The URI");
                }
            }
        }


        private void setVideoPath(String path) {
            mPosition = 0;
            if (path != null) {
                mUri = Uri.parse(path);
                if (mPlayerView != null) {
                    if(mPlayerState!= PlayerState.stopped){
                        mPlayerView.stopPlayback();
                        mPlayerState = PlayerState.stopped;
                    }
                    try {
                        mPlayerView.setVideoPath(path);
                        mPlayerView.setOnPreparedListener(PlayerSupportFragment.this);
                        startTimerForPrepare();
                    } catch (IllegalStateException e) {
                        reset();
                        if (mPlayerCallback != null)
                            mPlayerCallback.playerMessage(Constants.Message.error.ordinal(), 0,"Media Player Error");
                    }
                }
            } else {
                if (mLogger != null) {
                    mLogger.info("Please Check The URI");
                }
            }
        }

        /**
         * Method to request rights from license server and then validate 
         * using DRM engine.
         * @param uri
         * @param mediaId
         */
        private void requestRights(final String url, final String mediaId) {
            final Uri uri = Uri.parse(url.toString());
            WidevineRightsRequester requester = new WidevineRightsRequester(
                    getActivity(), uri, mediaId,
                    new WidevineRightsRequester.Callback() {
                        @Override
                        public void onSuccess() {
                            try {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        setVideoUri(uri);
                                    }
                                });
                            } catch (Exception ex) {
                                Log.e(TAG,"Error During Playback: "+ ex.getMessage());
                                reset();
                                if (mPlayerCallback != null)
                                    mPlayerCallback.playerMessage(Constants.Message.error.ordinal(), 0,"Error During Playback");
                            }
                        }
        
                        @Override
                        public void onError(Throwable throwable) {
                            reset();
                            if (mPlayerCallback != null)
                                mPlayerCallback.playerMessage(Constants.Message.error.ordinal(), 0,throwable.getMessage());
                        }
                    });
            if(!requester.mDrmInitFailed){
                if(mPlayerCallback != null)
                    mPlayerCallback.playerMessage(Constants.Message.status.ordinal(), 0, "Processing Widevine Rights!");
                requester.requestRights();
            }
        }

        /**
         * Method to download widevine content and then play it after validating the rights.
         * @param url
         * @param mediaId
         * @param saveDirLocation
         */
        private void downloadWidevine(String url, final String mediaId,String saveDirLocation) {
            mDownloadingUrl = url;
            mWidevineDownloader = new Downloader(getActivity());
            mWidevineDownloader.startDownload(url, "video/wvm", saveDirLocation, new DownLoadCallback(){

                @Override
                public void onSuccess(String path) {
                        requestRights(path, mediaId);
                }

                @Override
                public void onError(Throwable throwable) {
                    reset();
                    if (mPlayerCallback != null)
                        mPlayerCallback.playerMessage(Constants.Message.progress.ordinal(), 100,throwable.getMessage());
                }

                @Override
                public void onProgress(int percentFinished) {
                    if (mPlayerCallback != null)
                        mPlayerCallback.playerMessage(Constants.Message.progress.ordinal(), percentFinished,null);
                }
            });
        }

        @Override
        public void resume() {
            // TODO Auto-generated method stub
            
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // configureLayoutParams();
    }


    @Override
    public void onPrepared(MediaPlayer mp) {
        stopTimerForPrepare();
        if(mPlayerState == PlayerState.paused){
            mPlayerView.pause();
        }else if(mPlayerState == PlayerState.completed){
            mPlayerView.stopPlayback();
        }else{
            //Special case for 3GP url as it takes long time to play.
            if(MimeTypeMap.getFileExtensionFromUrl(mUri.toString()).equalsIgnoreCase("3gp")){
                mp.setOnInfoListener(new OnInfoListener() {
                    @Override
                    public boolean onInfo(MediaPlayer mp, int what, int extra) {
                        if(what == MediaPlayer.MEDIA_INFO_BUFFERING_START||
                                what == MediaPlayer.MEDIA_INFO_BUFFERING_END||
                                what == MediaPlayer.MEDIA_INFO_METADATA_UPDATE){
                            Log.i(TAG, "Info now"+": "+what +":"+extra);
                            mPlayerCallback.playerPrepared(mPlayerControl);
                        }
                        return true;
                    }
                });
                mp.setOnBufferingUpdateListener(new OnBufferingUpdateListener() {

                    @Override
                    public void onBufferingUpdate(MediaPlayer mp, int percent) {
                        mPlayerCallback.playerPrepared(mPlayerControl);
                        stopTimerForBuffer();
                    }
                });
                startTimerForBuffer();
            }else{
                mPlayerCallback.playerPrepared(mPlayerControl);
            }
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mPlayerState = PlayerState.completed;
    }

    private void reset(){
        mPlayerView.stopPlayback();
        mPlayerView.setVideoURI(null);
        mUri = null;
        mPlayerState = PlayerState.stopped;
        stopTimerForPrepare();
        stopTimerForBuffer();
        if(mWidevineDownloader!= null){
            mWidevineDownloader.cancelDownload(mDownloadingUrl);
        }
    }
    /**
     * Method to start timer for buffering the media data.<br>
     * The timer kicks its callback methods on completion to terminate the player 
     * preparation and send message to developer application.
     */
    private void startTimerForBuffer() {
        mBufferingTimer = new CountDownTimer(Constants.BUFFERING_TIMEOUT, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                reset();
                if (mPlayerCallback != null)
                    mPlayerCallback.playerMessage(Constants.Message.error.ordinal(), 0,"Timeout Buffering Media");
            }
        };
        mBufferingTimer.start();
    }

    /**
     * Method to stop timer for buffering.
     */
    private void stopTimerForBuffer() {
        if(mBufferingTimer != null)
            mBufferingTimer.cancel();
    }

    /**
     * Method to start timer for fetching the media data.<br>
     * The timer kicks its callback methods on completion to terminate the player 
     * preparation and send message to developer application.
     */
    private void startTimerForPrepare() {
        mTimer = new CountDownTimer(Constants.PREPARING_TIMEOUT, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                reset();
                if (mPlayerCallback != null)
                    mPlayerCallback.playerMessage(Constants.Message.error.ordinal(), 0,"Timeout Fetching Media");
            }
        };
        mTimer.start();
    }

    /**
     * Method to stop timer.
     */
    private void stopTimerForPrepare() {
        if(mTimer != null)
            mTimer.cancel();
    }
}