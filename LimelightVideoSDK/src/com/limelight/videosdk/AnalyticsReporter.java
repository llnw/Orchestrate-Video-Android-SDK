package com.limelight.videosdk;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.preference.PreferenceManager;
import com.google.gson.JsonObject;
import com.limelight.videosdk.utility.Connection;
import com.limelight.videosdk.utility.Setting;

/**This class is used to report analytics data to Limelight analytics server.
 * AnalyticsReporter is used to send the captured analytics data to Limelight analytics server.
 * When video is played, paused or seek is triggered, 
 * the event is captured and AnalyticsReporter will be invoked to send this information to Limelight analytics server.
 * Reports contain video play, pause, seek  or play completed events. Reports include information about application name, version, device name etc.
 * AnalyticsReporter contains attributes like app name which holds the application name, 
 * app version which holds the application version, device make which holds the device manufacturer,
 * device model which holds the device model, OS version which holds the operating system version,
 * platform which holds the platform name like iOS or Android,
 * player provider ID which holds the player provider identifier like Limelight Networks,
 * version which holds the version information and user ID which holds the user identifier.
*/
class AnalyticsReporter {

    private static final String TAG = AnalyticsReporter.class.getSimpleName();

    private String mAppName;
    private long mStartTime;
    private String mDeviceModel;
    private String mDeviceMake;
    private String mAppVersion;
    private String mOsVersion;
    private String mPlatform;
    private String mPlayerProviderId;
    private String mUserId;
    private String mVersion;
    private final Context mContext;;
    private JsonObject mAnalyticsData;
    private String mSourceInstanceId;
    private RequestExecutor mRequestExecutor;
    private BroadcastReceiver mConnectionReceiver;
    private Logger mLogger;
    private final int MAX_THREAD_COUNT = 1;
    private final String mMilliSecondElapsed = "millisecondsElapsed";
    private final String mMediaId = "mediaId";
    private final String mChannelId = "channelId";
    private final String mChannelListId = "channelListId";

    AnalyticsReporter(Context ctx) {
        mContext = ctx;
        init();
        mRequestExecutor= new RequestExecutor(MAX_THREAD_COUNT);
        mLogger = LoggerUtil.getLogger(ctx);
    }

    /**
     * This method starts the Analytics session
     */
    void sendStartSession(){
        JsonObject obj = new JsonObject();
        obj.addProperty("appName", mAppName);
        obj.addProperty("userId", mUserId);
        obj.addProperty("htmlCapabilities", "Android");
        String userAgent = System.getProperty("http.agent");
        obj.addProperty("userAgent", userAgent);
        obj.addProperty(mMilliSecondElapsed, 0);
        obj.addProperty("platform", mPlatform);
        obj.addProperty(mMediaId, "");
        obj.addProperty("osVersion", mOsVersion);
        obj.addProperty("appVersion",mAppVersion);
        obj.addProperty("playerProviderId", mPlayerProviderId);
        obj.addProperty("version", mVersion);
        obj.addProperty(mChannelId, "");
        obj.addProperty("deviceMake", mDeviceMake);
        obj.addProperty("deviceModel", mDeviceModel);
        obj.addProperty(mChannelListId, "");
        addAnalyticsData("StartSession",obj);
        post(obj);
    }

    /**
     * This method sends the playing position to server.
     * @param position position in playback.
     * @param mediaId Media ID for media which is being played
     * @param channelId Channel ID for the media being played.
     */
    void sendPlayWithPosition(long position,String mediaId,String channelId){
        JsonObject obj = new JsonObject();
        obj.addProperty(mChannelListId, "");
        if(channelId==null){
            obj.addProperty(mChannelId, "");
        }
        else{
            obj.addProperty(mChannelId, channelId);
        }
        obj.addProperty(mMilliSecondElapsed, (System.currentTimeMillis()-mStartTime));
        if(mediaId == null){
            obj.addProperty(mMediaId, "");
        }
        else{
            obj.addProperty(mMediaId, mediaId);
        }
        obj.addProperty("positionInMilliseconds", position);
        addAnalyticsData("Play",obj);
        post(obj);
    }

    /**
     * This method sends the paused position of player to server.
     * @param position position in playback.
     * @param mediaId Media ID for media which is being played
     * @param channelId Channel ID for the media being played.
     */
    void sendPauseWithPosition (long position,String mediaId,String channelId){
        JsonObject obj = new JsonObject();
        obj.addProperty(mChannelListId, "");
        if(channelId==null){
            obj.addProperty(mChannelId, "");
        }
        else{
            obj.addProperty(mChannelId, channelId);
        }
        obj.addProperty(mMilliSecondElapsed, (System.currentTimeMillis()-mStartTime));
        if(mediaId == null){
            obj.addProperty(mMediaId, "");
        }
        else{
            obj.addProperty(mMediaId, mediaId);
        }
        obj.addProperty("positionInMilliseconds", position);
        addAnalyticsData("Pause", obj);
        post(obj);
    }

    /**
     * This method sends the seeked position to server.
     * @param positionBefore position before seeking in playback.
     * @param positionAfter position after seeking in playback.
     * @param mediaId Media ID for media which is being played
     * @param channelId Channel ID for the media being played.
     */
    void sendSeekWithPositionBefore(long positionBefore,long positionAfter,String mediaId,String channelId){
        JsonObject obj = new JsonObject();
        if(mediaId == null){
            obj.addProperty(mMediaId, "");
        }
        else{
            obj.addProperty(mMediaId, mediaId);
        }
        obj.addProperty("offsetBefore", positionBefore);
        obj.addProperty(mChannelListId, "");
        obj.addProperty(mMilliSecondElapsed, (System.currentTimeMillis()-mStartTime));
        obj.addProperty("spectrumColor", "0");
        obj.addProperty("relatedConcepts", "");
        obj.addProperty("spectrumType", "");
        if(channelId==null){
            obj.addProperty(mChannelId, "");
        }
        else{
            obj.addProperty(mChannelId, channelId);
        }
        obj.addProperty("offsetAfter",positionAfter);
        obj.addProperty("heatmapDisplayed", false);
        addAnalyticsData("Seek",obj);
        post(obj);
    }

    /**
     * This method sends the playing completed event to server.
     * @param mediaId Media ID for media which is being played
     * @param channelId Channel ID for the media being played.
     */
    void sendMediaComplete(String mediaId,String channelId){
        JsonObject obj = new JsonObject();
        if(channelId==null){
            obj.addProperty(mChannelId, "");
        }
        else{
            obj.addProperty(mChannelId, channelId);
        }
        obj.addProperty(mMilliSecondElapsed, (System.currentTimeMillis()-mStartTime));
        if(mediaId == null){
            obj.addProperty(mMediaId, "");
        }
        else{
            obj.addProperty(mMediaId, mediaId);
        }
        obj.addProperty(mChannelListId, "");
        addAnalyticsData("MediaComplete",obj);
        post(obj);
    }

    /**
     * This method adds the specific events data to final data being sent to server.
     * @param eventType Event type like play, pause, seek.
     * @param data  Event specific data
     * @param mediaId Media ID for media which is being played
     * @param channelId Channel ID for the media being played.
     */
    private void addAnalyticsData(String eventType,JsonObject data){
        mAnalyticsData = new JsonObject();
        mAnalyticsData.addProperty("eventType", eventType);
        mAnalyticsData.addProperty("source", "Limelight Android Player");
        mAnalyticsData.add("data", data);
        mAnalyticsData.addProperty("sourceInstanceId", mSourceInstanceId);
        mAnalyticsData.addProperty("sourceVersion", 1);
    }

    /**
     * This method pauses the execution of sending analytics reports to server.
     * It is required in case when device loses connection to Internet.
     */
    private void pause(){
        mRequestExecutor.pause();
    }

    /**
     * This method resumes the execution of sending analytics reports to server.
     * It is required in case when device gets back the connection to Internet.
     */
    private void resume(){
        mRequestExecutor.resume();
    }

    /**
     * This method posts the analytics data to server after checking the Internet availability.
     * It queues the request to be sent in request executor.
     * If Internet is not there, it pauses sending of data.
     * @param obj
     */
    private void post(final JsonObject obj){
        if(!Connection.isConnected(mContext)){
            pause();
        }
        Runnable reqRunnable =  new Runnable() {

            @Override
            public void run() {
                HttpURLConnection urlConnection;
                try {
                    byte[] data = mAnalyticsData.toString().getBytes(Constants.URL_CHARACTER_ENCODING_TYPE);
                    urlConnection = (HttpURLConnection) new URL(Setting.getAnalyticsEndPoint()).openConnection();
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setRequestProperty("Content-Type", "application/json");
                    urlConnection.setRequestProperty("Content-Length", "" + data.length);
                    urlConnection.setRequestProperty("Content-Language", "en-US");  
                    urlConnection.setUseCaches (false);
                    urlConnection.setDoInput(true);
                    urlConnection.setDoOutput(true);
                    DataOutputStream str = new DataOutputStream(urlConnection.getOutputStream());
                    str.write(data);
                    str.flush();
                    str.close();
//                    System.out.println(TAG+" ResponseCode: "+urlConnection.getResponseCode());
                } catch (ProtocolException e) {
                    mLogger.error(TAG+" ProtocolException");
                }catch (IOException e1) {
                    mLogger.error(TAG+" IOException");
                }
            }
        };
        mRequestExecutor.execute(reqRunnable);
    }

    /**
     * This method initializes the various parameters to be sent to server as part of analytics.
     * It includes appname, app version, device model, device make, os, platform,user id etc.
     */
    private void init(){
        mAppName  = mContext.getApplicationContext().getPackageName();
        PackageInfo packageInfo;
        try {
            packageInfo = mContext.getApplicationContext().getPackageManager().getPackageInfo(mAppName, 0);
            mAppVersion = packageInfo.versionName;
        } catch (NameNotFoundException e) {
            mAppVersion = "1.0";
        }
        mDeviceMake = Build.MANUFACTURER;
        mDeviceModel = Build.MODEL;
        mOsVersion = Integer.toString(Build.VERSION.SDK_INT);
        mPlatform = "Android";
        mPlayerProviderId = "Limelight Networks";
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        if (!preferences.contains("USER_ID_KEY")) {
            mUserId = UUID.randomUUID().toString();
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("USER_ID_KEY", mUserId);
            editor.commit();
        }else{
            mUserId = preferences.getString("USER_ID_KEY", "");
        }
        mVersion = "0.4";
        mStartTime = System.currentTimeMillis();
        mSourceInstanceId = UUID.randomUUID().toString();
        mConnectionReceiver = new BroadcastReceiver(){
            @Override
            public void onReceive(Context ctx, Intent intent) {
                mLogger.debug(TAG + "Reached onReceive, there is a change in internet connection");
                if(intent.getExtras() != null){
                    ConnectivityManager connMgr = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                    if(networkInfo != null && networkInfo.getState() == NetworkInfo.State.CONNECTED){
                        //Network is connected
                        mLogger.debug(TAG + "Connected to network");
                        resume();
                    }
                    else{
                        //No network connectivity
                        mLogger.debug(TAG + "No network connectivity");
                        pause();
                    }
                }
            }
        };
        IntentFilter filter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        mContext.registerReceiver(mConnectionReceiver, filter);
    }

    /**
     * This method unregisters the receiver for Internet connectivity change.
     * It also shuts down the request executor when receiver is unregistered.
     */
    void unregisterReceiver(){
        mContext.unregisterReceiver(mConnectionReceiver);

        //mRequestExecutor.resume();
        mRequestExecutor.shutdown();
        //Wait for the current running tasks 
        try {
            mRequestExecutor.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            mLogger.debug(TAG + " InterruptedException");
        }
        //Interrupt the threads and shutdown the reqExecutor
        mRequestExecutor.shutdownNow();
    }
}
