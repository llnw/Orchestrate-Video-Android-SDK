package com.limelight.videosdk.utility;

import java.util.UUID;
import com.limelight.videosdk.Constants;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * This class initializes and stores device id.
 * This class also manages widevine specific settings and Limelight API end points.
 * Developer can access the APIs exposed to set the values.
 *
 */
public final class Setting{
    private static final boolean IS_RELEASE = true;
    private static String sContentApi;
    private static String sLicenseProxy;
    private static String sPortalkey;
    private static String sDeviceId;
    private static String sAnalyticsAPI;

    private Setting(){}
    /**
     * To set widevine specific details like, Limelight API endpoint,
     * License proxy address and portal key
     * @param apiEndPoint ApiEndPoint
     * @param licenseProxy LicenseProxy
     * @param portalKey PortalKey
     */
    public static void configureLimelightSettings(final String apiEndPoint,final String licenseProxy,final String portalKey){
        sContentApi= apiEndPoint;
        sLicenseProxy = licenseProxy;
        sPortalkey = portalKey;
    }

    /**
     * To create a new device id if it does not exist, 
     * then store it and returns it in subsequent requests. 
     * Device id is required for making requests to DRM engine in case of Widevine content.
     * @param ctx context
     */
    public static String getDeviceID(final Context ctx) {
        // Create a device ID, if none exists
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
        if (preferences.contains(Constants.DEVICE_ID_KEY)) {
            sDeviceId = preferences.getString(Constants.DEVICE_ID_KEY, "");
        }else{
            sDeviceId = UUID.randomUUID().toString();
            final SharedPreferences.Editor editor = preferences.edit();
            editor.putString(Constants.DEVICE_ID_KEY, sDeviceId);
            editor.commit();
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
    public static boolean isAccountConfigured(final String... values) {
        for (final String value : values) {
            if (value == null || value.trim().isEmpty()){
                return false;
            }
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
        if(sContentApi== null|| sContentApi.trim().isEmpty()){
            if(IS_RELEASE){
                return Constants.API_PROD;
            }else{
                return Constants.API_STAGING;
            }
        }else{
            return sContentApi;
        }
    }

    /**
     * This method returns Widevine License Proxy URL.
     * If Application has provided Widevine License Proxy URL, then provided URL will be used.
     * Otherwise SDK will use default Widevine License Proxy URL based on Release mode(staging or production) 
     * @return AnalyticsEndPoint
     */
    public static String getLicenseProxyURL(){
        if(sLicenseProxy== null|| sLicenseProxy.trim().isEmpty()){
            if(IS_RELEASE){
                return Constants.LICENSE_PROD;
            }else{
                return Constants.LICENSE_STAGING;
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
        if(sPortalkey== null || sPortalkey.trim().isEmpty()){
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
        if(sAnalyticsAPI== null || sAnalyticsAPI.trim().isEmpty()){
            if(IS_RELEASE){
                return Constants.ANALYTICS_PROD;
            }else{
                return Constants.ANALYTICS_STAGING;
            }
        }else{
            return sAnalyticsAPI;
        }
    }
    
    //when not required to expose this method from SDK. Simple change the scope of this method to private.
    /**
     * This method is to set the analytics end point.
     * If Application has provided analytics end point, then provided end point will be used.
     * Otherwise SDK will use default analytics end point based on Release mode(staging or production) 
     */
    public static void SetAnalyticsEndPoint(String analyticsEndPoint) {
        sAnalyticsAPI = analyticsEndPoint;
    }
}