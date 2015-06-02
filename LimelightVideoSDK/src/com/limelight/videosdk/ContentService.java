package com.limelight.videosdk;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import org.json.JSONException;
import android.content.Context;
import android.net.Uri;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.limelight.videosdk.model.Channel;
import com.limelight.videosdk.model.ChannelGroup;
import com.limelight.videosdk.model.Delivery;
import com.limelight.videosdk.model.DeliveryType;
import com.limelight.videosdk.model.Encoding;
import com.limelight.videosdk.model.Media;
import com.limelight.videosdk.model.Media.MediaThumbnail;
import com.limelight.videosdk.model.MediaType;
import com.limelight.videosdk.model.PrimaryUse;
import com.limelight.videosdk.utility.Connection;
import com.limelight.videosdk.utility.Setting;

/**
 * This class facilitates developer and SDK in making requests to Lime light server
 * and fetching various data like channel group, channels,media and encodings.
 * This exposes APIs for  the developers.
 * It uses connection class to check if the connection exists before making the 
 * requests.It also uses URLAutheticator to authenticate the requests by 
 * using developer details stored in setting.It stores the list of channel 
 * groups, channels, media, encodings and delivery.
 * This class stores the organization account details like organization id, access key and secret.
 * @author kanchan
 */
public class ContentService {

    private static final String TAG = ContentService.class.getSimpleName();
    private final ArrayList<Channel> mChannelList = new ArrayList<Channel>();
    private final ArrayList<ChannelGroup> mChannelGroupList = new ArrayList<ChannelGroup>();
    private final ArrayList<Media> mMediaList = new ArrayList<Media>();
    private final ArrayList<Encoding> mEncodingList = new ArrayList<Encoding>();

    private int mPageSize = 500;
    private final String[] mSortValues = {Constants.SORT_BY_UPDATE_DATE,Constants.SORT_BY_CREATE_DATE};
    private final String[] mSortOrderValues = {Constants.SORT_ORDER_ASC,Constants.SORT_ORDER_DESC};
    private String mSortBy = Constants.SORT_BY_UPDATE_DATE;
    private String mSortOrder = Constants.SORT_ORDER_ASC;
    private int mPageId;
    private boolean mHasNext;

    private final Logger mLogger;
    private final Context mContext;

    private final String mOrgId;
    private final String mAccessKey;
    private final String mSecret;

    /**
     * Getter for Organization Id.
     * @return Organization ID
     */
    public String getOrgId() {
        return mOrgId;
    }

    /**
     * Getter for access key.
     * @return Access Key
     */
    public String getAccessKey() {
        return mAccessKey;
    }

    /**
     * Getter for secret.
     * @return Secret Key
     */
    public String getSecret() {
        return mSecret;
    }

    /**
     * This method return the medialist. This is particularly used to for channel playlist.
     * @return
     */
    ArrayList<Media> getMediaList() {
        return mMediaList;
    }

    /**
     * This method return true if there is next page for the last requested content.
     * @return true if next page else false.
     */
    public boolean hasNextPage() {
        return mHasNext;
    }

    public ContentService(final Context context, final String orgId,final String access,final String secret){
        mContext = context;
        mOrgId = orgId;
        mAccessKey = access;
        mSecret = secret;
        mLogger = LoggerUtil.getLogger(mContext);
    }

    /**
     * A delegate for getting the response back from the call for fetching channels.
     * Parameters are marked final, as the callback will be invoked on another thread.
     */
    public interface ChannelCallback {
        void onSuccess(final ArrayList<Channel> list);
        void onError(final Throwable throwable);
    }

    /**
     * A delegate for getting the response back from the call for fetching Media.
     * Parameters are marked final, as the callback will be invoked on another thread.
     */
    public interface MediaCallback {
        void onSuccess(final ArrayList<Media> list);
        void onError(final Throwable throwable);
    }

    /**
     * A delegate for getting the response back from the call for fetching Encoding.
     * Parameters are marked final, as the callback will be invoked on another thread.
     */
    public interface EncodingsCallback {
        void onSuccess(final ArrayList<Encoding> encodingList);
        void onError(final Throwable throwable);
    }

    /**
     * A delegate for getting the response back from the call for fetching Channel Group.
     * Parameters are marked final, as the callback will be invoked on another thread.
     */
    public interface ChannelGroupCallback {
        void onSuccess(final ArrayList<ChannelGroup> groupList);
        void onError(final Throwable throwable);
    }

   /**
    * This method will fetch all the channel groups on server.
    * This is a blocking call.
    * @param isLoadMore True if load more called else false.
    * @return list of channel groups.
    * @throws Exception
    */
   public ArrayList<ChannelGroup> getAllChannelGroup(final boolean isLoadMore) throws Exception{

       if (!Setting.isAccountConfigured(mOrgId, mAccessKey, mSecret)) {
           throw new Exception(Constants.ACCOUNT_ERROR);
       } else {
           if(!Connection.isConnected(mContext)){
               throw new Exception(Constants.CONNECTION_ERROR);
           }else{
               Exception exception = null;
               HttpURLConnection urlConnection = null;
               try{
                   final String resourceUrl = Setting.getApiEndPoint() + String.format(Constants.FETCH_ALL_CHANNEL_GROUP_PATH, mOrgId);
                   mLogger.debug(TAG + " getAllChannelGroup "+Constants.RES_URL+resourceUrl + Constants.IS_LOAD_MORE+ isLoadMore);
                   mLogger.debug(TAG + " getAllChannelGroup "+ " mHasNextGroup: " + mHasNext +" mPageIdGroup: " + mPageId);
                   if(isLoadMore){
                       if(mHasNext){
                          mPageId++;
                       }else {
                           return mChannelGroupList;
                       }
                   }else{
                       mChannelGroupList.clear();
                       mHasNext = false;
                       mPageId = 0;
                   }
                   final HashMap<String, String> params = new HashMap<String, String>();
                   params.put(Constants.PAGE_ID, Integer.toString(mPageId));
                   params.put(Constants.PAGE_SIZE, Integer.toString(mPageSize));
                   params.put(Constants.SORT_BY, mSortBy);
                   params.put(Constants.SORT_ORDER, mSortOrder);
                   mLogger.debug(TAG+ " getAllChannelGroup "+Constants.SORT_BY+Constants.SPACE+ mSortBy+Constants.SPACE+Constants.SORT_ORDER+Constants.SPACE+mSortOrder+Constants.SPACE+Constants.PAGE_SIZE+Constants.SPACE+mPageSize);
                   final String url = URLAuthenticator.authenticateRequest(Constants.GET,resourceUrl, mAccessKey, mSecret, params);
                   urlConnection = (HttpURLConnection) new URL(url).openConnection();
                   urlConnection.setConnectTimeout(Constants.PREPARING_TIMEOUT);
                   final JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader(urlConnection.getInputStream())));
                   parseChannelGroups(reader);
               }
               catch (NoSuchAlgorithmException e) {
                   exception = new Exception("Signing Failed !");
               }
               catch (MalformedURLException e) {
                   exception =  new Exception(Constants.URL_ERROR);
               }
               catch(InvalidKeyException e){
                   exception = new Exception(Constants.AUTH_ERROR);
               }
               catch(UnsupportedEncodingException e){
                   exception = new Exception(Constants.AUTH_ERROR);
               }
               catch(URISyntaxException e){
                   exception = new Exception(Constants.AUTH_ERROR);
               }
               catch(IOException e){
                   String error = null;
                   if(urlConnection != null){
                       final JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader(urlConnection.getErrorStream())));
                       error = parseError(reader);
                   }
                   exception = new Exception(error == null?Constants.CONNECTION_ERROR:error);
               }
               finally {
                   if (urlConnection != null) {
                       urlConnection.disconnect();
                   }
                   if(exception != null){
                       if(mPageId != 0){
                           mPageId--;
                       }
                       throw exception;
                   }
               }
               return mChannelGroupList;
           }
       }
   }

    /**
     * This method will fetch all the channel groups on server.
     * This is as asynchronous call and list of channel groups is returned in callback.
     * @param isLoadMore True if load more called else false.
     * @param callback ChannelGroupCallback
     */
    public void getAllChannelGroupAsync(final boolean isLoadMore,final ChannelGroupCallback callback) {

        if (!Setting.isAccountConfigured(mOrgId, mAccessKey, mSecret)) {
            if(callback != null){
                callback.onError(new Throwable(Constants.ACCOUNT_ERROR));
            }
        } else {
            if(!Connection.isConnected(mContext)){
                if(callback != null){
                    callback.onError(new Throwable(Constants.CONNECTION_ERROR));
                }
            }else{
                try {
                    final String resourceUrl = Setting.getApiEndPoint() + String.format(Constants.FETCH_ALL_CHANNEL_GROUP_PATH, mOrgId);
                    mLogger.debug(TAG + " getAllChannelGroupAsync "+Constants.RES_URL+resourceUrl + Constants.IS_LOAD_MORE+ isLoadMore);
                    mLogger.debug(TAG + " getAllChannelGroupAsync "+ " mHasNextGroup: " + mHasNext +" mPageIdGroup: " + mPageId);
                    if(isLoadMore){
                        if(mHasNext){
                            mPageId++;
                        }else {
                            if(callback != null){
                                callback.onSuccess(mChannelGroupList);
                            }
                            return;
                        }
                    }else{
                        mChannelGroupList.clear();
                        mHasNext = false;
                        mPageId = 0;
                    }
                    final HashMap<String, String> params = new HashMap<String, String>();
                    params.put(Constants.PAGE_ID, Integer.toString(mPageId));
                    params.put(Constants.PAGE_SIZE, Integer.toString(mPageSize));
                    params.put(Constants.SORT_BY, mSortBy);
                    params.put(Constants.SORT_ORDER, mSortOrder);
                    mLogger.debug(TAG+" getAllChannelGroupAsync "+Constants.SORT_BY+Constants.SPACE+mSortBy+Constants.SPACE+Constants.SORT_ORDER+Constants.SPACE+mSortOrder+Constants.SPACE+Constants.PAGE_SIZE+Constants.SPACE+mPageSize);
                    final String url = URLAuthenticator.authenticateRequest(Constants.GET,resourceUrl, mAccessKey, mSecret, params);
                    final Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            Throwable throwable = null;
                            HttpURLConnection urlConnection = null;
                            try {
                                urlConnection = (HttpURLConnection) new URL(url).openConnection();
                                urlConnection.setConnectTimeout(Constants.PREPARING_TIMEOUT);
                                JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader(urlConnection.getInputStream())));
                                parseChannelGroups(reader);
                                if(callback != null){
                                    callback.onSuccess(mChannelGroupList);
                                }
                            }
                            catch (MalformedURLException e) {
                                throwable = new Throwable(Constants.THUMB_URL_ERROR);
                            }
                            catch (IOException e) {
                                String error = null;
                                if(urlConnection != null){
                                    JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader(urlConnection.getErrorStream())));
                                    error = parseError(reader);
                                }
                                throwable = new Throwable(error == null?Constants.CONNECTION_ERROR:error);
                            }
                            finally {
                                if (urlConnection != null) {
                                    urlConnection.disconnect();
                                }
                                if(throwable != null){
                                    if(mPageId != 0){
                                        mPageId--;
                                    }
                                    if(callback != null){
                                        callback.onError(throwable);
                                    }
                                }
                            }
                        }
                    };
                    final Thread thread = new Thread(runnable);
                    thread.start();
                }
                catch (Exception e) {
                    if(callback != null){
                        callback.onError(new Throwable(Constants.AUTH_ERROR));
                    }
                }
            }
        }
    }

    /**
     * This method will fetch all the channels of a particular channel group.
     * This method does not requires authentication.
     * This is a blocking call and list of channels is returned.
     * @param channelGroupId ChannelGroupId
     * @param isLoadMore True if load more called else false.
     * @return list of channels
     * @throws Exception
     */
    public ArrayList<Channel> getAllChannelOfGroup(final String channelGroupId,final boolean isLoadMore) throws Exception{

        if (mOrgId == null || mOrgId.trim().isEmpty()) {
            throw new Exception(Constants.ORG_ERROR);
        } else {
            if(!Connection.isConnected(mContext)){
                throw new Exception(Constants.CONNECTION_ERROR);
            }else{
                Exception exception = null;
                HttpURLConnection urlConnection = null;
                try {
                    String resourceUrl = Setting.getApiEndPoint() + String.format(Constants.FETCH_ALL_CHANNEL_OF_GROUP_PATH, mOrgId,channelGroupId);
                    mLogger.debug(TAG + " getAllChannelOfGroup "+Constants.RES_URL+resourceUrl + Constants.IS_LOAD_MORE+ isLoadMore);
                    mLogger.debug(TAG + " getAllChannelOfGroup "+ " mHasNextChannelOfGroup: " + mHasNext +" mPageIdChannelOfGroup: " + mPageId);
                    if(isLoadMore){
                        if(mHasNext){
                            mPageId++;
                        }else {
                            return mChannelList;
                        }
                    }else{
                        mChannelList.clear();
                        mHasNext = false;
                        mPageId = 0;
                    }
                    final HashMap<String, String> params = new HashMap<String, String>();
                    params.put(Constants.PAGE_ID, Integer.toString(mPageId));
                    params.put(Constants.PAGE_SIZE, Integer.toString(mPageSize));
                    params.put(Constants.SORT_BY, mSortBy);
                    params.put(Constants.SORT_ORDER, mSortOrder);
                    mLogger.debug(TAG+" getAllChannelOfGroup "+Constants.SORT_BY+Constants.SPACE+mSortBy+Constants.SPACE+Constants.SORT_ORDER+Constants.SPACE+mSortOrder+Constants.SPACE+Constants.PAGE_SIZE+Constants.SPACE+mPageSize);
                    resourceUrl = appendPagingParameters(resourceUrl, params);
                    urlConnection = (HttpURLConnection) new URL(resourceUrl).openConnection();
                    urlConnection.setConnectTimeout(Constants.PREPARING_TIMEOUT);
                    final JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader(urlConnection.getInputStream())));
                    parseChannels(reader);
                }
                catch (MalformedURLException e) {
                    exception = new Exception(Constants.THUMB_URL_ERROR);
                }
                catch (UnsupportedEncodingException e) {
                    exception = new Exception(Constants.ENCODING_ERROR);
                }
                catch (IOException e) {
                    String error = null;
                    if(urlConnection != null){
                        final JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader(urlConnection.getErrorStream())));
                        error = parseError(reader);
                    }
                    exception = new Exception(error == null?Constants.CONNECTION_ERROR:error);
                }
                finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    if(exception != null){
                        if(mPageId != 0){
                            mPageId--;
                        }
                        throw exception;
                    }
                }
                return mChannelList;
            }
        }
    }

    /**
     * This method will fetch all the channels of a particular channel group.
     * This method does not requires authentication.
     * This is as asynchronous call and list of channels is returned in callback.
     * @param channelGroupId ChannelGroupId
     * @param isLoadMore True if load more called else false.
     * @param callback ChannelCallback
     */
    public void getAllChannelOfGroupAsync(final String channelGroupId, final boolean isLoadMore,final ChannelCallback callback) {

        if (mOrgId == null || mOrgId.trim().isEmpty()) {
            if(callback != null){
                callback.onError(new Throwable(Constants.ORG_ERROR));
            }
        } else {
            if(!Connection.isConnected(mContext)){
                if(callback != null){
                    callback.onError(new Throwable(Constants.CONNECTION_ERROR));
                }
            }else{
                final Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        HttpURLConnection urlConnection = null;
                        Throwable throwable = null;
                        try {
                            String resourceUrl = Setting.getApiEndPoint() + String.format(Constants.FETCH_ALL_CHANNEL_OF_GROUP_PATH, mOrgId,channelGroupId);
                            mLogger.debug(TAG + " getAllChannelOfGroupAsync "+Constants.RES_URL+resourceUrl + Constants.IS_LOAD_MORE+ isLoadMore);
                            mLogger.debug(TAG + " getAllChannelOfGroupAsync "+ " mHasNextChannelOfGroup: " + mHasNext +" mPageIdChannelOfGroup: " + mPageId);
                            if(isLoadMore){
                                if(mHasNext){
                                    mPageId++;
                                }else {
                                    if(callback != null){
                                        callback.onSuccess(mChannelList);
                                    }
                                    return;
                                }
                            }else{
                                mChannelList.clear();
                                mHasNext = false;
                                mPageId = 0;
                            }
                            HashMap<String, String> params = new HashMap<String, String>();
                            params.put(Constants.PAGE_ID, Integer.toString(mPageId));
                            params.put(Constants.PAGE_SIZE, Integer.toString(mPageSize));
                            params.put(Constants.SORT_BY, mSortBy);
                            params.put(Constants.SORT_ORDER, mSortOrder);
                            mLogger.debug(TAG+" getAllChannelOfGroupAsync "+Constants.SORT_BY+Constants.SPACE+mSortBy+Constants.SPACE+Constants.SORT_ORDER+Constants.SPACE+mSortOrder+Constants.SPACE+Constants.PAGE_SIZE+Constants.SPACE+mPageSize);
                            resourceUrl = appendPagingParameters(resourceUrl, params);
                            urlConnection = (HttpURLConnection) new URL(resourceUrl).openConnection();
                            urlConnection.setConnectTimeout(Constants.PREPARING_TIMEOUT);
                            JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader(urlConnection.getInputStream())));
                            parseChannels(reader);
                            if(callback != null){
                                callback.onSuccess(mChannelList);
                            }
                        }
                        catch (UnsupportedEncodingException e) {
                            throwable = new Throwable(Constants.ENCODING_ERROR);
                        }
                        catch (MalformedURLException e) {
                            throwable = new Throwable(Constants.THUMB_URL_ERROR);
                        }
                        catch (IOException e) {
                            String error = null;
                            if(urlConnection != null){
                                JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader(urlConnection.getErrorStream())));
                                error = parseError(reader);
                            }
                            throwable = new Throwable(error == null?Constants.CONNECTION_ERROR:error);
                        }
                        finally {
                            if (urlConnection != null) {
                                urlConnection.disconnect();
                            }
                            if(throwable != null){
                                if(mPageId != 0){
                                    mPageId--;
                                }
                                if(callback != null){
                                    callback.onError(throwable);
                                }
                            }
                        }
                    }
                };
                final Thread thread = new Thread(runnable);
                thread.start();
            }
        }
    }

    /**
     * This method fetches all the channels from server.
     * This is a blocking call.
     * @param isLoadMore True if load more called else false.
     * @return list of channels
     * @throws Exception
     */
    public ArrayList<Channel> getAllChannel(boolean isLoadMore) throws Exception{

        if (!Setting.isAccountConfigured(mOrgId, mAccessKey, mSecret)) {
            throw new Exception(Constants.ACCOUNT_ERROR);
        } else {
            if(!Connection.isConnected(mContext)){
                throw new Exception(Constants.CONNECTION_ERROR);
            }else{
                Exception exception = null;
                HttpURLConnection urlConnection = null;
                try {
                    final String resourceUrl = Setting.getApiEndPoint() + String.format(Constants.FETCH_ALL_CHANNEL_PATH, mOrgId);
                    mLogger.debug(TAG + " getAllChannel "+Constants.RES_URL+resourceUrl + Constants.IS_LOAD_MORE+ isLoadMore);
                    mLogger.debug(TAG + " getAllChannel "+ " mHasNextChannel: " + mHasNext +" mPageIdChannel: " + mPageId);
                    if(isLoadMore){
                        if(mHasNext){
                            mPageId++;
                        }else {
                            return mChannelList;
                        }
                    }else{
                        mChannelList.clear();
                        mHasNext = false;
                        mPageId = 0;
                    }
                    final HashMap<String, String> params = new HashMap<String, String>();
                    params.put(Constants.PAGE_ID, Integer.toString(mPageId));
                    params.put(Constants.PAGE_SIZE, Integer.toString(mPageSize));
                    params.put(Constants.SORT_BY, mSortBy);
                    params.put(Constants.SORT_ORDER, mSortOrder);
                    mLogger.debug(TAG+" getAllChannel "+Constants.SORT_BY+Constants.SPACE+mSortBy+Constants.SPACE+Constants.SORT_ORDER+Constants.SPACE+mSortOrder+Constants.SPACE+Constants.PAGE_SIZE+Constants.SPACE+mPageSize);
                    final String url = URLAuthenticator.authenticateRequest(Constants.GET,resourceUrl, mAccessKey, mSecret, params);
                    urlConnection = (HttpURLConnection) new URL(url).openConnection();
                    urlConnection.setConnectTimeout(Constants.PREPARING_TIMEOUT);
                    final JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader(urlConnection.getInputStream())));
                    parseChannels(reader);
                }
                catch (MalformedURLException e) {
                    exception = new Exception(Constants.URL_ERROR);
                }
                catch(InvalidKeyException e){
                    exception = new Exception(Constants.AUTH_ERROR);
                }
                catch(UnsupportedEncodingException e){
                    exception = new Exception(Constants.AUTH_ERROR);
                }
                catch(URISyntaxException e){
                    exception = new Exception(Constants.AUTH_ERROR);
                }
                catch(IOException e){
                    String error = null;
                    if(urlConnection != null){
                        final JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader(urlConnection.getErrorStream())));
                        error = parseError(reader);
                    }
                    exception = new Exception(error == null?Constants.CONNECTION_ERROR:error);
                }
                finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    if(exception != null){
                        if(mPageId != 0){
                            mPageId--;
                        }
                        throw exception;
                    }
                }
                return mChannelList;
            }
        }
    }

    /**
     * This method fetches all the channels from server.
     * This is as asynchronous call and list of channels is returned in callback.
     * @param isLoadMore True if load more called else false.
     * @param callback ChannelCallback
     */
    public void getAllChannelAsync(boolean isLoadMore,final ChannelCallback callback) {

        if (!Setting.isAccountConfigured(mOrgId, mAccessKey, mSecret)) {
            if(callback != null){
                callback.onError(new Throwable(Constants.ACCOUNT_ERROR));
            }
        } else {
            if(!Connection.isConnected(mContext)){
                if(callback != null){
                    callback.onError(new Throwable(Constants.CONNECTION_ERROR));
                }
            }else{
                try {
                    final String resourceUrl = Setting.getApiEndPoint() + String.format(Constants.FETCH_ALL_CHANNEL_PATH, mOrgId);
                    mLogger.debug(TAG + " getAllChannelAsync "+Constants.RES_URL+resourceUrl + Constants.IS_LOAD_MORE+ isLoadMore);
                    mLogger.debug(TAG + " getAllChannelAsync "+ " mHasNextChannel: " + mHasNext +" mPageIdChannel: " + mPageId);
                    if(isLoadMore){
                        if(mHasNext){
                            mPageId++;
                        }else {
                            if(callback != null){
                                callback.onSuccess(mChannelList);
                            }
                            return;
                        }
                    }else{
                        mChannelList.clear();
                        mHasNext = false;
                        mPageId = 0;
                    }
                    final HashMap<String, String> params = new HashMap<String, String>();
                    params.put(Constants.PAGE_ID, Integer.toString(mPageId));
                    params.put(Constants.PAGE_SIZE, Integer.toString(mPageSize));
                    params.put(Constants.SORT_BY, mSortBy);
                    params.put(Constants.SORT_ORDER, mSortOrder);
                    mLogger.debug(TAG+" getAllChannelAsync "+Constants.SORT_BY+Constants.SPACE+mSortBy+Constants.SPACE+Constants.SORT_ORDER+Constants.SPACE+mSortOrder+Constants.SPACE+Constants.PAGE_SIZE+Constants.SPACE+mPageSize);
                    final String url = URLAuthenticator.authenticateRequest(Constants.GET,resourceUrl, mAccessKey, mSecret, params);
                    final Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            Throwable throwable = null;
                            HttpURLConnection urlConnection = null;
                            try {
                                urlConnection = (HttpURLConnection) new URL(url).openConnection();
                                urlConnection.setConnectTimeout(Constants.PREPARING_TIMEOUT);
                                final JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader(urlConnection.getInputStream())));
                                parseChannels(reader);
                                if(callback != null){
                                    callback.onSuccess(mChannelList);
                                } 
                            }
                            catch (MalformedURLException e) {
                                throwable = new Throwable(Constants.THUMB_URL_ERROR);
                            }
                            catch (IOException e) {
                                String error = null;
                                if(urlConnection != null){
                                    final JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader(urlConnection.getErrorStream())));
                                    error = parseError(reader);
                                }
                                throwable = new Throwable(error == null?Constants.CONNECTION_ERROR:error);
                            }
                            finally {
                                if (urlConnection != null) {
                                    urlConnection.disconnect();
                                }
                                if(throwable != null){
                                    if(mPageId != 0){
                                        mPageId--;
                                    }
                                    if(callback != null){
                                        callback.onError(throwable);
                                    }
                                }
                            }
                        }
                    };
                    final Thread thread = new Thread(runnable);
                    thread.start();
                }
                catch (Exception e) {
                    if(callback != null){
                        callback.onError(new Throwable(Constants.AUTH_ERROR));
                    }
                }
            }
        }
    }

    /**
     * This method will fetch all the media of a particular channel.
     * This method does not requires authentication.
     * This is a blocking call.
     * @param channelId ChannelId
     * @param isLoadMore True if load more called else false.
     * @return list of medias
     * @throws Exception
     */
    public ArrayList<Media> getAllMediaOfChannel(String channelId,boolean isLoadMore) throws Exception{

        if (mOrgId == null || mOrgId.trim().isEmpty()) {
            throw new Exception(Constants.ORG_ERROR);
        } else {
            if(!Connection.isConnected(mContext)){
                throw new Exception(Constants.CONNECTION_ERROR);
            }else{
                Exception exception = null;
                HttpURLConnection urlConnection = null;
                try {
                    String resourceUrl = Setting.getApiEndPoint() + String.format(Constants.FETCH_ALL_MEDIA_OF_CHANNEL_PATH, mOrgId,channelId);
                    mLogger.debug(TAG + " getAllMediaOfChannel "+Constants.RES_URL+resourceUrl + Constants.IS_LOAD_MORE+ isLoadMore);
                    mLogger.debug(TAG + " getAllMediaOfChannel "+ " mHasNextMediaOfChannel: " + mHasNext +" mPageIdMediaOfChannel: " + mPageId);
                    if(isLoadMore){
                        if(mHasNext){
                           mPageId++;
                        }else {
                            return mMediaList;
                        }
                    }else{
                        mMediaList.clear();
                        mHasNext = false;
                        mPageId = 0;
                    }
                    final HashMap<String, String> params = new HashMap<String, String>();
                    params.put(Constants.PAGE_ID, Integer.toString(mPageId));
                    params.put(Constants.PAGE_SIZE, Integer.toString(mPageSize));
                    params.put(Constants.SORT_BY, mSortBy);
                    params.put(Constants.SORT_ORDER, mSortOrder);
                    StringBuilder searchStr = new StringBuilder();
                    searchStr = searchStr.append(String.format(Constants.SEARCH_PATTERN, Constants.STATE,"published"));
                    params.put("and", searchStr.toString());
                    mLogger.debug(TAG+" getAllMediaOfChannel "+Constants.SORT_BY+Constants.SPACE+mSortBy+Constants.SPACE+Constants.SORT_ORDER+Constants.SPACE+mSortOrder+Constants.SPACE+Constants.PAGE_SIZE+Constants.SPACE+mPageSize);
                    resourceUrl = appendPagingParameters(resourceUrl, params);
                    urlConnection = (HttpURLConnection) new URL(resourceUrl).openConnection();
                    urlConnection.setConnectTimeout(Constants.PREPARING_TIMEOUT);
                    final JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader(urlConnection.getInputStream())));
                    parseMedias(reader);
                }
                catch (UnsupportedEncodingException e) {
                    exception = new Exception(Constants.ENCODING_ERROR);
                }
                catch (MalformedURLException e) {
                    exception = new Exception(Constants.THUMB_URL_ERROR);
                }
                catch (IOException e) {
                    String error = null;
                    if(urlConnection != null){
                        final JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader(urlConnection.getErrorStream())));
                        error = parseError(reader);
                    }
                    exception = new Exception(error == null?Constants.CONNECTION_ERROR:error);
                }
                finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    if(exception != null){
                        if(mPageId != 0){
                            mPageId--;
                        }
                        throw exception;
                    }
                }
                return mMediaList;
            }
        }
    }

    /**
     * This method will fetch all the media of a particular channel.
     * This method does not requires authentication.
     * This is as async call and list of media is returned in callback.
     * @param channelId ChannelId
     * @param isLoadMore True if load more called else false.
     * @param callback MediaCallback
     */
    public void getAllMediaOfChannelAsync(final String channelId,final boolean isLoadMore,final MediaCallback callback) {

        if (mOrgId == null || mOrgId.trim().isEmpty()) {
            if(callback != null){
                callback.onError(new Throwable(Constants.ORG_ERROR));
            }
        } else {
            if(!Connection.isConnected(mContext)){
                if(callback != null){
                    callback.onError(new Throwable(Constants.CONNECTION_ERROR));
                }
            }else{
                final Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        Throwable throwable = null;
                        HttpURLConnection urlConnection = null;
                        try {
                            String resourceUrl = Setting.getApiEndPoint() + String.format(Constants.FETCH_ALL_MEDIA_OF_CHANNEL_PATH, mOrgId,channelId);
                            mLogger.debug(TAG + " getAllMediaOfChannelAsync "+Constants.RES_URL+resourceUrl + Constants.IS_LOAD_MORE+ isLoadMore);
                            mLogger.debug(TAG + " getAllMediaOfChannelAsync "+ " mHasNextMediaOfChannel: " + mHasNext +" mPageIdMediaOfChannel: " + mPageId);
                            if(isLoadMore){
                                if(mHasNext){
                                    mPageId++;
                                }else {
                                    if(callback != null){
                                        callback.onSuccess(mMediaList);
                                    }
                                    return;
                                }
                            }else{
                                mMediaList.clear();
                                mHasNext = false;
                                mPageId = 0;
                            }
                            HashMap<String, String> params = new HashMap<String, String>();
                            params.put(Constants.PAGE_ID, Integer.toString(mPageId));
                            params.put(Constants.PAGE_SIZE, Integer.toString(mPageSize));
                            params.put(Constants.SORT_BY, mSortBy);
                            params.put(Constants.SORT_ORDER, mSortOrder);
                            StringBuilder searchStr = new StringBuilder();
                            searchStr = searchStr.append(String.format(Constants.SEARCH_PATTERN, Constants.STATE,"published"));
                            params.put("and", searchStr.toString());
                            mLogger.debug(TAG+" getAllMediaOfChannelAsync "+Constants.SORT_BY+Constants.SPACE+mSortBy+Constants.SPACE+Constants.SORT_ORDER+Constants.SPACE+mSortOrder+Constants.SPACE+Constants.PAGE_SIZE+Constants.SPACE+mPageSize);
                            resourceUrl = appendPagingParameters(resourceUrl, params);
                            urlConnection = (HttpURLConnection) new URL(resourceUrl).openConnection();
                            urlConnection.setConnectTimeout(Constants.PREPARING_TIMEOUT);
                            JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader(urlConnection.getInputStream())));
                            parseMedias(reader);
                            if(callback != null){
                                callback.onSuccess(mMediaList);
                            }
                        }
                        catch (UnsupportedEncodingException e) {
                            throwable = new Throwable(Constants.ENCODING_ERROR);
                        }
                        catch (MalformedURLException e) {
                            throwable = new Throwable(Constants.THUMB_URL_ERROR);
                        }
                        catch (IOException e) {
                            String error = null;
                            if(urlConnection != null){
                                JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader(urlConnection.getErrorStream())));
                                error = parseError(reader);
                            }
                            throwable = new Throwable(error == null?Constants.CONNECTION_ERROR:error);
                        }
                        finally {
                            if (urlConnection != null) {
                                urlConnection.disconnect();
                            }
                            if(throwable != null){
                                if(mPageId != 0){
                                    mPageId--;
                                }
                                if(callback != null){
                                    callback.onError(throwable);
                                }
                            }
                        }
                    }
                };
                final Thread thread = new Thread(runnable);
                thread.start();
            }
        }
    }

    /**
    * This method will fetch all the media on server.
    * This is a blocking call.
    * @param isLoadMore True if load more called else false.
    * @return list of media
    * @throws Exception
    */
    public ArrayList<Media> getAllMedia(boolean isLoadMore) throws Exception{

        if (!Setting.isAccountConfigured(mOrgId, mAccessKey, mSecret)) {
            throw new Exception(Constants.ACCOUNT_ERROR);
        } else {
            if(!Connection.isConnected(mContext)){
                throw new Exception(Constants.CONNECTION_ERROR);
            }else{
                Exception exception = null;
                HttpURLConnection urlConnection = null;
                try {
                    final String resourceUrl = Setting.getApiEndPoint() + String.format(Constants.SEARCH_ALL_MEDIA_PATH, mOrgId);
                    mLogger.debug(TAG + " getAllMedia "+Constants.RES_URL+resourceUrl + Constants.IS_LOAD_MORE+ isLoadMore);
                    mLogger.debug(TAG + " getAllMedia "+ " mHasNextMediaOfChannel: " + mHasNext +" mPageIdMedia: " + mPageId);
                    if(isLoadMore){
                        if(mHasNext){
                           mPageId++;
                        }else {
                            return mMediaList;
                        }
                    }else{
                        mMediaList.clear();
                        mHasNext = false;
                        mPageId = 0;
                    }
                    final HashMap<String, String> params = new HashMap<String, String>();
                    params.put(Constants.PAGE_ID, Integer.toString(mPageId));
                    params.put(Constants.PAGE_SIZE, Integer.toString(mPageSize));
                    params.put(Constants.SORT_BY, mSortBy);
                    params.put(Constants.SORT_ORDER, mSortOrder);
                    StringBuilder searchStr = new StringBuilder();
                    searchStr = searchStr.append(String.format(Constants.SEARCH_PATTERN, Constants.STATE,"published"));
                    params.put("and", searchStr.toString());
                    mLogger.debug(TAG+" getAllMedia "+Constants.SORT_BY+Constants.SPACE+mSortBy+Constants.SPACE+Constants.SORT_ORDER+Constants.SPACE+mSortOrder+Constants.SPACE+Constants.PAGE_SIZE+Constants.SPACE+mPageSize);
                    final String url = URLAuthenticator.authenticateRequest(Constants.GET,resourceUrl, mAccessKey, mSecret, params);
                    urlConnection = (HttpURLConnection) new URL(url).openConnection();
                    urlConnection.setConnectTimeout(Constants.PREPARING_TIMEOUT);
                    final JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader(urlConnection.getInputStream())));
                    parseMedias(reader);
                }catch (MalformedURLException e) {
                    exception = new Exception(Constants.URL_ERROR);
                }
                catch(InvalidKeyException e){
                    exception = new Exception(Constants.AUTH_ERROR);
                }
                catch(UnsupportedEncodingException e){
                    exception = new Exception(Constants.AUTH_ERROR);
                }
                catch(URISyntaxException e){
                    exception = new Exception(Constants.AUTH_ERROR);
                }
                catch(IOException e){
                    String error = null;
                    if(urlConnection != null){
                        final JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader(urlConnection.getErrorStream())));
                        error = parseError(reader);
                    }
                    exception = new Exception(error == null?Constants.CONNECTION_ERROR:error);
                }
                finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    if(exception != null){
                        if(mPageId != 0){
                            mPageId--;
                        }
                        throw exception;
                    }
                }
                return mMediaList;
            }
        }
    }

    /**
     * This method will fetch all the media on server.
     * This is as asynchronous call and list of media is returned in callback.
     * @param isLoadMore True if load more called else false.
     * @param callback MediaCallback
     */
    public void getAllMediaAsync(boolean isLoadMore,final MediaCallback callback) {

        if (!Setting.isAccountConfigured(mOrgId, mAccessKey, mSecret)) {
            if(callback != null){
                callback.onError(new Throwable(Constants.ACCOUNT_ERROR));
            }
        } else {
            if(!Connection.isConnected(mContext)){
                if(callback != null){
                    callback.onError(new Throwable(Constants.CONNECTION_ERROR));
                }
            }else{
                try {
                    final String resourceUrl = Setting.getApiEndPoint() + String.format(Constants.SEARCH_ALL_MEDIA_PATH, mOrgId);
                    mLogger.debug(TAG + " getAllMediaAsync "+Constants.RES_URL+resourceUrl + Constants.IS_LOAD_MORE+ isLoadMore);
                    mLogger.debug(TAG + " getAllMediaAsync "+ " mHasNextMediaOfChannel: " + mHasNext +" mPageIdMedia: " + mPageId);
                    if(isLoadMore){
                        if(mHasNext){
                            mPageId++;
                        }else {
                            if(callback != null){
                                callback.onSuccess(mMediaList);
                            }
                            return;
                        }
                    }else{
                        mMediaList.clear();
                        mHasNext = false;
                        mPageId = 0;
                    }
                    final HashMap<String, String> params = new HashMap<String, String>();
                    params.put(Constants.PAGE_ID, Integer.toString(mPageId));
                    params.put(Constants.PAGE_SIZE, Integer.toString(mPageSize));
                    params.put(Constants.SORT_BY, mSortBy);
                    params.put(Constants.SORT_ORDER, mSortOrder);
                    StringBuilder searchStr = new StringBuilder();
                    searchStr = searchStr.append(String.format(Constants.SEARCH_PATTERN, Constants.STATE,"published"));
                    params.put("and", searchStr.toString());
                    mLogger.debug(TAG+" getAllMediaAsync "+Constants.SORT_BY+Constants.SPACE+mSortBy+Constants.SPACE+Constants.SORT_ORDER+Constants.SPACE+mSortOrder+Constants.SPACE+Constants.PAGE_SIZE+Constants.SPACE+mPageSize);
                    final String url = URLAuthenticator.authenticateRequest(Constants.GET,resourceUrl, mAccessKey, mSecret, params);
                    final Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            Throwable throwable = null;
                            HttpURLConnection urlConnection = null;
                            try {
                                urlConnection = (HttpURLConnection) new URL(url).openConnection();
                                urlConnection.setConnectTimeout(Constants.PREPARING_TIMEOUT);
                                JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader(urlConnection.getInputStream())));
                                parseMedias(reader);
                                if(callback != null){
                                    callback.onSuccess(mMediaList);
                                }
                            }
                            catch (MalformedURLException e1) {
                                throwable = new Throwable(Constants.THUMB_URL_ERROR);
                            }
                            catch (IOException e) {
                                String error = null;
                                if(urlConnection != null){
                                    JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader(urlConnection.getErrorStream())));
                                    error = parseError(reader);
                                }
                                throwable = new Throwable(error == null?Constants.CONNECTION_ERROR:error);
                            }
                            finally {
                                if (urlConnection != null) {
                                    urlConnection.disconnect();
                                }
                                if(throwable != null){
                                    if(mPageId != 0){
                                        mPageId--;
                                    }
                                    if(callback != null){
                                        callback.onError(throwable);
                                    }
                                }
                            }
                        }
                    };
                    final Thread thread = new Thread(runnable);
                    thread.start();
                } 
                catch (Exception e) {
                    if(callback != null){
                        callback.onError(new Throwable(Constants.AUTH_ERROR));
                    }
                }
            }
        }
    }

    /**
     * This method searches for media on server as per the search parameters supplied.
     * @param isLoadMore true if load next page
     * @param operator And / OR - Optional
     * @param title Title of Media - Optional
     * @param description Description of Media - Optional
     * @param originalFilename Name of the original file - Optional
     * @param tag Tag of Media - Optional
     * @param state state of Media(published/unpublished) - Optional
     * @param mediaType Type of media {@link MediaType}- Optional
     * @param channelId channel id for the medias - Optional
     * @param createdAfter String holding the Date the media is created after, date should be in unix format - Optional
     * @param updatedAfter String holding the Date the media is updated after, date should be in unix format - Optional
     * @param publishedAfter String holding the Date the media is published after, date should be in unix format - Optional
     * @throws Exception
     */
    public ArrayList<Media> searchMedia(final boolean isLoadMore,final String operator,final String title,final String description,final String originalFilename,
            final String tag,final String state,final String mediaType,final String channelId,final String createdAfter, final String updatedAfter, final String publishedAfter) throws Exception{

        if (!Setting.isAccountConfigured(mOrgId, mAccessKey, mSecret)) {
            throw new Exception(Constants.ACCOUNT_ERROR);
        } else {
            if(!Connection.isConnected(mContext)){
                throw new Exception(Constants.CONNECTION_ERROR);
            }else{
                Exception exception = null;
                HttpURLConnection urlConnection = null;
                mLogger.debug(TAG + " searchMedia "+ " mHasNextSearchMedia: " + mHasNext +" mPageIdSearchMedia: " + mPageId);
                try {
                    if(isLoadMore){
                        if(mHasNext){
                             mPageId++;
                        }else {
                            return mMediaList;
                        }
                    }else{
                        mMediaList.clear();
                        mHasNext = false;
                        mPageId = 0;
                    }
                    StringBuilder searchStr = new StringBuilder();
                    if(title!= null){
                        searchStr = searchStr.append(String.format(Constants.SEARCH_PATTERN, Constants.TITLE,title));
                    }
                    if(description!= null){
                        searchStr = searchStr.append(String.format(Constants.SEARCH_PATTERN, Constants.DESCRIPTION,description));
                    }
                    if(originalFilename!= null){
                        searchStr = searchStr.append(String.format(Constants.SEARCH_PATTERN, Constants.ORIGINAL_FILENAME,originalFilename));
                    }
                    if(tag!= null){
                        searchStr = searchStr.append(String.format(Constants.SEARCH_PATTERN, Constants.TAG,tag));
                    }
                    if(state!= null){
                        searchStr = searchStr.append(String.format(Constants.SEARCH_PATTERN, Constants.STATE,state));
                    }
                    if(mediaType!= null){
                        searchStr = searchStr.append(String.format(Constants.SEARCH_PATTERN, Constants.MEDIA_TYPE,mediaType));
                    }
                    if(channelId!= null){
                        searchStr = searchStr.append(String.format(Constants.SEARCH_PATTERN, Constants.CHANNEL_ID,channelId));
                    }
                    if(createdAfter!= null){
                        searchStr = searchStr.append(String.format(Constants.SEARCH_PATTERN, Constants.CREATED_AFTER,createdAfter));
                    }
                    if(updatedAfter!= null){
                        searchStr = searchStr.append(String.format(Constants.SEARCH_PATTERN, Constants.UPDATED_AFTER,updatedAfter));
                    }
                    if(publishedAfter!= null){
                        searchStr = searchStr.append(String.format(Constants.SEARCH_PATTERN, Constants.PUBLISHED_AFTER,publishedAfter));
                    }
                    if(searchStr!= null){
                        searchStr = searchStr.deleteCharAt(searchStr.length()-1);
                    }
                    final HashMap<String, String> params = new HashMap<String, String>();
                    params.put(Constants.PAGE_ID, Integer.toString(mPageId));
                    params.put(Constants.PAGE_SIZE, Integer.toString(mPageSize));
                    params.put(Constants.SORT_BY, mSortBy);
                    params.put(Constants.SORT_ORDER, mSortOrder);
                    params.put(operator, searchStr.toString());
                    mLogger.debug(TAG +" searchMedia "+ " search String: "+ searchStr);
                    final String resourceUrl = Setting.getApiEndPoint() + String.format(Constants.SEARCH_ALL_MEDIA_PATH, mOrgId);
                    mLogger.debug(TAG + " searchMedia "+Constants.RES_URL+resourceUrl + Constants.IS_LOAD_MORE+ isLoadMore);
                    mLogger.debug(TAG+" searchMedia "+Constants.SORT_BY+Constants.SPACE+mSortBy+Constants.SPACE+Constants.SORT_ORDER+Constants.SPACE+mSortOrder+Constants.SPACE+Constants.PAGE_SIZE+Constants.SPACE+mPageSize);
                    final String url = URLAuthenticator.authenticateRequest(Constants.GET,resourceUrl, mAccessKey, mSecret, params);
                    urlConnection = (HttpURLConnection) new URL(url).openConnection();
                    urlConnection.setConnectTimeout(Constants.PREPARING_TIMEOUT);
                    final JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader(urlConnection.getInputStream())));
                    parseMedias(reader);
                }catch (MalformedURLException e) {
                    exception = new Exception(Constants.URL_ERROR);
                }
                catch(InvalidKeyException e){
                    exception = new Exception(Constants.AUTH_ERROR);
                }
                catch(UnsupportedEncodingException e){
                    exception = new Exception(Constants.AUTH_ERROR);
                }
                catch(URISyntaxException e){
                    exception = new Exception(Constants.AUTH_ERROR);
                }
                catch(IOException e){
                    String error = null;
                    if(urlConnection != null){
                        final JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader(urlConnection.getErrorStream())));
                        error = parseError(reader);
                    }
                    exception = new Exception(error == null?Constants.CONNECTION_ERROR:error);
                }
                finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    if(exception != null){
                        if(mPageId != 0){
                            mPageId--;
                        }
                        throw exception;
                    }
                }
                return mMediaList;
            }
        }
    }

    /**
     * This method searches for media on server as per the search parameters supplied.
     * This is as asynchronous call and list of media is returned in callback.
     * @param isLoadMore true if load next page
     * @param operator And / OR - Optional
     * @param title Title of Media - Optional
     * @param description Description of Media - Optional
     * @param originalFilename Name of the original file - Optional
     * @param tag Tag of Media - Optional
     * @param state state of Media(published/unpublished) - Optional
     * @param mediaType Type of media {@link MediaType}- Optional
     * @param channelId channel id for the medias - Optional
     * @param created_after String holding the Date the media is created after, date should be in unix format - Optional
     * @param updated_after String holding the Date the media is updated after, date should be in unix format - Optional
     * @param published_after String holding the Date the media is published after, date should be in unix format - Optional
     * @param callback MediaCallback
     * @throws Exception
     */
    public void searchMediaAsync(final boolean isLoadMore,final String operator,final String title,final String description,final String originalFilename,
            final String tag,final String state,final String mediaType,final String channelId,final String createdAfter,final String updatedAfter,final String publishedAfter,final MediaCallback callback) throws Exception{

        if (!Setting.isAccountConfigured(mOrgId, mAccessKey, mSecret)) {
            if(callback != null){
                callback.onError(new Throwable(Constants.ACCOUNT_ERROR));
            }
        } else {
            if(!Connection.isConnected(mContext)){
                if(callback != null){
                    callback.onError(new Throwable(Constants.CONNECTION_ERROR));
                }
            }else{
                mLogger.debug(TAG + " searchMediaAsync "+ " mHasNextSearchMedia: " + mHasNext +" mPageIdSearchMedia: " + mPageId);
                if(isLoadMore){
                    if(mHasNext){
                        mPageId++;
                    }else {
                        if(callback != null){
                            callback.onSuccess(mMediaList);
                        }
                        return;
                    }
                }else{
                    mMediaList.clear();
                    mHasNext = false;
                    mPageId = 0;
                }
                StringBuilder searchStr = new StringBuilder();
                if(title!= null){
                    searchStr = searchStr.append(String.format(Constants.SEARCH_PATTERN, Constants.TITLE,title));
                }
                if(description!= null){
                    searchStr = searchStr.append(String.format(Constants.SEARCH_PATTERN, Constants.DESCRIPTION,description));
                }
                if(originalFilename!= null){
                    searchStr = searchStr.append(String.format(Constants.SEARCH_PATTERN, Constants.ORIGINAL_FILENAME,originalFilename));
                }
                if(tag!= null){
                    searchStr = searchStr.append(String.format(Constants.SEARCH_PATTERN, Constants.TAG,tag));
                }
                if(state!= null){
                    searchStr = searchStr.append(String.format(Constants.SEARCH_PATTERN, Constants.STATE,state));
                }
                if(mediaType!= null){
                    searchStr = searchStr.append(String.format(Constants.SEARCH_PATTERN, Constants.MEDIA_TYPE,mediaType));
                }
                if(channelId!= null){
                    searchStr = searchStr.append(String.format(Constants.SEARCH_PATTERN, Constants.CHANNEL_ID,channelId));
                }
                if(createdAfter!= null){
                    searchStr = searchStr.append(String.format(Constants.SEARCH_PATTERN, Constants.CREATED_AFTER,createdAfter));
                }
                if(updatedAfter!= null){
                    searchStr = searchStr.append(String.format(Constants.SEARCH_PATTERN, Constants.UPDATED_AFTER,updatedAfter));
                }
                if(publishedAfter!= null){
                    searchStr = searchStr.append(String.format(Constants.SEARCH_PATTERN, Constants.PUBLISHED_AFTER,publishedAfter));
                }
                if(searchStr!= null){
                    searchStr = searchStr.deleteCharAt(searchStr.length()-1);
                }
                final HashMap<String, String> params = new HashMap<String, String>();
                params.put(Constants.PAGE_ID, Integer.toString(mPageId));
                params.put(Constants.PAGE_SIZE, Integer.toString(mPageSize));
                params.put(Constants.SORT_BY, mSortBy);
                params.put(Constants.SORT_ORDER, mSortOrder);
                params.put(operator, searchStr.toString());
                mLogger.debug(TAG +" searchMediaAsync "+ " search String: "+ searchStr);
                mLogger.debug(TAG+" searchMediaAsync "+Constants.SORT_BY+Constants.SPACE+mSortBy+Constants.SPACE+Constants.SORT_ORDER+Constants.SPACE+mSortOrder+Constants.SPACE+Constants.PAGE_SIZE+Constants.SPACE+mPageSize);
                final String resourceUrl = Setting.getApiEndPoint() + String.format(Constants.SEARCH_ALL_MEDIA_PATH, mOrgId);
                mLogger.debug(TAG + " searchMediaAsync "+Constants.RES_URL+resourceUrl + Constants.IS_LOAD_MORE+ isLoadMore);
                try {
                    final String url = URLAuthenticator.authenticateRequest(Constants.GET,resourceUrl, mAccessKey, mSecret, params);
                    final Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            Throwable throwable = null;
                            HttpURLConnection urlConnection = null;
                            try {
                                urlConnection = (HttpURLConnection) new URL(url).openConnection();
                                urlConnection.setConnectTimeout(Constants.PREPARING_TIMEOUT);
                                JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader(urlConnection.getInputStream())));
                                parseMedias(reader);
                                if(callback != null){
                                    callback.onSuccess(mMediaList);
                                }
                            }
                            catch (MalformedURLException e1) {
                                throwable = new Throwable("Invalid Request URL !");
                            }
                            catch (IOException e) {
                                String error = null;
                                if(urlConnection != null){
                                    JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader(urlConnection.getErrorStream())));
                                    error = parseError(reader);
                                }
                                throwable = new Throwable(error == null?Constants.CONNECTION_ERROR:error);
                            }
                            finally {
                                if (urlConnection != null) {
                                    urlConnection.disconnect();
                                }
                                if(throwable != null){
                                    if(mPageId != 0){
                                        mPageId--;
                                    }
                                    if(callback != null){
                                        callback.onError(throwable);
                                    }
                                }
                            }
                        }
                    };
                    final Thread thread = new Thread(runnable);
                    thread.start();
                }
                catch (Exception e) {
                    if(callback != null){
                        callback.onError(new Throwable(Constants.AUTH_ERROR));
                    }
                }
            }
        }
    }

    /**
     * This method fetches the properties of a specific channel.
     * It does not require authentication.
     * This is as asynchronous call and channel list with a single channel is returned in callback.
     * @param channelId ChannelId
     * @param callback ChannelCallback
     */
    public void getChannelAsync(final String channelId,final ChannelCallback callback) {
        if (mOrgId == null || mOrgId.trim().isEmpty()) {
            if(callback != null){
                callback.onError(new Throwable(Constants.ORG_ERROR));
            }
        } else {
            if(!Connection.isConnected(mContext)){
                if(callback != null){
                    callback.onError(new Throwable(Constants.CONNECTION_ERROR));
                }
            }else{
                final Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        HttpURLConnection urlConnection = null;
                        try {
                            String resourceUrl = Setting.getApiEndPoint() + String.format(Constants.FETCH_CHANNEL_PROPERTY_PATH, mOrgId,channelId);
                            mLogger.debug(TAG + " getChannelAsync " + " resourceUrl "+ resourceUrl);
                            urlConnection = (HttpURLConnection) new URL(resourceUrl).openConnection();
                            urlConnection.setConnectTimeout(Constants.PREPARING_TIMEOUT);
                            JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader(urlConnection.getInputStream())));
                            ArrayList<Channel> channelList = new ArrayList<Channel>();
                            channelList.add(parseChannelProperty(reader));
                            if(callback != null){
                                callback.onSuccess(channelList);
                            }
                        }
                        catch (MalformedURLException e) {
                            if(callback != null){
                                callback.onError(new Throwable("Invalid Request URL !"));
                            }
                        } 
                        catch (IOException e) {
                            String error = null;
                            if(urlConnection != null){
                                JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader(urlConnection.getErrorStream())));
                                error = parseError(reader);
                            }
                            if(callback != null){
                                callback.onError(new Throwable(error == null?Constants.CONNECTION_ERROR:error));
                            }
                        }
                        finally {
                            if (urlConnection != null) {
                                urlConnection.disconnect();
                            }
                        }
                    }
                };
                final Thread thread = new Thread(runnable);
                thread.start();
            }
        }
    }

    /**
     * To fetch the properties of a specific media.
     * It does not require authentication.
     * This is as asynchronous call and list of a single media is returned in callback.
     * @param mediaId mediaId
     * @param callback MediaCallback
     */
    public void getMediaAsync(final String mediaId,final MediaCallback callback) {

        if (mOrgId == null || mOrgId.trim().isEmpty()) {
            if(callback != null){
                callback.onError(new Throwable(Constants.ORG_ERROR));
            }
        } else {
            if(!Connection.isConnected(mContext)){
                if(callback != null){
                    callback.onError(new Throwable(Constants.CONNECTION_ERROR));
                }
            }else{
                final Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        HttpURLConnection urlConnection = null;
                        try {
                            String resourceUrl = Setting.getApiEndPoint() + String.format(Constants.FETCH_MEDIA_PROPERTY_PATH, mOrgId,mediaId);
                            mLogger.debug(TAG + " getMediaAsync " + " resourceUrl "+ resourceUrl);
                            urlConnection = (HttpURLConnection) new URL(resourceUrl).openConnection();
                            urlConnection.setConnectTimeout(Constants.PREPARING_TIMEOUT);
                            JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader(urlConnection.getInputStream())));
                            ArrayList<Media> mediaList = new ArrayList<Media>();
                            mediaList.add(parseMediaProperty(reader));
                            if(callback != null){
                                callback.onSuccess(mediaList);
                            }
                        }
                        catch (MalformedURLException e1) {
                            if(callback != null){
                                callback.onError(new Throwable(Constants.THUMB_URL_ERROR));
                            }
                        }
                        catch (IOException e) {
                            String error = null;
                            if(urlConnection != null){
                                JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader(urlConnection.getErrorStream())));
                                error = parseError(reader);
                            }
                            if(callback != null){
                                callback.onError(new Throwable(error == null?Constants.CONNECTION_ERROR:error));
                            }
                        }
                        finally {
                            if (urlConnection != null) {
                                urlConnection.disconnect();
                            }
                        }
                    }
                };
                final Thread thread = new Thread(runnable);
                thread.start();
            }
        }
    }

    /**
     * This method fetches all the available encodings for the media.
     * @param mediaId media Id
     * @param callback EncodingsCallback
     */
    public void getAllEncodingsForMediaId(final String mediaId,final EncodingsCallback callback) {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put(Constants.PRIMARY_USE, "all");
        if (!Setting.isAccountConfigured(mOrgId, mAccessKey, mSecret)) {
            if(callback != null){
                callback.onError(new Throwable(Constants.ACCOUNT_ERROR));
            }
        } else {
            if(!Connection.isConnected(mContext)){
                if(callback != null){
                    callback.onError(new Throwable(Constants.CONNECTION_ERROR));
                }
            }else {
                try {
                    if(mediaId != null && mediaId.trim().length() == 0){
                        if(callback != null){
                            callback.onError(new Throwable("Invalid Media Id"));
                        }
                        return;
                    }
                    final String resourceUrl = Setting.getApiEndPoint() + String.format(Constants.ENCODING_PATH, mOrgId, mediaId);
                    mLogger.debug(TAG + " getAllEncodingsForMediaId " + " resourceUrl "+ resourceUrl);
                    final String url = URLAuthenticator.authenticateRequest(Constants.GET,resourceUrl, mAccessKey, mSecret, params);
                    final Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            HttpURLConnection urlConnection = null;
                            try {
                                urlConnection = (HttpURLConnection) new URL(url).openConnection();
                                urlConnection.setConnectTimeout(Constants.PREPARING_TIMEOUT);
                                InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                                JsonReader reader = new JsonReader(new InputStreamReader(inputStream));
                                parseEncodings(reader,mediaId);
                                if(callback != null){
                                    callback.onSuccess(mEncodingList);
                                }
                            }
                            catch(JSONException e){
                                if(callback != null){
                                    callback.onError(new Throwable("JSON Error !"));
                                }
                            }
                            catch (MalformedURLException e) {
                                if(callback != null){
                                    callback.onError(new Throwable("MalformedURLException Error !"));
                                }
                            }
                            catch(FileNotFoundException e){
                                if(callback != null){
                                    callback.onError(new Throwable("Invalid Media Id !"));
                                }
                            }
                            catch (IOException e) {
                                String error = null;
                                if(urlConnection != null){
                                    JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader(urlConnection.getErrorStream())));
                                    error = parseError(reader);
                                }
                                if(callback != null){
                                    callback.onError(new Throwable(error == null?Constants.CONNECTION_ERROR:error));
                                }
                            }
                            finally {
                                if (urlConnection != null) {
                                    urlConnection.disconnect();
                                }
                            }
                        }
                    };
                    final Thread thread = new Thread(runnable);
                    thread.start();

                }catch (Exception e) {
                    if(callback != null){
                        callback.onError(new Throwable(Constants.AUTH_ERROR));
                    }
                }
            }
        }
    }

    /**
     * This method parses channel groups from JSON data.
     * @param channelGroups channel group JSON array
     */
    private void parseChannelGroups(final JsonReader reader){
        final GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Date.class, new DateDeserializer());
        builder.registerTypeAdapter(Uri.class, new UriDeserializer());
        final Gson gson = builder.create();
        final JsonParser parser = new JsonParser();
        final JsonObject channelGroups = parser.parse(reader).getAsJsonObject();
        if(channelGroups.isJsonObject()){
            mHasNext = channelGroups.get("has_next").getAsBoolean();
            final JsonArray channelGroupList = channelGroups.get(Constants.CHANNEL_GROUPS).getAsJsonArray();
            if(channelGroupList.isJsonArray()){
                for(int i=0; i< channelGroupList.size(); i++){
                    final ChannelGroup group = gson.fromJson(channelGroupList.get(i),  ChannelGroup.class);
                    mChannelGroupList.add(group);
                }
            }
        }
    }

    /**
     * This method parses channels from JSON data.
     * @param channels channels JSON array
     * @throws JSONException
     */
    private void parseChannels(final JsonReader reader){
        final GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Date.class, new DateDeserializer());
        builder.registerTypeAdapter(Uri.class, new UriDeserializer());
        final Gson gson = builder.create();
        final JsonParser parser = new JsonParser();
        final JsonObject channelsObject = parser.parse(reader).getAsJsonObject();
        if(channelsObject.isJsonObject()){
            mHasNext = channelsObject.get("has_next").getAsBoolean();
            final JsonArray channelsList = channelsObject.get(Constants.CHANNELS).getAsJsonArray();
            if(channelsList.isJsonArray()){
                for(int i=0; i< channelsList.size(); i++){
                    final Channel channel = gson.fromJson(channelsList.get(i),  Channel.class);
                    mChannelList.add(channel);
                }
            }
        }
    }

    /**
     * This method parses channels properties from JSON data.
     * @param channels channels JSON array
     * @throws JSONException
     */
    private Channel parseChannelProperty(final JsonReader reader){
        Channel channel = new Channel();
        final GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Date.class, new DateDeserializer());
        builder.registerTypeAdapter(Uri.class, new UriDeserializer());
        final Gson gson = builder.create();
        final JsonParser parser = new JsonParser();
        final JsonObject channelsObject = parser.parse(reader).getAsJsonObject();
        if(channelsObject.isJsonObject()){
            channel = gson.fromJson(channelsObject,  Channel.class);
        }
        return channel;
    }

    /**
     * This method parses media properties from JSON data.
     * @param medias JSON array
     */
    private Media parseMediaProperty(final JsonReader reader){
        Media media = new Media();
        final GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Date.class, new DateDeserializer());
        builder.registerTypeAdapter(Time.class, new TimeDeserializer());
        builder.registerTypeAdapter(Uri.class, new UriDeserializer());
        builder.registerTypeAdapter(MediaThumbnail.class, new ThumbnailDeserializer());
        final Gson gson = builder.create();
        final JsonParser parser = new JsonParser();
        final JsonObject mediasObject = parser.parse(reader).getAsJsonObject();
        if(mediasObject.isJsonObject()){
            media = gson.fromJson(mediasObject,  Media.class);
        }
        return media;
    }

    /**
     * This method parses media from JSON data.
     * @param medias JSON array
     */
    private void parseMedias(final JsonReader reader){
        final GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Date.class, new DateDeserializer());
        builder.registerTypeAdapter(Time.class, new TimeDeserializer());
        builder.registerTypeAdapter(Uri.class, new UriDeserializer());
        builder.registerTypeAdapter(MediaThumbnail.class, new ThumbnailDeserializer());
        final Gson gson = builder.create();
        final JsonParser parser = new JsonParser();
        final JsonObject mediasObject = parser.parse(reader).getAsJsonObject();
        if(mediasObject.isJsonObject()){
            mHasNext = mediasObject.get("has_next").getAsBoolean();
            final JsonArray mediasList = mediasObject.get(Constants.MEDIAS).getAsJsonArray();
            if(mediasList.isJsonArray()){
                for(int i=0; i< mediasList.size(); i++){
                    final Media media = gson.fromJson(mediasList.get(i),  Media.class);
                    mMediaList.add(media);
                }
            }
        }
    }

    /**
     * This method parses encodings from JSON data.
     * @param reader JsonReader object of encodings
     * @param mediaId
     * @throws JSONException
     */
    private void parseEncodings(final JsonReader reader, final String mediaId) throws JSONException{
        mEncodingList.clear();
        final JsonParser parser = new JsonParser();
        final JsonObject object = parser.parse(reader).getAsJsonObject();
        final JsonArray encodingsList = object.getAsJsonArray(Constants.ENCODINGS);
        for (int i = 0; i < encodingsList.size(); i++) {
            final JsonObject encodingObject = encodingsList.get(i).getAsJsonObject();
            Encoding encoding = new Encoding();
            String use = null;
            if(!(encodingObject.get(Constants.PRIMARY_USE).isJsonNull())){
                use = encodingObject.get(Constants.PRIMARY_USE).getAsString();
                try{
                    encoding.primaryUse = PrimaryUse.valueOf(use);
                }catch(Exception e){
                    mLogger.error(TAG + " Unsupported Primary Use : "+use);
                    continue;
                }
            }
            String url= null;
            if(PrimaryUse.Flash.equals(encoding.primaryUse)){
                if(!(encodingObject.get(Constants.FILE_URL).isJsonNull())){
                    url = encodingObject.get(Constants.FILE_URL).getAsString();
                }
            }else{
                if(!(encodingObject.get(Constants.URL).isJsonNull())){
                    url = encodingObject.get(Constants.URL).getAsString();
                }
            }
            if("null".equalsIgnoreCase(url)|| url == null){
                if(!(encodingObject.get(Constants.MASTER_PLAYLIST_URL).isJsonNull())){
                    url = encodingObject.get(Constants.MASTER_PLAYLIST_URL).getAsString();
                }
            }
            mLogger.error(TAG + " Unsupported URL : "+url);
            //some cases only rtmp is present, dont add the encoding
            if("null".equalsIgnoreCase(url)|| url == null){
                continue;
            }
            encoding.mEncodingUrl = url== null ?null:Uri.parse(url);
            //check if the encoding is already added based on url
            if(getEncodingFromUrl(url)!= null){
                continue;
            }
            encoding.mMediaID = mediaId;
            if(!(encodingObject.get(Constants.GROUP).isJsonNull())){
                encoding.mGroup = encodingObject.get(Constants.GROUP).getAsString();
            }
            if(!(encodingObject.get(Constants.SIZE).isJsonNull())){
                encoding.mSizeInBytes = Integer.parseInt(encodingObject.get(Constants.SIZE).getAsString());
            }
            if(!(encodingObject.get(Constants.HEIGHT).isJsonNull())){
                encoding.mHeight = Integer.parseInt(encodingObject.get(Constants.HEIGHT).getAsString());
            }
            if(!(encodingObject.get(Constants.WIDTH).isJsonNull())){
                encoding.mWidth = Integer.parseInt(encodingObject.get(Constants.WIDTH).getAsString());
            }
            if(!(encodingObject.get(Constants.AUDIO_BITRATE).isJsonNull())){
                encoding.mAudioBitRate = Integer.parseInt(encodingObject.get(Constants.AUDIO_BITRATE).getAsString());
            }
            if(!(encodingObject.get(Constants.VIDEO_BITRATE).isJsonNull())){
                encoding.mVideoBitRate = Integer.parseInt(encodingObject.get(Constants.VIDEO_BITRATE).getAsString());
            }
            mEncodingList.add(encoding);
        }
    }

    /**
     * This method parses the error information from error stream of response.
     * @param reader
     * @return Error message
     */
    private String parseError(final JsonReader reader){
        final JsonParser parser = new JsonParser();
        final JsonObject object = parser.parse(reader).getAsJsonObject();
        if(!object.get("errors").isJsonNull()){
            return object.get("errors").getAsString();
        }
        return null;
    }

    /**
     * This method returns the encoding associated with encoding url.
     * @param encodingUrl
     * @return Encoding
     */
    Encoding getEncodingFromUrl(final String encodingUrl) {
        synchronized (mEncodingList) {
            for(Encoding encoding: mEncodingList ){
                if(encoding.mEncodingUrl.toString().equalsIgnoreCase(encodingUrl)){
                    return encoding;
                }
            }
            return null;
        }
    }

    /**
     * Method to set the paging parameters.
     * @param pageSize The number of results to return per page. The default and maximum page size is 500.Min is 50.
     * Example: page_size=100<br>
     * @param sortBy The field by which the results should be sorted. It has values like publish_date, create_date, update_date<br>
     * @param sortOrder The order in which the results should display. It has values like asc or  desc<br>
     */
    public void setPagingParameters(final int pageSize,final String sortBy, final String sortOrder){
        mLogger.debug(TAG + " setPagingParameters "+ " pageSize: " + pageSize +" sortBy: " +sortBy+ " sortOrder: " + sortOrder);
        if(pageSize>= 50 && pageSize <=500){
            mPageSize = pageSize;
        }
        if(sortBy!= null && sortBy.equalsIgnoreCase(mSortValues[0])||sortBy.equalsIgnoreCase(mSortValues[1])){
            mSortBy = sortBy;
        }
        if(sortOrder!= null && sortOrder.equalsIgnoreCase(mSortOrderValues[0])||sortOrder.equalsIgnoreCase(mSortOrderValues[1])){
            mSortOrder = sortOrder;
        }
    }

    /**
     * This method appends the paging parameters to the request URL.
     * @param url Request URL
     * @param params Paging Parameters
     * @return Paging Parameters appended URL
     * @throws UnsupportedEncodingException
     */
    private String appendPagingParameters(final String url,final Map<String, String> params) throws UnsupportedEncodingException{
        StringBuilder urlBuilder = new StringBuilder(url);
        urlBuilder = urlBuilder.append("?");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            urlBuilder.append(URLEncoder.encode(entry.getKey(),Constants.URL_CHARACTER_ENCODING_TYPE));
            urlBuilder = urlBuilder.append("=");
            urlBuilder.append(URLEncoder.encode(entry.getValue(),Constants.URL_CHARACTER_ENCODING_TYPE));
            urlBuilder = urlBuilder.append("&");
            urlBuilder.append(entry.getKey());
            urlBuilder = urlBuilder.append("=");
            urlBuilder.append(entry.getValue());
            urlBuilder = urlBuilder.append("&");
        }
        // Removing trailing "&"
        urlBuilder.deleteCharAt(urlBuilder.length() - 1);
        return urlBuilder.toString();
    }

    /**
     * Creates a delivery from an array of encodings.
     * It first creates a map of unique encodings URLs as key and the corresponding encodings list as value.
     * Then it creates a list of deliveries from this map.
     * Then it finds the suitable delivery from this list of deliveries.
     * This is based on the priority of primary use of the deliveries.
     * @param encodings
     * @return Delivery
     */
    Delivery getDeliveryForMedia(final ArrayList<Encoding> encodingList) {
        if(encodingList!= null && !encodingList.isEmpty()){
            final Map<Uri,ArrayList<Encoding>> encodingsMap = getEncodingsByUrl(encodingList);
            final ArrayList<Delivery> deliveryList =  new ArrayList<Delivery>();
            for(Uri uri:encodingsMap.keySet()){
                final ArrayList<Encoding> enc = encodingsMap.get(uri);
                Delivery delivery =  new Delivery();
                switch (enc.get(0).primaryUse) {
                case MobileH264:
                    delivery.mDeliveryType = DeliveryType.LVKDeliveryTypeMobileH264;
                    break;
                case Flash:
                    delivery.mDeliveryType = DeliveryType.LVKDeliveryTypeFlash;
                    break;
                case HttpLiveStreaming:
                    delivery.mDeliveryType = DeliveryType.LVKDeliveryTypeHTTPLiveStreaming;
                    break;
                case Widevine:
                    delivery.mProtected = true;
                    delivery.mDeliveryType = DeliveryType.LVKDeliveryTypeWidevine;
                    break;
                case WidevineOffline:
                    delivery.mDeliveryType = DeliveryType.LVKDeliveryTypeWidevineOffline;
                    delivery.mProtected = true;
                    delivery.mDownloadable = true;
                    break;
                default:
                    continue;
                }
                delivery.mRemoteURL = enc.get(0).mEncodingUrl;
                delivery.mMediaId = enc.get(0).mMediaID;
                delivery.mEncodings = encodingList;
                deliveryList.add(delivery);
            }
            return getSuitableDelivery(deliveryList);
        }else{
            mLogger.error(TAG + "getDeliveryForMedia : encodingList size is 0");
            return null;
        }
    }

    /**
     * This method creates a map of unique encodings URLs as key and the corresponding encodings list as value.
     * @param encodingList
     * @return Map of Encoding Uri and Encoding List
     */
    private Map<Uri,ArrayList<Encoding>> getEncodingsByUrl(final ArrayList<Encoding> encodingList){
        final Map<Uri,ArrayList<Encoding>> map = new HashMap<Uri, ArrayList<Encoding>>();
        for(Encoding encoding:encodingList){
            if(map.containsKey(encoding.mEncodingUrl)){
                final ArrayList<Encoding> enc = map.get(encoding.mEncodingUrl);
                enc.add(encoding);
                map.put(encoding.mEncodingUrl, enc);
            }else{
                final ArrayList<Encoding> enc = new ArrayList<Encoding>();
                enc.add(encoding);
                map.put(encoding.mEncodingUrl, enc);
                
            }
        }
        return map;
    }

    /**
     * This method has the logic to find the suitable delivery from a list of deliveries.
     * This is based on the priority of primary use of the deliveries.
     * @param deliveryList
     * @return Delivery
     */
    private Delivery getSuitableDelivery(final ArrayList<Delivery> deliveryList){
        DeliveryType cur = DeliveryType.LVKDeliveryTypeNone;
        Delivery delivery = null;
        for(Delivery del:deliveryList){
            switch(del.mDeliveryType){
            case LVKDeliveryTypeWidevineOffline:
                cur = DeliveryType.LVKDeliveryTypeWidevineOffline;
                delivery = del;
                break;
            case LVKDeliveryTypeWidevine:
                if(DeliveryType.LVKDeliveryTypeWidevine.ordinal() > cur.ordinal()){
                    cur = DeliveryType.LVKDeliveryTypeWidevine;
                    delivery = del;
                }
                break;
            case LVKDeliveryTypeHTTPLiveStreaming:
                if(DeliveryType.LVKDeliveryTypeHTTPLiveStreaming.ordinal() > cur.ordinal()){
                    cur = DeliveryType.LVKDeliveryTypeHTTPLiveStreaming;
                    delivery = del;
                }
                break;
            case LVKDeliveryTypeMobileH264:
                if(DeliveryType.LVKDeliveryTypeMobileH264.ordinal() > cur.ordinal()){
                    cur = DeliveryType.LVKDeliveryTypeMobileH264;
                    delivery = del;
                }
                break;
            case LVKDeliveryTypeFlash:
                if(DeliveryType.LVKDeliveryTypeFlash.ordinal() > cur.ordinal()){
                    cur = DeliveryType.LVKDeliveryTypeFlash;
                    delivery = del;
                }
                break;
            default:
                break;
            }
        }
        return delivery;
    }
}