package com.limelight.videosdk;

import java.util.ArrayList;
import org.apache.log4j.Logger;
import com.limelight.videosdk.Constants.PlayerState;
import com.limelight.videosdk.ContentService.EncodingsCallback;
import com.limelight.videosdk.MediaControl.FullScreenCallback;
import com.limelight.videosdk.WidevineManager.WVCallback;
import com.limelight.videosdk.model.Delivery;
import com.limelight.videosdk.model.Encoding;
import com.limelight.videosdk.model.PrimaryUse;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.app.Fragment;
import android.content.IntentFilter;
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
import android.support.v4.content.LocalBroadcastManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;
import android.widget.RelativeLayout;
import android.widget.Toast;

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
 * PlayerFragment gets the list of channels groups, channels,media and 
 * encodings by calling ContentService methods.
 * <P>
 * PlayerFragment also has a countdown timer.When player crosses a certain 
 * time in fetching the media data, this timer kicks its callback methods to terminate the player preparation and send message to developer application.

 * Since the Player has to be an embed-able component, Player can not be an
 * activity.It has to be either fragment or a layout so that it can embed in an
 * activity inside the customer application. Here fragment has been chosen
 * because of its flexibility in usage.
 * <p>
 * Sample code for developers to use Player:<br>
 * PlayerFragment player = new PlayerFragment();<br>
 * getFragmentManager().beginTransaction().add(R.id.container,
 * player).commit();// Embeds the player into container layout.<br>
 * player.setPlayerCallback(IPlayerCallback);<br>
 * 
 * @author kanchan
 * 
 */
public class PlayerFragment extends Fragment implements OnErrorListener,OnPreparedListener, OnCompletionListener,IMediaControllerCallback{
    private static final String TAG = PlayerSupportFragment.class.getSimpleName();
    private VideoPlayerView mPlayerView;
    private Uri mUri;
    private IPlayerCallback mPlayerCallback;
    private Logger mLogger = null;
    private int mPosition = 0;
    private RelativeLayout mPlayerLayout;
    private PlayerControl mPlayerControl;
    private MediaControl mMediaController;
    private CountDownTimer mTimer;
    private CountDownTimer mBufferingTimer;
    private WidevineManager mWidevineManager = null;
    private AnalyticsReporter mReporter = null;
    private String mMediaId = null;

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
        mPlayerLayout.setBackgroundColor(Color.BLACK);
        mPlayerLayout.setGravity(Gravity.CENTER);
        mPlayerView = new VideoPlayerView(getActivity());
        mMediaController = new MediaControl(getActivity(), true);
        final Toast toast = Toast.makeText(getActivity(), "Please Add FullScreenPlayer Activity In Manifest !", Toast.LENGTH_LONG);
        mMediaController.setFullScreenCallback(new FullScreenCallback() {
            @Override
            public void fullScreen() {
                Intent i = new Intent(getActivity(),FullScreenPlayer.class);
                i.putExtra("URI",mUri.toString());
                i.putExtra("POSITION",mPlayerView.getCurrentPosition());
                i.putExtra("STATE",mPlayerView.mPlayerState.name());
                try{
                    getActivity().startActivity(i);
                    mPlayerControl.pause();
                }catch(Exception ex){
                    if(ex != null)
                        mLogger.error(ex.getMessage());
                    if(!toast.getView().isShown()){
                        toast.show();
                    }
                }
            }
            @Override
            public void closeFullScreen() {
            }
        },true);
        mMediaController.setAnchorView(mPlayerView);
        mPlayerView.setMediaController(mMediaController);
        mPlayerLayout.addView(mPlayerView);
        mPlayerView.setOnErrorListener(this);
        mPlayerView.setOnCompletionListener(this);
        mPlayerView.setMediaControllerCallback(this);
        mLogger = LoggerUtil.getLogger(getActivity(),LoggerUtil.sLoggerName);
        mReporter = new AnalyticsReporter(getActivity());
        mPlayerControl = new PlayerControl();
        return mPlayerLayout;
    }

    /**
     * Player control bar normally disappears after 3 seconds.
     * So this Method hides the player control bar instantly.
     */
    public void hideMediaController() {
        if(mMediaController != null)
            mMediaController.hide();
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
                }
                return;
            }
        }
        mReporter.sendStartSession();
        mPlayerCallback.playerAttached(mPlayerControl);
        IntentFilter filter = new IntentFilter("limelight.intent.action.PLAY_FULLSCREEN");
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mPlayerView.mPlayerState = PlayerState.valueOf(intent.getStringExtra("STATE"));
                mPosition  = intent.getIntExtra("POSITION",0);
            }
        }, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mPlayerView != null) {
            if(mPlayerView.mPlayerState != PlayerState.stopped){
                mPlayerView.mPlayerState = mPlayerView.isPlaying()?PlayerState.playing:PlayerState.paused;
                mPlayerView.pause();
                mPosition = mPlayerView.getCurrentPosition();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mPlayerView != null) {
            if(mPlayerView.mPlayerState!= PlayerState.stopped){
                if(mPlayerView.canSeekBackward() || mPlayerView.canSeekForward()){
                    mPlayerView.seekTo(mPosition);
                    if (mPlayerView.mPlayerState == PlayerState.playing) {
                        mPlayerView.start();
                    }
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
            mLogger.error("Error in media player" + what + ":"+ extra);
        }
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
        if (mPlayerView.mPlayerState != PlayerState.stopped) {
            if (mPlayerView.isPlaying())
                mPlayerView.mPlayerState = PlayerState.playing;
            else if(mPlayerView.mPlayerState != PlayerState.completed)
                mPlayerView.mPlayerState = PlayerState.paused;
        }
        return mPlayerView.mPlayerState.ordinal();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        reset();
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
        public void play(final String media, final ContentService contentService) {
            /*if URL is valid, it can be direct remote URL ,encoding remote URL or Local content URL
            if it is encoding URL fetch URL from encoding URL map
            if direct remote URL then play
            if local content URL  then play.*/
            if (mLogger != null) {
                mLogger.debug(TAG+" PlayerState:"+mPlayerView.mPlayerState.name());
                mLogger.debug(TAG+" Media play:"+ media);
            }
            if(media != null && mPlayerView != null && mPlayerView.mPlayerState!= PlayerState.stopped){
                mPlayerView.stopPlayback();
                mPlayerView.mPlayerState = PlayerState.stopped;
            }
            if(URLUtil.isValidUrl(media)){
                //Local content URL
                if(URLUtil.isContentUrl(media)){
                    if (mLogger != null) {
                        mLogger.debug(TAG+" Local Content: "+media);
                    }
                    try {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setVideoPath(media);
                                play();
                            }
                        });
                    } catch (Exception ex) {
                        playerError();
                    }
                }
                //encoded remote URL or direct remote url
                else{
                    if (mPlayerCallback != null)
                        mPlayerCallback.playerMessage(Constants.Message.status.ordinal(), 0,"Fetching Media From Server !");
                    if(contentService != null){
                        Encoding encoding = contentService.getEncodingFromUrl(media);
                        if(encoding != null){
                            mMediaId = encoding.mMediaID;
                            if (PrimaryUse.WidevineOffline.equals(encoding.primaryUse)||PrimaryUse.Widevine.equals(encoding.primaryUse)) {
                                mWidevineManager = new WidevineManager(getActivity(),media,contentService);
                                mWidevineManager.playWidewineEncodedContent(encoding, new WVCallback() {
                                    @Override
                                    public void onSuccess(final String path) {
                                        if (mLogger != null) {
                                            mLogger.debug(TAG+" Remote widevine Content: path : "+path);
                                        }
                                        try {
                                            getActivity().runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    setVideoPath(path);
                                                }
                                            });
                                        } catch (Exception ex) {
                                            playerError();
                                        }
                                    }

                                    @Override
                                    public void onError(Throwable throwable) {
                                        reset();
                                        if (mPlayerCallback != null)
                                            mPlayerCallback.playerMessage(Constants.Message.error.ordinal(), 0,throwable.getMessage());
                                    }

                                    @Override
                                    public void onProgress(int percentFinished) {
                                        if (mPlayerCallback != null)
                                            mPlayerCallback.playerMessage(Constants.Message.progress.ordinal(), percentFinished,null);
                                    }

                                    @Override
                                    public void onSendMessage(String message) {
                                        if(mPlayerCallback != null)
                                            mPlayerCallback.playerMessage(Constants.Message.status.ordinal(), 0, message);
                                    }
                                });
                            } else {
                                if(null != encoding.mEncodingUrl){
                                    final String url = encoding.mEncodingUrl.toString();
                                    if (mLogger != null) {
                                        mLogger.debug(TAG+" Encoding remote URL: " + url);
                                    }
                                    try {
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                setVideoUri(Uri.parse(url));
                                                //play();
                                            }
                                        });
                                    } catch (Exception ex) {
                                        playerError();
                                    }
                                }else{
                                    if (mLogger != null) {
                                        mLogger.debug(TAG+" Encoding Remote url is null");
                                    }
                                    if (mPlayerCallback != null)
                                        mPlayerCallback.playerMessage(Constants.Message.error.ordinal(), 0,"InValid Media !");
                                }
                            }
                        }else{
                            String mimetype = MimeTypeMap.getFileExtensionFromUrl(media);
                            if("wvm".equalsIgnoreCase(mimetype)){
                                if (mLogger != null) {
                                    mLogger.debug(TAG+" widevine direct url Content: "+media);
                                }
                                Delivery delivery =  new Delivery();
                                delivery.mRemoteURL = Uri.parse(media);
                                String[] paths = media.split("/");
                                delivery.mMediaId = paths[5];
                                if (mLogger != null) {
                                    mLogger.debug(TAG+" Local widevine Content: media Id : "+delivery.mMediaId);
                                }
                                delivery.mProtected = true;
                                mWidevineManager = new WidevineManager(getActivity(), media,contentService);
                                mWidevineManager.playWidewineDeliveryContent(delivery,new WVCallback() {
                                    @Override
                                    public void onSuccess(final String path) {
                                        if (mLogger != null) {
                                            mLogger.debug(TAG+" Delivery widevine media path" + path);
                                        }
                                        try {
                                            getActivity().runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    setVideoPath(path);
                                                }
                                            });
                                        } catch (Exception ex) {
                                            playerError();
                                        }
                                    }

                                    @Override
                                    public void onError(Throwable throwable) {
                                        reset();
                                        if (mPlayerCallback != null)
                                            mPlayerCallback.playerMessage(Constants.Message.error.ordinal(), 0,throwable.getMessage());
                                    }

                                    @Override
                                    public void onProgress(int percentFinished) {
                                        if (mPlayerCallback != null)
                                            mPlayerCallback.playerMessage(Constants.Message.progress.ordinal(), percentFinished,null);
                                    }

                                    @Override
                                    public void onSendMessage(String message) {
                                        if(mPlayerCallback != null)
                                            mPlayerCallback.playerMessage(Constants.Message.status.ordinal(), 0, message);
                                    }
                                });
                            }
                            else {
                                //this is direct remote URL
                                try {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            setVideoPath(media);
                                            return;
                                        }
                                    });
                                } catch (Exception ex) {
                                    playerError();
                                    return;
                                }
                            }
                        }
                    }else{
                        //play directly
                        try {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    setVideoPath(media);
                                    return;
                                }
                            });
                        } catch (Exception ex) {
                            playerError();
                            return;
                        }
                    }
                }
            }
            //it may be 3GP or just media Id
            //if media ID, fetch encodings and find suitable delivery and play
            else if(media!= null && media.trim().length()>0){
                if (mPlayerCallback != null)
                    mPlayerCallback.playerMessage(Constants.Message.status.ordinal(), 0,"Fetching Media From Server !");
                String scheme = Uri.parse(media).getScheme();
                if (mLogger != null) {
                    mLogger.debug(TAG+" 3gp or delivery media : " + media);
                }
                if("RTSP".equalsIgnoreCase(scheme)){
                    setVideoPath(media);
                }else{
                    if (mLogger != null) {
                        mLogger.debug(TAG+" Fetch encodings to get delivery for media id:" + media);
                    }
                    if(contentService == null){
                        if (mLogger != null) {
                            mLogger.debug(TAG+" media URL : "+media);
                        }
                        if (mPlayerCallback != null)
                            mPlayerCallback.playerMessage(Constants.Message.error.ordinal(), 0,"No Media Library !");
                        return;
                    }
                    contentService.getAllEncodingsForMediaId(media, new EncodingsCallback() {

                        @Override
                        public void onError(Throwable throwable) {
                            if (mPlayerCallback != null)
                                mPlayerCallback.playerMessage(Constants.Message.error.ordinal(), 0,throwable.getMessage());
                        }

                        @Override
                        public void onSuccess(ArrayList<Encoding> encodingList) {
                            mMediaId = media;
                            final Delivery delivery = contentService.getDeliveryForMedia(encodingList);
                            if(delivery!= null){
                                if (delivery.mProtected) {
                                    if (mLogger != null) {
                                        mLogger.debug(TAG+" Delivery is widevine" + media);
                                    }
                                    mWidevineManager = new WidevineManager(getActivity(), media,contentService);
                                    mWidevineManager.playWidewineDeliveryContent(delivery,new WVCallback() {
                                        @Override
                                        public void onSuccess(final String path) {
                                            if (mLogger != null) {
                                                mLogger.debug(TAG+" Delivery widevine media path" + path);
                                            }
                                            try {
                                                getActivity().runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        setVideoPath(path);
                                                    }
                                                });
                                            } catch (Exception ex) {
                                                playerError();
                                            }
                                        }

                                        @Override
                                        public void onError(Throwable throwable) {
                                            reset();
                                            if (mPlayerCallback != null)
                                                mPlayerCallback.playerMessage(Constants.Message.error.ordinal(), 0,throwable.getMessage());
                                        }

                                        @Override
                                        public void onProgress(int percentFinished) {
                                            if (mPlayerCallback != null)
                                                mPlayerCallback.playerMessage(Constants.Message.progress.ordinal(), percentFinished,null);
                                        }

                                        @Override
                                        public void onSendMessage(String message) {
                                            if(mPlayerCallback != null)
                                                mPlayerCallback.playerMessage(Constants.Message.status.ordinal(), 0, message);
                                        }
                                    });
                                }
                                else {
                                    try {
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                setVideoUri(Uri.parse(delivery.mRemoteURL.toString()));
                                                //play();
                                            }
                                        });
                                    } catch (Exception ex) {
                                        playerError();
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
                if (mLogger != null) {
                    mLogger.debug(TAG+" Simply play");
                }
                play();
            }
        }

        private void playerError() {
            if (mLogger != null) {
                mLogger.debug(TAG+" PlayerState: play"+mPlayerView.mPlayerState.name());
                mLogger.debug(TAG+" Error during playback");
            }
            reset();
            if (mPlayerCallback != null)
                mPlayerCallback.playerMessage(Constants.Message.error.ordinal(), 0,"Error During Playback");
            
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
                mPlayerView.mPlayerState = PlayerState.playing;
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
            if (mLogger != null) {
                mLogger.debug(TAG+" PlayerState: play :"+mPlayerView.mPlayerState.name());
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
                mPlayerView.mPlayerState = PlayerState.paused;
            } else {
                if (mLogger != null) {
                    mLogger.error("Player Not Initilized");
                }
            }
            if (mLogger != null) {
                mLogger.debug(TAG+" PlayerState: pause: "+mPlayerView.mPlayerState.name());
            }
        }

        public void stop() {
            if (mPlayerView != null){
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
                    if(mPlayerView.mPlayerState!= PlayerState.stopped){
                        mPlayerView.stopPlayback();
                        mPlayerView.mPlayerState = PlayerState.stopped;
                    }
                    try {
                        mPlayerView.setVideoURI(mUri);
                        mPlayerView.setOnPreparedListener(PlayerFragment.this);
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
                if (mLogger != null) {
                    mLogger.debug(TAG+" Video path: "+mUri.toString());
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
                    if(mPlayerView.mPlayerState!= PlayerState.stopped){
                        mPlayerView.stopPlayback();
                        mPlayerView.mPlayerState = PlayerState.stopped;
                    }
                    try {
                        mPlayerView.setVideoPath(path);
                        mPlayerView.setOnPreparedListener(PlayerFragment.this);
                        startTimerForPrepare();
                    } catch (IllegalStateException e) {
                        reset();
                        if (mPlayerCallback != null)
                            mPlayerCallback.playerMessage(Constants.Message.error.ordinal(), 0,"Media Player Error");
                    }
                }
                if (mLogger != null) {
                    mLogger.debug(TAG+" Video path: "+mUri.toString());
                }
            } else {
                if (mLogger != null) {
                    mLogger.info("Please Check The URI");
                }
            }
        }

        @Override
        public void resume() {
            mPlayerView.resume();
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        stopTimerForPrepare();
        if(mPlayerView.mPlayerState == PlayerState.paused){
            mPlayerView.pause();
        }else if(mPlayerView.mPlayerState == PlayerState.completed){
            if(mPlayerView.getDuration() == -1)
                mPlayerView.stopPlayback();
        }
        else{
            //Special case for 3GP url as it takes long time to play.
            if(!(URLUtil.isContentUrl(mUri.toString())) && "3gp".equalsIgnoreCase(MimeTypeMap.getFileExtensionFromUrl(mUri.toString()))){
                if (mLogger != null) {
                    mLogger.debug(TAG+" 3GP case Uri:" + mUri.toString());
                }
                mp.setOnInfoListener(new OnInfoListener() {
                    @Override
                    public boolean onInfo(MediaPlayer mp, int what, int extra) {
                        if(what == MediaPlayer.MEDIA_INFO_BUFFERING_START||
                                what == MediaPlayer.MEDIA_INFO_BUFFERING_END||
                                what == MediaPlayer.MEDIA_INFO_METADATA_UPDATE){
                            if (mLogger != null) {
                                mLogger.debug(TAG+" Info now"+": "+what +":"+extra);
                            }
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
        if (mLogger != null) {
            mLogger.debug(TAG+" PlayerState:"+mPlayerView.mPlayerState.name());
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (mLogger != null) {
            mLogger.debug(TAG+" PlayerState:"+mPlayerView.mPlayerState.name());
        }
        mPlayerView.mPlayerState = PlayerState.completed;
        mReporter.sendMediaComplete(mMediaId,null);
        mPlayerCallback.playerMessage(Constants.Message.status.ordinal(), Constants.PlayerState.completed.ordinal(),null);
    }

    /**
     * Stops the playback.Clears the video path set in player.
     * Stops the timers running for buffering or preparing if any.
     * It also stops the wide vine media downloading.
     */
    private void reset(){
        if (mLogger != null) {
            mLogger.debug(TAG+" Reset Called");
        }
        if(mPlayerView !=null){
            mPlayerView.stopPlayback();
            mPlayerView.setVideoURI(null);
        }
        mUri = null;
        if(mPlayerView !=null){
            mPlayerView.mPlayerState = PlayerState.stopped;
        }
        stopTimerForPrepare();
        stopTimerForBuffer();
        if(mWidevineManager!= null){
            mWidevineManager.cancelDownload();
        }
        if (mLogger != null) {
            mLogger.debug(TAG+" PlayerState:"+mPlayerView.mPlayerState.name());
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
        if (mLogger != null) {
            mLogger.debug(TAG+" Started Timer Buffer");
        }
    }

    /**
     * Method to stop timer for buffering.
     */
    private void stopTimerForBuffer() {
        if(mBufferingTimer != null){
            if (mLogger != null) {
                mLogger.debug(TAG+" Stopped Timer Buffer");
            }
            mBufferingTimer.cancel();
        }
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
        if (mLogger != null) {
            mLogger.debug(TAG+" Start Timer Prepare");
        }
    }

    /**
     * Method to stop timer.
     */
    private void stopTimerForPrepare() {
        if(mTimer != null){
            if (mLogger != null) {
                mLogger.debug(TAG+" Stopped Timer Prepare");
            }
            mTimer.cancel();
        }
    }

    @Override
    public void onMediaControllerPlay(long position) {
        mReporter.sendPlayWithPosition(position,mMediaId,null);
    }

    @Override
    public void onMediaControllerPause(long position) {
        mReporter.sendPauseWithPosition(position,mMediaId,null);
    }

    @Override
    public void onMediaControllerSeek(long positionBefore,long positionAfter) {
        mReporter.sendSeekWithPositionBefore(positionBefore, positionAfter,mMediaId,null);
    }
}