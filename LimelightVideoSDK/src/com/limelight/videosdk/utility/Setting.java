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
    public static String sOrgId;
    public static String sAccessKey;
    public static String sSecret;
    public static String sApiEndPoint;
    public static String sLicenseProxy;
    public static String sPortalkey;
    public static String sDeviceId;

    /**
     * To set the developer account details like organization id, access key and secret.
     * @param id organization id
     * @param key access key
     * @param secret secret
     */
    public static void configureAccount(String id,String key,String secret){
        sOrgId = id;
        sAccessKey = key;
        sSecret = secret;
    }

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
}