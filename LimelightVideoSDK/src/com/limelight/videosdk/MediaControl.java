package com.limelight.videosdk;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.MediaController;
/**
 * This class is custom MediaController to support Full Screen and also to add a full screen button to the default MediaController.
 * @author kanchan
 */
class MediaControl extends MediaController{

    private final Context mContext;
    private FullScreenCallback mCallback;
    private boolean mIsFullScreen;

    MediaControl(final Context context, final boolean useFastForward) {
        super(context, useFastForward);
        mContext = context;
    }

    MediaControl(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    MediaControl(final Context context) {
        super(context);
        mContext = context;
    }

    @Override 
    public void setAnchorView(final View view) {
        super.setAnchorView(view);
        final OnClickListener listener = new OnClickListener() {
            @Override
            public void onClick(final View view) {
                if(mIsFullScreen){
                    if(mCallback!=null){
                        mCallback.fullScreen();
                    }
                }
               else{
                   if(mCallback !=null){
                       mCallback.closeFullScreen();
                   }
               }
            }
        };
        final Button fullScreenButton = new Button(mContext);
        fullScreenButton.setOnClickListener(listener);
        final FrameLayout container = new FrameLayout(mContext);
        container.setOnClickListener(listener);
        final FrameLayout.LayoutParams containerParams = new FrameLayout.LayoutParams(60, 50);
        containerParams.gravity = Gravity.RIGHT;
        containerParams.rightMargin = 30;
        containerParams.topMargin = 2;
        final ShapeDrawable drawable = new ShapeDrawable(new RectShape());
        drawable.getPaint().setColor(Color.WHITE);
        fullScreenButton.setBackgroundDrawable(drawable);
        final FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(30, 20);
        params.gravity = Gravity.RIGHT;
        params.rightMargin = 30;
        params.topMargin = 20;
        container.addView(fullScreenButton, params);
        //add a vertical bar to view
        final View rightVerticalBar = new View(mContext);
        rightVerticalBar.setBackgroundColor(Color.BLACK);
        final FrameLayout.LayoutParams rightBarParams = new FrameLayout.LayoutParams(2,15);
        rightBarParams.gravity = Gravity.RIGHT;
        rightBarParams.rightMargin = 33;
        rightBarParams.topMargin = 22;
        container.addView(rightVerticalBar, rightBarParams);
        //add a vertical bar to view
        final View leftVerticalBar = new View(mContext);
        leftVerticalBar.setBackgroundColor(Color.BLACK);
        final FrameLayout.LayoutParams leftBarParams = new FrameLayout.LayoutParams(2,15);
        leftBarParams.gravity = Gravity.RIGHT;
        leftBarParams.rightMargin = 55;
        leftBarParams.topMargin = 22;
        container.addView(leftVerticalBar,leftBarParams);
        addView(container,containerParams);
    }

    /**
     * This method sets a callback to be used for sending fullscreen event to player and vice versa.
     * @param callback
     * @param fullScreen
     */
    void setFullScreenCallback(final FullScreenCallback callback,final boolean fullScreen){
        mCallback = callback;
        mIsFullScreen = fullScreen;
    }

    interface FullScreenCallback{
        void fullScreen();
        void closeFullScreen();
    }
}
