package com.limelight.videosdk.utility;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * This class helps in finding the current network status of device.<br>
 * If the device is connected, then only ContentService, WidevineManager 
 * and Downloader will send requests to Limelight server.
 * @author kanchan
 *
 */
public class Connection {

    /**
     * This method checks if the device is connected to Internet.
     * @param ctx
     * @return true if connected else false
     */
    public static boolean isConnected(Context ctx){
        ConnectivityManager connMgr = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }
}
