package com.limelight.videosdk;

import org.apache.log4j.Logger;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import com.limelight.videosdk.Constants.PlayerState;
import com.limelight.videosdk.MediaControl.FullScreenCallback;

/**
 * This class will play the video in full screen.
 * @author kanchan
 *
 */
public class FullScreenPlayer extends Activity implements OnErrorListener,OnPreparedListener, OnCompletionListener,IMediaControllerCallback{
    private static final String TAG = FullScreenPlayer.class.getSimpleName();
    private VideoPlayerView mPlayerView;
    private Logger mLogger;
    private int mPosition;
    private AnalyticsReporter mReporter;
    private String mMediaId;
    private ProgressBar mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        final RelativeLayout playerLayout = new RelativeLayout(this);
        playerLayout.setBackgroundColor(Color.BLACK);
        playerLayout.setGravity(Gravity.CENTER);
        mPlayerView = new VideoPlayerView(this);
        final MediaControl mediaController = new MediaControl(this, true);
        mediaController.setFullScreenCallback(new FullScreenCallback() {
            @Override
            public void fullScreen() {
                mLogger.debug(TAG+" Full Screen Should not be Called here");
            }

            @Override
            public void closeFullScreen() {
                close(mPlayerView.getCurrentPosition());
            }
        },false);
        mediaController.setAnchorView(mPlayerView);
        mPlayerView.setMediaController(mediaController);
        playerLayout.addView(mPlayerView);
        final RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        mProgress = new ProgressBar(this);
        mProgress.setIndeterminate(true);
        playerLayout.addView(mProgress,params);
        setContentView(playerLayout);
        mPlayerView.setOnErrorListener(this);
        mPlayerView.setOnCompletionListener(this);
        mPlayerView.setMediaControllerCallback(this);
        mLogger = LoggerUtil.getLogger(this);
        mReporter = new AnalyticsReporter(this);
        final Uri uri = Uri.parse(getIntent().getStringExtra("URI"));
        mPosition  = getIntent().getIntExtra("POSITION",0);
        final String state = getIntent().getStringExtra("STATE");
        mPlayerView.mPlayerState = PlayerState.valueOf(state);
        mMediaId = getIntent().getStringExtra("MEDIAID");
        mPlayerView.setVideoURI(uri);
        mPlayerView.setOnPreparedListener(this);
        mReporter = new AnalyticsReporter(this);
        mLogger.debug(TAG+" Created");
    }

    @Override
    public void onMediaControllerPlay(final long position) {
        if(mMediaId!= null){
            mReporter.sendPlayWithPosition(position,mMediaId,null);
        }
    }

    @Override
    public void onMediaControllerPause(final long position) {
        if(mMediaId!= null){
            mReporter.sendPauseWithPosition(position,mMediaId,null);
        }
    }

    @Override
    public void onMediaControllerSeek(final long beforePosition, final long afterPosition) {
        if(mMediaId!= null){
            mReporter.sendSeekWithPositionBefore(beforePosition, afterPosition,mMediaId,null);
        }
    }

    @Override
    public void onMediaControllerComplete() {
        if(mMediaId!= null){
            mReporter.sendMediaComplete(mMediaId, null);
        }
    }

    @Override
    public void onCompletion(final MediaPlayer mediaPlayer) {
        mLogger.debug(TAG+" Completed Playing");
        final int duration = mediaPlayer.getDuration();
        if(mMediaId!= null){
            mReporter.sendMediaComplete(mMediaId, null);
        }
        close(duration);
    }

    @Override
    public void onPrepared(final MediaPlayer mediaPlayer) {
        if(mPlayerView.mPlayerState==PlayerState.stopped || mPlayerView.mPlayerState== PlayerState.completed){
            mPlayerView.mPlayerState = PlayerState.stopped;
            close(0);
        }
        else{
            mPlayerView.seekTo(mPosition);
            if(mPlayerView.mPlayerState == PlayerState.playing){
                mPlayerView.start();
            }
            mProgress.setVisibility(View.GONE);
            mPlayerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onError(final MediaPlayer mediaPlayer, final int what, final int extra) {
        mPlayerView.mPlayerState = PlayerState.stopped;
        close(0);
        return false;
    }

    /**
     * This is the method to close the FullScreenPlayer and send the current state to normal player.
     * @param position
     */
    private void close(final int position){
        final Intent intent = new Intent();
        intent.setAction("limelight.intent.action.PLAY_FULLSCREEN");
        intent.putExtra("POSITION",position);
        intent.putExtra("STATE",mPlayerView.mPlayerState.name());
        LocalBroadcastManager.getInstance(FullScreenPlayer.this).sendBroadcast(intent);
        mPlayerView.stopPlayback();
        this.finish();
    }
}
