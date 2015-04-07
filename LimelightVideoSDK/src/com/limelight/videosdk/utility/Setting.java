package com.limelight.videosdk.utility;

import java.util.UUID;

import com.limelight.videosdk.Constants;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * This class manages the developer details like organization id, 
 * access key and secret. This class initializes and stores device id.
 * This class also manages widevine specific settings.
 * Developer can access the APIs exposed to set the values.
 * @author kanchan
 *
 */
public class Setting{
    private static final boolean mIsRelease = false;
    private static String sApiEndPoint = null;
    private static String sLicenseProxy = null;
    private static String sPortalkey = null;
    private static String sDeviceId = null;
    private static String sAnalyticsEndPoint = null;


    /**
     * To set widevine specific details like, Limelight API endpoint,
     * License proxy address and portal key
     * @param apiEndPoint ApiEndPoint
     * @param licenseProxy LicenseProxy
     * @param portalKey PortalKey
     */
    public static void configureLimelightSettings(String apiEndPoint,String licenseProxy,String portalKey){
        sApiEndPoint= apiEndPoint;
        sLicenseProxy = licenseProxy;
        sPortalkey = portalKey;
    }

    /**
     * To create a new device id if it does not exist, 
     * then store it and returns it in subsequent requests. 
     * Device id is required for making requests to DRM engine in case of Widevine content.
     * @param ctx context
     */
    public static String getDeviceID(Context ctx) {
        // Create a device ID, if none exists
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
        if (!preferences.contains(Constants.DEVICE_ID_KEY)) {
            sDeviceId = UUID.randomUUID().toString();
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(Constants.DEVICE_ID_KEY, sDeviceId);
            editor.commit();
        }else{
            sDeviceId = preferences.getString(Constants.DEVICE_ID_KEY, "");
        }
        return sDeviceId;
    }

    /**
     * To check if the LimelightVideoSDK has got developer details from developer 
     * application. If developer details are not supplied then error message will 
     * be sent to application when application requests to fetch any data from Limelight server.
     * @param values developer account details
     * @return true if account configured else false
     */
    public static boolean isAccountConfigured(String... values) {
        for (String value : values) {
            if (value == null || "".equals(value))
                return false;
        }
        return true;
    }

    /**
     * This method returns Content API end point.
     * If Application has provided Content end point, then provided end point will be used.
     * Otherwise SDK will use default Content API end point based on Release mode(staging or production) 
     * @return ContentAPIEndPoint
     */
    public static String getApiEndPoint(){
        if(sApiEndPoint== null){
            if(mIsRelease){
                  return Constants.API_ENDPOINT_PROD;
            }else{
                  return Constants.API_ENDPOINT_STAGING;
            }
        }else{
            return sApiEndPoint;
        }
    }

    /**
     * This method returns Widevine License Proxy URL.
     * If Application has provided Widevine License Proxy URL, then provided URL will be used.
     * Otherwise SDK will use default Widevine License Proxy URL based on Release mode(staging or production) 
     * @return AnalyticsEndPoint
     */
    public static String getLicenseProxyURL(){
        if(sLicenseProxy== null){
            if(mIsRelease){
                  return Constants.LICENSE_PROXY_PROD;
            }else{
                  return Constants.LICENSE_PROXY_STAGING;
            }
        }else{
            return sLicenseProxy;
        }
    }

    /**
     * This method returns Portal Key.
     * If Application has provided Portal Key, then provided key will be used.
     * Otherwise SDK will use default Portal Key 
     * @return AnalyticsEndPoint
     */
    public static String getPortalKey(){
        if(sPortalkey== null){
          return "Limelight";
        }else{
            return sPortalkey;
        }
    }

    /**
     * This method returns analytics end point.
     * If Application has provided analytics end point, then provided end point will be used.
     * Otherwise SDK will use default analytics end point based on Release mode(staging or production) 
     * @return AnalyticsEndPoint
     */
    public static String getAnalyticsEndPoint(){
        if(sAnalyticsEndPoint== null){
            if(mIsRelease){
                  return Constants.ANALYTICS_ENDPOINT_PROD;
            }else{
                  return Constants.ANALYTICS_ENDPOINT_STAGING;
            }
        }else{
            return sAnalyticsEndPoint;
        }
    }
}