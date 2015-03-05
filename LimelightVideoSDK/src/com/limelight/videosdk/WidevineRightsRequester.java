package com.limelight.videosdk;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.json.JSONException;
import org.json.JSONObject;

import com.limelight.videosdk.utility.Setting;
import android.content.Context;
import android.drm.DrmErrorEvent;
import android.drm.DrmEvent;
import android.drm.DrmInfoEvent;
import android.drm.DrmInfoRequest;
import android.drm.DrmManagerClient;
import android.drm.DrmStore;
import android.net.Uri;
import android.util.Log;

/**
 * This class checks for the widevine drm engine presence in device and 
 * requests drm engine to fetch license from Limelight server for rights 
 * associated with the media.It then requests DRM engine to validate the rights.
 * @author kanchan
 *
 */
class WidevineRightsRequester {

    private static final String TAG = WidevineRightsRequester.class.getSimpleName();

    private Callback mCallback;
    private DrmManagerClient mDrm;
    private Uri mUri;
    private String mMediaId;
    public boolean mDrmInitFailed = false;

    /**
     * A delegate for getting the response back from the call.
     * Parameters are marked final, as the callback will be invoked on another thread.
     */
    static interface Callback {
        void onSuccess();
        void onError(Throwable throwable);
    }

    WidevineRightsRequester(Context context, Uri uri, String mediaId, Callback callback) {
        this.mUri = uri;
        this.mMediaId = mediaId;
        this.mCallback = callback;
        initializeDrmManagerClient(context);
    }

    /**
     * It requests drm engine to fetch license from Limelight server for 
     * rights associated with the media.It then requests DRM engine to validate 
     * the rights.
     */
    void requestRights() {

        final long clientTime = System.currentTimeMillis() / 1000L;

        // access_key+"|"+client_time+"|"+media_id+"|get_license|"+organization_id"
        String toSign = String.format("%s|%s|%s|get_license|%s",Setting.sAccessKey, clientTime, mMediaId, Setting.sOrgId);

        // sign our string
        final String signature = URLAuthenticator.signWithKey(Setting.sSecret, toSign);

        if(signature == null){
            mCallback.onError(new Throwable("Signing Failed !"));
            return;
        }
        // create our credentials
        JSONObject credentials = null;
        try{
            credentials = new JSONObject() {
                {
                    put("access_key", Setting.sAccessKey);
                    put("client_time", clientTime);
                    put("media_id", mMediaId);
                    put("operation", "get_license");
                    put("organization_id", Setting.sOrgId);
                    put("signature", signature);
                }
            };
        }catch (JSONException e) {
            Log.w(TAG, "error serializing JSON: %s", e);
            mCallback.onError(new Throwable("Error Serializing JSON: %s"));
            return;
        }

        // serialize to JSON
        String userData = null;
        try {
            userData = URLEncoder.encode(credentials.toString(), Constants.URL_CHARACTER_ENCODING_TYPE);
        } catch (UnsupportedEncodingException e) {
            Log.w(TAG, "error encoding JSON: %s", e);
            mCallback.onError(new Throwable("Error Encoding JSON: %s"));
            return;
        }

        // create our drm request
        DrmInfoRequest drmInfoRequest = new DrmInfoRequest(DrmInfoRequest.TYPE_RIGHTS_ACQUISITION_INFO, "video/wvm");
        drmInfoRequest.put("WVCAUserDataKey", userData);
        drmInfoRequest.put("WVAssetURIKey", mUri.toString());
        drmInfoRequest.put("WVDRMServerKey", Setting.sLicenseProxy);
        drmInfoRequest.put("WVPortalKey", Setting.sPortalkey);
        drmInfoRequest.put("WVClientKey", Setting.sOrgId);
        drmInfoRequest.put("WVDeviceIDKey",Setting.sDeviceId);

        FileInputStream fis = null;
        boolean isOffline = mUri.toString().startsWith("/");
        if (isOffline) {
            try {
                fis = new FileInputStream(mUri.toString());
                FileDescriptor fd = fis.getFD();
                if (fd.valid()) {
                    drmInfoRequest.put("FileDescriptorKey", fd.toString());
                }else{
                    Log.w(TAG, "FileDescriptorKey is Invalid");
                    mCallback.onError(new Throwable("FileDescriptorKey is Invalid"));
                    if(fis != null){
                        try {
                            fis.close();
                        } catch (IOException e1) {
                            Log.w(TAG, "IOException In Closing FileInputStream", e1);
                        }
                    }
                    return;
                }
            } catch (FileNotFoundException e) {
                Log.w(TAG, "error adding file key %s", e);
                mCallback.onError(new Throwable("Error Adding File Key %s"));
                if(fis != null){
                    try {
                        fis.close();
                    } catch (IOException e1) {
                        Log.w(TAG, "IOException In Closing FileInputStream", e1);
                    }
                }
                return;
            } catch (IOException e) {
                Log.w(TAG, "error adding file key %s", e);
                mCallback.onError(new Throwable("Error Adding File Key %s"));
                if(fis != null){
                    try {
                        fis.close();
                    } catch (IOException e1) {
                        Log.w(TAG, "IOException In Closing FileInputStream", e1);
                    }
                }
                return;
            }
        }

        // get our license
        int val = mDrm.acquireRights(drmInfoRequest);
        if(val != DrmManagerClient.ERROR_NONE){
            mCallback.onError(new Throwable("AcquireRights Failed"));
        }
        if(fis != null){
            try {
                fis.close();
            } catch (IOException e) {
                Log.w(TAG, "IOException in closing FileInputStream", e);
                mCallback.onError(new Throwable("IOException In Closing FileInputStream"));
            }
        }
    }

    /**
     * To check availability of widevine DRM engine and on availability, 
     * initialize the drm engine.
     * @param context
     */
    private void initializeDrmManagerClient(Context context) {
        mDrm = new DrmManagerClient(context);
        boolean supported = false;
        String[] engines = mDrm.getAvailableDrmEngines();
        for (String engine : engines) {
            // "Widevine DRM plug-in"
            if (engine.contains("Widevine")) {
                supported = true;
                break;
            }
        }

        if (!supported) {
            Log.e(TAG, "Unsupported device!");
            mCallback.onError(new Throwable("WideVine Unsupported Device!"));
            mDrmInitFailed = true;
            return;
        }

        mDrm.setOnErrorListener(new DrmManagerClient.OnErrorListener() {
            @Override
            public void onError(DrmManagerClient client, DrmErrorEvent event) {
                switch (event.getType()) {
                case DrmErrorEvent.TYPE_PROCESS_DRM_INFO_FAILED:
                    Log.w(TAG, "Failed to process DRM info");
                    mCallback.onError(new Throwable("Failed To Process DRM Info"));
                    break;
                default:
                    Log.w(TAG, String.format("Error [%d]: %s", event.getType(), event.getMessage()));
                    mCallback.onError(new Throwable("DRM Error: "+event.getMessage()));
                    break;
                }
            }
        });
        mDrm.setOnEventListener(new DrmManagerClient.OnEventListener() {
            @Override
            public void onEvent(DrmManagerClient client, DrmEvent event) {
                switch (event.getType()) {
                case DrmEvent.TYPE_DRM_INFO_PROCESSED:
                    Log.i(TAG, "DRM info processed");
                    // check if everything went well and play if so
                    int status = mDrm.checkRightsStatus(mUri,DrmStore.Action.PLAY);
                    switch (status) {
                    case DrmStore.RightsStatus.RIGHTS_VALID:
                        Log.d(TAG, "Rights valid");
                        mCallback.onSuccess();
                        break;
                    case DrmStore.RightsStatus.RIGHTS_NOT_ACQUIRED:
                        Log.d(TAG, "Rights not acquired");
                        mCallback.onError(new Exception("Rights Not Acquired"));
                        break;
                    case DrmStore.RightsStatus.RIGHTS_EXPIRED:
                        Log.d(TAG, "Rights expired");
                        mCallback.onError(new Exception("Rights Expired"));
                        break;
                    case DrmStore.RightsStatus.RIGHTS_INVALID:
                        Log.d(TAG, "Rights invalid");
                        mCallback.onError(new Exception("Rights Invalid"));
                        break;
                    }
                    break;
                case DrmEvent.TYPE_ALL_RIGHTS_REMOVED:
                    Log.i(TAG, "All rights removed");
                    mCallback.onError(new Exception("All rights Removed"));
                    break;
                default:
                    Log.d(TAG, String.format("Event [%d]: %s", event.getType(),event.getMessage()));
                    break;
                }
            }
        });
        mDrm.setOnInfoListener(new DrmManagerClient.OnInfoListener() {
            @Override
            public void onInfo(DrmManagerClient client, DrmInfoEvent event) {
                switch (event.getType()) {
                case DrmInfoEvent.TYPE_RIGHTS_INSTALLED:
                    Log.i(TAG,"The rights have been successfully downloaded and installed");
                    break;
                case DrmInfoEvent.TYPE_ACCOUNT_ALREADY_REGISTERED:
                    Log.i(TAG,"The registration has already been done for the given account");
                    break;
                case DrmInfoEvent.TYPE_ALREADY_REGISTERED_BY_ANOTHER_ACCOUNT:
                    Log.i(TAG,"The registration has already been done by another account ID");
                    break;
                case DrmInfoEvent.TYPE_REMOVE_RIGHTS:
                    Log.i(TAG, "The rights need to be removed completely");
                    break;
                default:
                    Log.i(TAG, "Widevine Demo: INFO: " + event.getMessage());
                    break;
                }
            }
        });
    }
}
