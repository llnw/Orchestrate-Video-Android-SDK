package com.limelight.videosdk;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

/**
 * This class is a wrapper around the android player default player view.
 * It has the overridden methods for pause, start and seeks,
 * so that these events information can be sent to Player via IMediaControllerCallback object.
 * This information is used for Analytical Reporting.
 * @author kanchan
 */
class VideoPlayerView extends VideoView{

    private IMediaControllerCallback mListener;
    
    public VideoPlayerView(Context context) {
        super(context);
        
    }

    public VideoPlayerView(Context context, AttributeSet attrs){
        super(context, attrs);
    }

    public VideoPlayerView(Context context, AttributeSet attrs, int defStyle){
        super(context, attrs, defStyle);
    }

    void setMediaControllerCallback(IMediaControllerCallback listener) {
        mListener = listener;
    }

    @Override
    public void seekTo(int msec) {
        long before = this.getCurrentPosition();
        super.seekTo(msec);
        long after = msec;
        if (mListener != null) {
            mListener.onMediaControllerSeek(before, after);
        }
    }

    @Override
    public void pause() {
        if(this.isPlaying()){
            super.pause();
            if (mListener != null) {
                mListener.onMediaControllerPause(this.getCurrentPosition());
            }
        }
    }

    @Override
    public void start() {
        super.start();
        if (mListener != null) {
            if(this.getCurrentPosition() != 0)
                mListener.onMediaControllerPlay(this.getCurrentPosition());
        }
    }
}
