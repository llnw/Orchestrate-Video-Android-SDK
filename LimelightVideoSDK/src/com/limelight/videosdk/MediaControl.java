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
    private FullScreenCallback mFullScreenCallback;
    private boolean mIsFullScreen;

    public MediaControl(final Context context, final boolean useFastForward) {
        super(context, useFastForward);
        mContext = context;
    }

    public MediaControl(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public MediaControl(final Context context) {
        super(context);
        mContext = context;
    }

    @Override 
    public void setAnchorView(final View view) {
        super.setAnchorView(view);
        final Button searchButton = new Button(mContext);
        searchButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View view) {
                if(mIsFullScreen){
                    mFullScreenCallback.fullScreen();
                }
               else{
                   mFullScreenCallback.closeFullScreen();
               }
            }
        });
        final ShapeDrawable drawable = new ShapeDrawable(new RectShape());
        drawable.getPaint().setColor(Color.WHITE);
        searchButton.setBackgroundDrawable(drawable);
        final FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(30, 20);
        params.gravity = Gravity.RIGHT;
        params.rightMargin = 30;
        params.topMargin = 20;
        addView(searchButton, params);
        //add a vertical bar to view
        final View rightVerticalBar = new View(mContext);
        rightVerticalBar.setBackgroundColor(Color.BLACK);
        final FrameLayout.LayoutParams rightVerticalBarParams = new FrameLayout.LayoutParams(2,15);
        rightVerticalBarParams.gravity = Gravity.RIGHT;
        rightVerticalBarParams.rightMargin = 33;
        rightVerticalBarParams.topMargin = 22;
        addView(rightVerticalBar, rightVerticalBarParams);
        //add a vertical bar to view
        final View leftVerticalBar = new View(mContext);
        leftVerticalBar.setBackgroundColor(Color.BLACK);
        final FrameLayout.LayoutParams leftVerticalBarParams = new FrameLayout.LayoutParams(2,15);
        leftVerticalBarParams.gravity = Gravity.RIGHT;
        leftVerticalBarParams.rightMargin = 55;
        leftVerticalBarParams.topMargin = 22;
        addView(leftVerticalBar,leftVerticalBarParams);
    }

    /**
     * This method sets a callback to be used for sending fullscreen event to player and vice versa.
     * @param callback
     * @param fullScreen
     */
    void setFullScreenCallback(final FullScreenCallback callback,final boolean fullScreen){
        mFullScreenCallback = callback;
        mIsFullScreen = fullScreen;
    }

    interface FullScreenCallback{
        void fullScreen();
        void closeFullScreen();
    }
}
