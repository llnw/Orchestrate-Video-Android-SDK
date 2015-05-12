package com.limelight.videosdk;

import java.util.ArrayList;
import org.apache.log4j.Logger;
import com.limelight.videosdk.Constants.PlayerState;
import com.limelight.videosdk.ContentService.EncodingsCallback;
import com.limelight.videosdk.ContentService.MediaCallback;
import com.limelight.videosdk.MediaControl.FullScreenCallback;
import com.limelight.videosdk.WidevineManager.WVCallback;
import com.limelight.videosdk.model.Delivery;
import com.limelight.videosdk.model.Encoding;
import com.limelight.videosdk.model.Media;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
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
    private Logger mLogger;
    private int mPosition;
    private RelativeLayout mPlayerLayout;
    private PlayerControl mPlayerControl;
    private MediaControl mMediaController;
    private CountDownTimer mTimer;
    private CountDownTimer mBufferingTimer;
    private WidevineManager mWidevineManager;
    private AnalyticsReporter mReporter;
    private String mMediaId = null;
    private boolean mIsMediacontrollerRemoved = true;
    private String mPlaylistId;
    private ContentService mPlaylistContentSvc;
    private boolean mIsAutoPlay = true;
    private int mCurrentPlayPos;
    private boolean isPlaylistPlaying;

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
        mMediaController.setAnchorView(mPlayerView);//Not setting the media controller, setting media controller here results in issue when resumed back from home key press.
        final Toast toast = Toast.makeText(getActivity(), "Please Add FullScreenPlayer Activity In Manifest !", Toast.LENGTH_LONG);
        mMediaController.setFullScreenCallback(new FullScreenCallback() {
            @Override
            public void fullScreen() {
                Intent i = new Intent(getActivity(),FullScreenPlayer.class);
                i.putExtra("URI",mUri.toString());
                i.putExtra("POSITION",mPlayerView.getCurrentPosition());
                i.putExtra("STATE",mPlayerView.mPlayerState.name());
                i.putExtra("MEDIAID",mMediaId);
                try{
                    if(mPlayerView != null && mPlayerView.mPlayerState!= PlayerState.stopped){
                        mPlayerView.stopPlayback();
                        mPlayerView.mPlayerState = PlayerState.stopped;
                    }
                    
                    getActivity().startActivity(i);
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
        mPlayerLayout.addView(mPlayerView);
        mPlayerView.setOnErrorListener(this);
        mPlayerView.setOnCompletionListener(this);
        mLogger = LoggerUtil.getLogger(getActivity());
        mReporter = new AnalyticsReporter(getActivity());
        mPlayerControl = new PlayerControl();
        mPlayerView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(final View arg0, final MotionEvent arg1) {
                if(mIsMediacontrollerRemoved){
                    mPlayerView.setMediaController(mMediaController);
                    mIsMediacontrollerRemoved = false;
                }
                return false;
            }
        });
        return mPlayerLayout;
    }

    /**
     * Player control bar normally disappears after 3 seconds.
     * So this Method hides the player control bar instantly.
     */
    public void hideMediaController() {
        if(mMediaController != null)
        {
            mMediaController.hide();
            mPlayerView.setMediaController(null);
            mIsMediacontrollerRemoved = true;
        }
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
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
                PlayerState state = mPlayerView.isPlaying()?PlayerState.playing:PlayerState.paused;
                mPlayerView.pause();
                mPlayerView.mPlayerState = state;//store the old state
                mPosition = mPlayerView.getCurrentPosition();
                hideMediaController();
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
    public boolean onError(final MediaPlayer mediaPlayer, final int what, final int extra) {
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

    /**
     * Implementation of {@link IPlayerControl}
     * 
     * @author kanchan
     * 
     */
    private class PlayerControl implements IPlayerControl {

        /**
         * This method is to play the local file.
         * @param localURL The content URL to the local file.
         */
        private void playLocalURL(final String localURL) {
            mMediaId = null;
            if (mLogger != null) {
                mLogger.debug(TAG+" Local Content: "+localURL);
            }
            try {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setVideoPath(localURL);
                    }
                });
            } catch (Exception ex) {
                playerError();
            }
        }

        /**
         * This method is to play the remote URL.
         * @param remoteURL The remote URL to the remote file.
         * @param contentService The ContentService object.
         */
        private void playRemoteURL(final String remoteURL, final ContentService contentService) {

            if (mPlayerCallback != null)
                mPlayerCallback.playerMessage(Constants.Message.status.ordinal(), 0,"Fetching Media From Server !");
            if(contentService != null){
                Encoding encoding = contentService.getEncodingFromUrl(remoteURL);
                if(encoding != null){
                    mMediaId = encoding.mMediaID;
                    if (PrimaryUse.WidevineOffline.equals(encoding.primaryUse)||PrimaryUse.Widevine.equals(encoding.primaryUse)) {
                        mWidevineManager = new WidevineManager(getActivity(),contentService);
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
                            public void onError(final Throwable throwable) {
                                reset();
                                if (mPlayerCallback != null)
                                    mPlayerCallback.playerMessage(Constants.Message.error.ordinal(), 0,throwable.getMessage());
                            }

                            @Override
                            public void onProgress(final int percentFinished) {
                                if (mPlayerCallback != null)
                                    mPlayerCallback.playerMessage(Constants.Message.progress.ordinal(), percentFinished,null);
                            }

                            @Override
                            public void onSendMessage(final String message) {
                                if(mPlayerCallback != null)
                                    mPlayerCallback.playerMessage(Constants.Message.status.ordinal(), 0, message);
                            }
                        });
                    } else {
                        if(encoding.mEncodingUrl == null){
                            if (mLogger != null) {
                                mLogger.debug(TAG+" Encoding Remote url is null");
                            }
                            if (mPlayerCallback != null)
                                mPlayerCallback.playerMessage(Constants.Message.error.ordinal(), 0,"Invalid Media !");
                        }else {
                            final String url = encoding.mEncodingUrl.toString();
                            if (mLogger != null) {
                                mLogger.debug(TAG+" Encoding remote URL: " + url);
                            }
                            try {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        setVideoUri(Uri.parse(url));
                                    }
                                });
                            } catch (Exception ex) {
                                playerError();
                            }
                        }
                    }
                }else{
                    final String mimetype = MimeTypeMap.getFileExtensionFromUrl(remoteURL);
                    if("wvm".equalsIgnoreCase(mimetype)){
                        if (mLogger != null) {
                            mLogger.debug(TAG+" widevine direct url Content: "+remoteURL);
                        }
                        Delivery delivery =  new Delivery();
                        delivery.mRemoteURL = Uri.parse(remoteURL);
                        String[] paths = remoteURL.split("/");
                        delivery.mMediaId = paths[5];
                        if (mLogger != null) {
                            mLogger.debug(TAG+" Local widevine Content: media Id : "+delivery.mMediaId);
                        }
                        delivery.mProtected = true;
                        mWidevineManager = new WidevineManager(getActivity(),contentService);
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
                            public void onError(final Throwable throwable) {
                                reset();
                                if (mPlayerCallback != null)
                                    mPlayerCallback.playerMessage(Constants.Message.error.ordinal(), 0,throwable.getMessage());
                            }

                            @Override
                            public void onProgress(final int percentFinished) {
                                if (mPlayerCallback != null)
                                    mPlayerCallback.playerMessage(Constants.Message.progress.ordinal(), percentFinished,null);
                            }

                            @Override
                            public void onSendMessage(final String message) {
                                if(mPlayerCallback != null)
                                    mPlayerCallback.playerMessage(Constants.Message.status.ordinal(), 0, message);
                            }
                        });
                    }
                    else {
                        mMediaId = null;
                        //this is direct remote URL
                        try {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    setVideoPath(remoteURL);
                                }
                            });
                        } catch (Exception ex) {
                            playerError();
                            return;
                        }
                    }
                }
            }else{
                mMediaId = null;
                //play directly
                try {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setVideoPath(remoteURL);
                        }
                    });
                } catch (Exception ex) {
                    playerError();
                    return;
                }
            }
        }

        /**
         * This method is to play the given RTSP url this is for 3GP.
         * @param rtspURL The rtsp UTL of the content to play.
         * @param contentService The ContentService object.
         */
        private void playRTSP(final String rtspURL) {
            if (mPlayerCallback != null)
                mPlayerCallback.playerMessage(Constants.Message.status.ordinal(), 0,"Fetching Media From Server !");
            if (mLogger != null) {
                mLogger.debug(TAG+" 3gp media : " + rtspURL);
            }
            setVideoPath(rtspURL);
        }

        /**
         * This method is to play the given mediaID.
         * @param mediaID The mediaID of the content to play.
         * @param contentService The ContentService object.
         */
        private void playMediaID(final String mediaID, final ContentService contentService) {
            mPlayerView.setMediaControllerCallback(PlayerFragment.this);
            if (mPlayerCallback != null)
                mPlayerCallback.playerMessage(Constants.Message.status.ordinal(), 0,"Fetching Media From Server !");
            if (mLogger != null) {
                mLogger.debug(TAG+" delivery media : " + mediaID);
            }
            if (mLogger != null) {
                mLogger.debug(TAG+" Fetch encodings to get delivery for media id:" + mediaID);
            }
            if(contentService == null){
                if (mLogger != null) {
                    mLogger.debug(TAG+" media URL : "+mediaID);
                }
                if (mPlayerCallback != null)
                    mPlayerCallback.playerMessage(Constants.Message.error.ordinal(), 0,"No Media Library !");
                return;
            }
            contentService.getAllEncodingsForMediaId(mediaID, new EncodingsCallback() {

                @Override
               public void onError(final Throwable throwable) {
                        if (mPlayerCallback != null)
                            mPlayerCallback.playerMessage(Constants.Message.error.ordinal(), 0,throwable.getMessage());
                    }

                    @Override
                    public void onSuccess(ArrayList<Encoding> encodingList) {
                        mMediaId = mediaID;
                        final Delivery delivery = contentService.getDeliveryForMedia(encodingList);
                        if(delivery!= null){
                            if (delivery.mProtected) {
                                if (mLogger != null) {
                                    mLogger.debug(TAG+" Delivery is widevine" + mediaID);
                                }
                                mWidevineManager = new WidevineManager(getActivity(),contentService);
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
                                public void onError(final Throwable throwable) {
                                        reset();
                                        if (mPlayerCallback != null)
                                            mPlayerCallback.playerMessage(Constants.Message.error.ordinal(), 0,throwable.getMessage());
                                }

                                @Override
                                public void onProgress(final int percentFinished) {
                                        if (mPlayerCallback != null)
                                            mPlayerCallback.playerMessage(Constants.Message.progress.ordinal(), percentFinished,null);
                                    }

                                @Override
                                public void onSendMessage(final String message) {
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

        /**
         * This method is to play the media.
         * @param media The media string which can hold mediaID,remote URL or local URL.
         * @param contentService The ContentService object.
         */
        @Override
        public void play(final String media, final ContentService contentService) {
            /*if URL is valid, it can be direct remote URL ,encoding remote URL or Local content URL
            if it is encoding URL fetch URL from encoding URL map
            if direct remote URL then play
            if local content URL  then play.*/
            if (mLogger != null) {
                mLogger.debug(TAG+Constants.PLAYER_STATE_STRING+mPlayerView.mPlayerState.name());
                mLogger.debug(TAG+" Media play:"+ media);
            }
            mPlayerView.setMediaControllerCallback(null);
            if(media!= null && !media.trim().isEmpty()){
                if(mPlayerView != null && mPlayerView.mPlayerState!= PlayerState.stopped){
                    mPlayerView.stopPlayback();
                    mPlayerView.mPlayerState = PlayerState.stopped;
                }

                if(URLUtil.isValidUrl(media)){
                    //Local content URL
                    if(URLUtil.isContentUrl(media)){
                        playLocalURL(media);
                    }
                    //encoded remote URL or direct remote url
                    else{
                        playRemoteURL(media, contentService);
                    }
                }
                //it may be 3GP or just media Id
                //if media ID, fetch encodings and find suitable delivery and play
                else{
                    String scheme = Uri.parse(media).getScheme();
                    if (mLogger != null) {
                        mLogger.debug(TAG+" 3gp or delivery media : " + media);
                    }
                    if("RTSP".equalsIgnoreCase(scheme)){
                        playRTSP(media);
                    }
                    else
                    {
                        playMediaID(media, contentService);
                    }
                }
            }
            else{
                if (mPlayerCallback != null)
                    mPlayerCallback.playerMessage(Constants.Message.error.ordinal(), 0,"Invalid Media!");
            }
            //clearing out playlist informations start
            isPlaylistPlaying = false;
            mCurrentPlayPos = 0;
            mPlaylistContentSvc = null;
            mPlaylistId = null;
            //clearing out playlist informations end
        }

        @Override
        public void setAutoPlay(boolean isAutoPlay){
            mIsAutoPlay = isAutoPlay;
        }

        @Override
        public int getPlaylistPosition(){
            return mCurrentPlayPos;
        }

        /* (non-Javadoc)
         * @see com.limelight.videosdk.IPlayerControl#playChannel(java.lang.String, com.limelight.videosdk.ContentService, int, com.limelight.videosdk.IPlaylistCallback)
         */
        @Override
        public void playChannel(String channelId, final ContentService contentService,final IPlaylistCallback callback){

            if(contentService == null){
                if (mPlayerCallback != null){
                    mPlayerCallback.playerMessage(Constants.Message.error.ordinal(), 0,"No Media Library !");
                }
                return;
            }
            if(callback == null){
                if (mPlayerCallback != null){
                    mPlayerCallback.playerMessage(Constants.Message.error.ordinal(), 0,"No Callback Provided !");
                }
                return;
            }
            if(channelId == null){
                if (mPlayerCallback != null){
                    mPlayerCallback.playerMessage(Constants.Message.error.ordinal(), 0,"Invalid Channel !");
                }
                return;
            }

            if (mPlayerCallback != null){
                mPlayerCallback.playerMessage(Constants.Message.status.ordinal(), 0,"Fetching Media From Server !");
            }

            if (mLogger != null) {
                mLogger.debug(TAG+" Channel Id For Playlist : "+channelId);
            }

            if(mPlayerView != null && mPlayerView.mPlayerState!= PlayerState.stopped){
                mPlayerView.stopPlayback();
                mPlayerView.mPlayerState = PlayerState.stopped;
            }

            mCurrentPlayPos = 0;

            if(channelId.equals(mPlaylistId)){
                if(mPlaylistContentSvc!= null &&  !mPlaylistContentSvc.getMediaList().isEmpty()
                        && mPlaylistContentSvc.getMediaList().get(mCurrentPlayPos)!= null){
                    playMediaID(mPlaylistContentSvc.getMediaList().get(mCurrentPlayPos).mMediaID,mPlaylistContentSvc);
                    callback.getChannelPlaylist(mPlaylistContentSvc.getMediaList());
                }
                else{
                    fetchPlaylist(channelId, callback);
                }
            }else{
                mPlaylistId = channelId;
                mPlaylistContentSvc = contentService;
                isPlaylistPlaying = false;//reset to initial
                fetchPlaylist(channelId, callback);
            }
        }

        @Override
        public void playInPlaylist(int position){
            if(mPlaylistContentSvc!= null &&  !mPlaylistContentSvc.getMediaList().isEmpty()
                    && mPlaylistContentSvc.getMediaList().get(mCurrentPlayPos)!= null){
                if(mPlayerView != null && mPlayerView.mPlayerState!= PlayerState.stopped){
                    mPlayerView.stopPlayback();
                    mPlayerView.mPlayerState = PlayerState.stopped;
                }
                mCurrentPlayPos = position;
                playMediaID(mPlaylistContentSvc.getMediaList().get(mCurrentPlayPos).mMediaID,mPlaylistContentSvc);
            }
        }
        /**
         * Method to fetch the media list belonging to specified channel.
         * @param channelId
         * @param callback
         */
        private void fetchPlaylist(final String channelId,final IPlaylistCallback callback){
            mPlaylistContentSvc.setPagingParameters(50, Constants.SORT_BY_CREATE_DATE, Constants.SORT_ORDER_ASC);
            mPlaylistContentSvc.getAllMediaOfChannelAsync(channelId, false, new MediaCallback() {

                @Override
                public void onSuccess(ArrayList<Media> list) {
                    if(list!= null && !list.isEmpty()){
                        callback.getChannelPlaylist(list);
                        //may be this is for fetching next page.
                        //Already playlist is playing.No need to start play
                        if(!isPlaylistPlaying){
                            playMediaID(list.get(mCurrentPlayPos).mMediaID,mPlaylistContentSvc);
                            isPlaylistPlaying = true;
                        }
                        if(mPlaylistContentSvc.hasNextPage()){
                            fetchPlaylist(channelId, callback);
                        }
                    }else{
                        if (mPlayerCallback != null){
                            mPlayerCallback.playerMessage(Constants.Message.error.ordinal(), 0,"No Media For This Channel !");
                        }
                        return;
                    }
                }

                @Override
                public void onError(Throwable throwable) {
                    if (mPlayerCallback != null){
                        mPlayerCallback.playerMessage(Constants.Message.error.ordinal(), 0,throwable.getMessage());
                    }
                }
            });
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
            if (mPlayerView == null) {
                if (mLogger != null) {
                    mLogger.error(Constants.PLAYER_NOT_INIT_ERROR);
                }
            } else{
                mPlayerView.start();

                if (mLogger != null) {
                    mLogger.info("Player started");
                    mLogger.error("Player started");
                    mLogger.fatal("Player started");
                    mLogger.debug("Player started");
                    mLogger.warn("Player started");
                }
            }
            if (mLogger != null) {
                mLogger.debug(TAG+" PlayerState: play :"+mPlayerView.mPlayerState.name());
            }
        }

        @Override
        public void pause() {
            if (mUri == null) {
                if (mLogger != null) {
                    mLogger.warn("Please Set The Uri");
                }
                return;
            }
            if (mPlayerView == null) {
                if (mLogger != null) {
                    mLogger.error(Constants.PLAYER_NOT_INIT_ERROR);
                }
            } else{
                mPlayerView.pause();
                mPlayerView.mPlayerState = PlayerState.paused;
            }
            if (mLogger != null) {
                mLogger.debug(TAG+" PlayerState: pause: "+mPlayerView.mPlayerState.name());
            }
        }

        @Override
        public void stop() {
            if (mPlayerView == null){
                if (mLogger != null) {
                    mLogger.error(Constants.PLAYER_NOT_INIT_ERROR);
                }
            } else{
                reset();
            }
        }

        private void setVideoUri(Uri uri) {
            mPosition = 0;
            if (uri == null) {
                if (mLogger != null) {
                    mLogger.info("Please Check The URI");
                }
            }else {
                mUri = uri;
                if (mPlayerView == null) {
                    if (mLogger != null) {
                        mLogger.error(Constants.PLAYER_NOT_INIT_ERROR);
                    }
                }else {
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
                }
                if (mLogger != null) {
                    mLogger.debug(TAG+" Video path: "+mUri.toString());
                }
            }
        }


        private void setVideoPath(String path) {
            mPosition = 0;
            if (path == null) {
                if (mLogger != null) {
                    mLogger.info("Please Check The URI");
                }
            }else {
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
            }
        }
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        stopTimerForPrepare();
        if(mPlayerView.mPlayerState == PlayerState.paused){
            mPlayerView.pause();
        }else if(mPlayerView.mPlayerState == PlayerState.completed){
            if(mPlayerView.getDuration() == -1)
                mPlayerView.stopPlayback();
        }
        else{
            final String str3gp = "3gp";
            //Special case for 3GP url as it takes long time to play.
            if(!(URLUtil.isContentUrl(mUri.toString())) && str3gp.equalsIgnoreCase(MimeTypeMap.getFileExtensionFromUrl(mUri.toString()))){
                if (mLogger != null) {
                    mLogger.debug(TAG+" 3GP case Uri:" + mUri.toString());
                }
                mediaPlayer.setOnInfoListener(new OnInfoListener() {
                    @Override
                    public boolean onInfo(final MediaPlayer mediaPlayer, final int what, final int extra) {
                        if(what == MediaPlayer.MEDIA_INFO_BUFFERING_START||
                                what == MediaPlayer.MEDIA_INFO_BUFFERING_END||
                                what == MediaPlayer.MEDIA_INFO_METADATA_UPDATE){
                            if (mLogger != null) {
                                mLogger.debug(TAG+" Info now"+": "+what +":"+extra);
                            }
                            if(mPlayerCallback!= null){
                                mPlayerCallback.playerPrepared(mPlayerControl);
                            }
                        }
                        return true;
                    }
                });
                mediaPlayer.setOnBufferingUpdateListener(new OnBufferingUpdateListener() {

                    @Override
                    public void onBufferingUpdate(final MediaPlayer mediaPlayer, final int percent) {
                        if(mPlayerCallback!= null){
                        mPlayerCallback.playerPrepared(mPlayerControl);
                        }
                        stopTimerForBuffer();
                    }
                });
                startTimerForBuffer();
                mPlayerControl.play();
            }else{
                if(mPlayerCallback!= null){
                    mPlayerCallback.playerPrepared(mPlayerControl);
                }
                mPlayerControl.play();
            }
        }
        if (mLogger != null) {
            mLogger.debug(TAG+Constants.PLAYER_STATE_STRING+mPlayerView.mPlayerState.name());
        }
        hideMediaController();
    }

    @Override
    public void onCompletion(MediaPlayer player) {
        if(mPlayerView.mPlayerState != PlayerState.completed){
            if (mLogger != null) {
                mLogger.debug(TAG+" PlayerState:"+mPlayerView.mPlayerState.name());
            }
            if(mIsAutoPlay){
                if(mPlaylistContentSvc!= null && !mPlaylistContentSvc.getMediaList().isEmpty()){
                    if(mPlaylistContentSvc.getMediaList().size() > mCurrentPlayPos+1){
                        mCurrentPlayPos++;
                        if(mPlayerView != null && mPlayerView.mPlayerState!= PlayerState.stopped){
                            mPlayerView.stopPlayback();
                            mPlayerView.mPlayerState = PlayerState.stopped;
                        }
                        mPlayerControl.playMediaID(mPlaylistContentSvc.getMediaList().get(mCurrentPlayPos).mMediaID, mPlaylistContentSvc);
                    }
                }
            }
            if(mPlayerCallback!= null){
                mPlayerCallback.playerMessage(Constants.Message.status.ordinal(), Constants.PlayerState.completed.ordinal(),null);
            }
        }
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
            mLogger.debug(TAG+Constants.PLAYER_STATE_STRING+mPlayerView.mPlayerState.name());
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
            public void onTick(final long millisUntilFinished) {
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
            public void onTick(final long millisUntilFinished) {
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
    public void onMediaControllerPlay(final long position) {
        mReporter.sendPlayWithPosition(position,mMediaId,null);
    }

    @Override
    public void onMediaControllerPause(final long position) {
        mReporter.sendPauseWithPosition(position,mMediaId,null);
    }

    @Override
    public void onMediaControllerSeek(long positionBefore,long positionAfter) {
        mReporter.sendSeekWithPositionBefore(positionBefore, positionAfter,mMediaId,null);
    }

    @Override
    public void onMediaControllerComplete() {
        mReporter.sendMediaComplete(mMediaId, null);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mReporter.unregisterReceiver();
        reset();
        mPlayerView = null;
        mPlayerLayout = null;
        mLogger = null;
        mPlayerCallback = null;
    }
}