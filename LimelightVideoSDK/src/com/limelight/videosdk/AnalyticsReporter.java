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
 * Reports contain video play, pause or seek events. Reports include information about application name, version, device name etc.
 * AnalyticsReporter contains attributes like app name which holds the application name, 
 * app version which holds the application version, device make which holds the device manufacturer,
 * device model which holds the device model, OS version which holds the operating system version,
 * platform which holds the platform name like iOS or Android,
 * player provider ID which holds the player provider identifier like Limelight Networks,
 * version which holds the version information and user ID which holds the user identifier.
*/
public class AnalyticsReporter {

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
    private Context mContext;;
    private JsonObject mAnalyticsData;
    private String mSourceInstanceId;
    private RequestExecutor mRequestExecutor;
    private BroadcastReceiver mConnectionReceiver = null;
    private IntentFilter mConnectionChangeFilter = null;
    private Logger mLogger=  null;
    private final int MAX_THREAD_COUNT = 1;

    public AnalyticsReporter(Context ctx) {
        mContext = ctx;
        init();
        mRequestExecutor= new RequestExecutor(MAX_THREAD_COUNT);
        mLogger = LoggerUtil.getLogger(ctx,LoggerUtil.sLoggerName);
    }

    /**
     * This method starts the Analytics session
     */
    void sendStartSession(){
        JsonObject obj = new JsonObject();
        obj.addProperty("appName", mAppName);
        obj.addProperty("appVersion",mAppVersion);
        obj.addProperty("deviceMake", mDeviceMake);
        obj.addProperty("deviceModel", mDeviceModel);
        obj.addProperty("osVersion", mOsVersion);
        obj.addProperty("platform", mPlatform);
        obj.addProperty("playerProviderId", mPlayerProviderId);
        obj.addProperty("userId", mUserId);
        obj.addProperty("version", mVersion);
        addAnalyticsData("StartSession",obj,null,null);
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
        obj.addProperty("positionInMilliseconds", ""+position);
        addAnalyticsData("Play",obj,mediaId,channelId);
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
        obj.addProperty("positionInMilliseconds", ""+position);
        addAnalyticsData("Pause", obj,mediaId,channelId);
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
        obj.addProperty("offsetBefore", positionBefore);
        obj.addProperty("offsetAfter",positionAfter);
        obj.addProperty("heatmapDisplayed", "NO");
        obj.addProperty("spectrumType", "");
        obj.addProperty("spectrumColor", "0");
        obj.addProperty("relatedConcepts", "");
        addAnalyticsData("Seek",obj,mediaId,channelId);
        post(obj);
    }

    /**
     * This method sends the playing completed event to server.
     * @param mediaId Media ID for media which is being played
     * @param channelId Channel ID for the media being played.
     */
    void sendMediaComplete(String mediaId,String channelId){
        JsonObject obj = new JsonObject();
        addAnalyticsData("MediaComplete",obj,mediaId,channelId);
        post(obj);
    }

    /**
     * This method adds the specific events data to final data being sent to server.
     * @param eventType Event type like play, pause, seek.
     * @param data  Event specific data
     * @param mediaId Media ID for media which is being played
     * @param channelId Channel ID for the media being played.
     */
    void addAnalyticsData(String eventType,JsonObject data, String mediaId, String channelId){
        System.out.println(TAG +" Event type: "+eventType);
        data.addProperty("millisecondsElapsed", ""+(System.currentTimeMillis()-mStartTime));
        if(mediaId == null)
            data.addProperty("mediaId", "");
        else
            data.addProperty("mediaId", mediaId);
        if(channelId==null)
            data.addProperty("channelId", "");
        else
            data.addProperty("channelId", channelId);
        data.addProperty("channelListId", "");
        mAnalyticsData = new JsonObject();
        mAnalyticsData.addProperty("eventType", eventType);
        mAnalyticsData.addProperty("source", "Limelight Android Player");
        mAnalyticsData.addProperty("sourceInstanceId", mSourceInstanceId);
        mAnalyticsData.addProperty("sourceVersion", "1");
        mAnalyticsData.add("data", data);
    }

    public void pause(){
        mRequestExecutor.pause();
    }

    public void resume(){
        mRequestExecutor.resume();
    }

    void post(final JsonObject obj){
        if(Connection.isConnected(mContext) == false)
        {
            pause();
        }
        Runnable reqRunnable =  new Runnable() {

            @Override
            public void run() {
                HttpURLConnection urlConnection;
                try {
                    byte[] data = mAnalyticsData.toString().getBytes("UTF-8");
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
                    System.out.println(TAG+" ResponseCode: "+urlConnection.getResponseCode());
                } catch (ProtocolException e) {
                    e.printStackTrace();
                }catch (IOException e1) {
                    e1.printStackTrace();
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
        mOsVersion = ""+Build.VERSION.SDK_INT;
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
        if(mConnectionReceiver == null)
        {
            mConnectionReceiver = new BroadcastReceiver(){
                @Override
                public void onReceive(Context ctx, Intent intent) {
                    mLogger.debug(TAG + "Reached onReceive, there is a change in internet connection");
                    if(intent.getExtras() != null)
                    {
                        ConnectivityManager connMgr = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
                        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                        if(networkInfo != null && networkInfo.getState() == NetworkInfo.State.CONNECTED)
                        {
                            //Network is connected
                            mLogger.debug(TAG + "Connected to network");
                            resume();
                        }
                        else
                        {
                            //No network connectivity
                            mLogger.debug(TAG + "No network connectivity");
                            pause();
                        }
                    }
                }
            };
        }//end of if
        mConnectionChangeFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        mContext.registerReceiver(mConnectionReceiver, mConnectionChangeFilter);
    }

    void unregisterReceiver(){
        mContext.unregisterReceiver(mConnectionReceiver);

        //mRequestExecutor.resume();
        mRequestExecutor.shutdown();
        //Wait for the current running tasks 
        try {
            mRequestExecutor.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //Interrupt the threads and shutdown the reqExecutor
        mRequestExecutor.shutdownNow();
    }
}
