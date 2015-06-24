package com.limelight.videosdk.utility;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * This class helps in finding the current network status of device.<br>
 * If the device is connected, then only ContentService, WidevineManager 
 * and Downloader will send requests to Limelight server.
 *
 */
final public class Connection {

    private Connection(){}
    /**
     * This method checks if the device is connected to Internet.
     * @param ctx
     * @return true if connected else false
     */
    public static boolean isConnected(final Context ctx){
        final ConnectivityManager connMgr = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }
}
