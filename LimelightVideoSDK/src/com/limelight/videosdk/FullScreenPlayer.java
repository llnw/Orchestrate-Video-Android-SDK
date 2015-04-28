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
    private Uri mUri;
    private Logger mLogger = null;
    private int mPosition = 0;
    private RelativeLayout mPlayerLayout;
    private MediaControl mMediaController;
    private AnalyticsReporter mReporter = null;
    private String mMediaId = null;
    private ProgressBar mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mPlayerLayout = new RelativeLayout(this);
        mPlayerLayout.setBackgroundColor(Color.BLACK);
        mPlayerLayout.setGravity(Gravity.CENTER);
        mPlayerView = new VideoPlayerView(this);
        mMediaController = new MediaControl(this, true);
        mMediaController.setFullScreenCallback(new FullScreenCallback() {
            @Override
            public void fullScreen() {}

            @Override
            public void closeFullScreen() {
                close(mPlayerView.getCurrentPosition());
            }
        },false);
        mMediaController.setAnchorView(mPlayerView);
        mPlayerView.setMediaController(mMediaController);
        mPlayerLayout.addView(mPlayerView);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        mProgress = new ProgressBar(this);
        mProgress.setIndeterminate(true);
        mPlayerLayout.addView(mProgress,params);
        setContentView(mPlayerLayout);
        mPlayerView.setOnErrorListener(this);
        mPlayerView.setOnCompletionListener(this);
        mPlayerView.setMediaControllerCallback(this);
        mLogger = LoggerUtil.getLogger(this,LoggerUtil.sLoggerName);
        mReporter = new AnalyticsReporter(this);
        mUri = Uri.parse(getIntent().getStringExtra("URI"));
        mPosition  = getIntent().getIntExtra("POSITION",0);
        String state = getIntent().getStringExtra("STATE");
        mPlayerView.mPlayerState = PlayerState.valueOf(state);
        mPlayerView.setVideoURI(mUri);
        mPlayerView.setOnPreparedListener(this);
        mReporter = new AnalyticsReporter(this);
        mLogger.debug(TAG+" Created");
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
    public void onMediaControllerSeek(long beforePosition, long afterPosition) {
        mReporter.sendSeekWithPositionBefore(beforePosition, afterPosition,mMediaId,null);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mLogger.debug(TAG+" Completed Playing");
        int duration = mp.getDuration();
        mPlayerView.mPlayerState = PlayerState.completed;
        mReporter.sendMediaComplete(mMediaId,null);
        close(duration);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        if(mPlayerView.mPlayerState!=PlayerState.stopped || mPlayerView.mPlayerState!= PlayerState.completed){
            mPlayerView.seekTo(mPosition);
            if(mPlayerView.mPlayerState == PlayerState.playing)
                mPlayerView.start();
            mProgress.setVisibility(View.GONE);
            mPlayerView.setVisibility(View.VISIBLE);
        }else{
            mPlayerView.mPlayerState = PlayerState.stopped;
            close(0);
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mPlayerView.mPlayerState = PlayerState.stopped;
        close(0);
        return false;
    }

    /**
     * This is the method to close the FullScreenPlayer and send the current state to normal player.
     * @param position
     */
    void close(int position){
        Intent i = new Intent();
        i.setAction("limelight.intent.action.PLAY_FULLSCREEN");
        i.putExtra("POSITION",position);
        i.putExtra("STATE",mPlayerView.mPlayerState.name());
        LocalBroadcastManager.getInstance(FullScreenPlayer.this).sendBroadcast(i);
        mPlayerView.stopPlayback();
        this.finish();
    }
}
