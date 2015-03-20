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
 * @author kanchan
 */
public class ContentService {

    private static final String TAG = PlayerSupportFragment.class.getSimpleName();
    private static ArrayList<Channel> mChannelList = new ArrayList<Channel>();
    private static ArrayList<Channel> mChannelListOfGroup = new ArrayList<Channel>();
    private static ArrayList<ChannelGroup> mChannelGroupList = new ArrayList<ChannelGroup>();
    private static ArrayList<Media> mMediaList = new ArrayList<Media>();
    private static ArrayList<Media> mMediaListOfChannel = new ArrayList<Media>();
    private static ArrayList<Encoding> mEncodingList = new ArrayList<Encoding>();
    private static ArrayList<Media> mMediaSearchList = new ArrayList<Media>();

    private int mPageSize = 500;
    private String[] mSortByValidValues = {Constants.SORT_BY_UPDATE_DATE,Constants.SORT_BY_CREATE_DATE};
    private String[] mSortOrderValidValues = {Constants.SORT_ORDER_ASC,Constants.SORT_ORDER_DESC};
    private String mSortBy = "update_date";
    private String mSortOrder = "asc";

    private static int mPageIdGroup = 0;
    private static boolean mHasNextGroup = false;
    private static int mPageIdChannel = 0;
    private static boolean mHasNextChannel = false;
    private static int mPageIdChannelOfGroup = 0;
    private static boolean mHasNextChannelOfGroup = false;
    private static int mPageIdMedia = 0;
    private static boolean mHasNextMedia = false;
    private static int mPageIdMediaOfChannel = 0;
    private static boolean mHasNextMediaOfChannel = false;
    private static boolean mHasNextSearchMedia = false;
    private static int mPageIdSearchMedia = 0;

    private Logger mLogger=  null;
    private Context mContext = null;

    public ContentService(Context context){
        mContext = context;
        mLogger = LoggerUtil.getLogger(mContext,LoggerUtil.sLoggerName);
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
   public ArrayList<ChannelGroup> getAllChannelGroup(boolean isLoadMore) throws Exception{

       if (!Setting.isAccountConfigured(Setting.sOrgId, Setting.sAccessKey, Setting.sSecret)) {
           throw new Exception("Please Ensure Organization ID, Access key And Secret Are Set.");
       } else {
           if(!Connection.isConnected(mContext)){
               throw new Exception("Device Not Connected !");
           }else{
               Exception exception = null;
               HttpURLConnection urlConnection = null;
               try{
                   String resourceUrl = Setting.sApiEndPoint + String.format(Constants.FETCH_ALL_CHANNEL_GROUP_PATH, Setting.sOrgId);
                   mLogger.debug(TAG + " getAllChannelGroup "+" resourceUrl: "+resourceUrl + " isLoadMore: "+ isLoadMore);
                   mLogger.debug(TAG + " getAllChannelGroup "+ " mHasNextGroup: " + mHasNextGroup +" mPageIdGroup: " + mPageIdGroup);
                   if(isLoadMore){
                       if(!mHasNextGroup){
                          return mChannelGroupList;
                       }else {
                           mPageIdGroup++;
                       }
                   }else{
                       mChannelGroupList.clear();
                       mHasNextGroup = false;
                       mPageIdGroup = 0;
                   }
                   HashMap<String, String> params = new HashMap<String, String>();
                   params.put(Constants.PAGE_ID, ""+mPageIdGroup);
                   params.put("page_size", ""+mPageSize);
                   params.put("sort_by", mSortBy);
                   params.put("sort_order", mSortOrder);
                   mLogger.debug(TAG + " getAllChannelGroup "+ " mSortBy: "+ mSortBy + " mSortOrder: "+ mSortOrder + " mPageSize " + mPageSize);
                   String url = URLAuthenticator.authenticateRequest(Constants.GET,resourceUrl, Setting.sAccessKey, Setting.sSecret, params);
                   urlConnection = (HttpURLConnection) new URL(url).openConnection();
                   urlConnection.setConnectTimeout(Constants.PREPARING_TIMEOUT);
                   JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader(urlConnection.getInputStream())));
                   parseChannelGroups(reader);
               }
               catch (NoSuchAlgorithmException e) {
                   exception = new Exception("Signing Failed !");
               }
               catch (MalformedURLException e) {
                   exception =  new Exception("Invalid URL !");
               }
               catch(InvalidKeyException e){
                   exception = new Exception("Authentication Failed !");
               }
               catch(UnsupportedEncodingException e){
                   exception = new Exception("Authentication Failed !");
               }
               catch(URISyntaxException e){
                   exception = new Exception("Authentication Failed !");
               }
               catch(IOException e){
                   exception = new Exception("Connection Error !");
               }
               finally {
                   if (urlConnection != null) {
                       urlConnection.disconnect();
                   }
                   if(exception != null){
                       if(mPageIdGroup != 0){
                           mPageIdGroup--;
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
     * This is as async call and list of channel groups is returned in callback.
     * @param isLoadMore True if load more called else false.
     * @param callback ChannelGroupCallback
     */
    public void getAllChannelGroupAsync(boolean isLoadMore,final ChannelGroupCallback callback) {

        if (!Setting.isAccountConfigured(Setting.sOrgId, Setting.sAccessKey, Setting.sSecret)) {
            callback.onError(new Throwable("Please Ensure Organization ID, Access key And Secret Are Set."));
        } else {
            if(!Connection.isConnected(mContext)){
                callback.onError(new Throwable("Device Not Connected !"));
            }else{
                try {
                    String resourceUrl = Setting.sApiEndPoint + String.format(Constants.FETCH_ALL_CHANNEL_GROUP_PATH, Setting.sOrgId);
                    mLogger.debug(TAG + " getAllChannelGroupAsync "+" resourceUrl: "+resourceUrl + " isLoadMore: "+ isLoadMore);
                    mLogger.debug(TAG + " getAllChannelGroupAsync "+ " mHasNextGroup: " + mHasNextGroup +" mPageIdGroup: " + mPageIdGroup);
                    if(isLoadMore){
                        if(!mHasNextGroup){
                            callback.onSuccess(mChannelGroupList);
                            return;
                        }else {
                            mPageIdGroup++;
                        }
                    }else{
                        mChannelGroupList.clear();
                        mHasNextGroup = false;
                        mPageIdGroup = 0;
                    }
                    HashMap<String, String> params = new HashMap<String,String>();
                    params.put(Constants.PAGE_ID, ""+mPageIdGroup);
                    params.put("page_size", ""+mPageSize);
                    params.put("sort_by", mSortBy);
                    params.put("sort_order", mSortOrder);
                    mLogger.debug(TAG + " getAllChannelGroupAsync "+ " mSortBy: "+ mSortBy + " mSortOrder: "+ mSortOrder + " mPageSize " + mPageSize);
                    final String url = URLAuthenticator.authenticateRequest(Constants.GET,resourceUrl, Setting.sAccessKey, Setting.sSecret, params);
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            Throwable throwable = null;
                            HttpURLConnection urlConnection = null;
                            try {
                                urlConnection = (HttpURLConnection) new URL(url).openConnection();
                                urlConnection.setConnectTimeout(Constants.PREPARING_TIMEOUT);
                                JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader(urlConnection.getInputStream())));
                                parseChannelGroups(reader);
                                callback.onSuccess(mChannelGroupList);
                            }
                            catch (MalformedURLException e) {
                                throwable = new Throwable("Invalid Thumbnail URL !");
                            }
                            catch (IOException e) {
                                throwable = new Throwable("Connection Error !");
                            }
                            finally {
                                if (urlConnection != null) {
                                    urlConnection.disconnect();
                                }
                                if(throwable != null){
                                    if(mPageIdGroup != 0){
                                        mPageIdGroup--;
                                    }
                                    callback.onError(throwable);
                                }
                            }
                        }
                    };
                    Thread thread = new Thread(runnable);
                    thread.start();
                }
                catch (Exception e) {
                    callback.onError(new Throwable("Authentication Failed !"));
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
    public ArrayList<Channel> getAllChannelOfGroup(String channelGroupId,boolean isLoadMore) throws Exception{

        if (Setting.sOrgId == null || Setting.sOrgId.trim().length() == 0) {
            throw new Exception("Please Ensure Organization ID is Set.");
        } else {
            if(!Connection.isConnected(mContext)){
                throw new Exception("Device Not Connected !");
            }else{
                Exception exception = null;
                HttpURLConnection urlConnection = null;
                try {
                    String resourceUrl = Setting.sApiEndPoint + String.format(Constants.FETCH_ALL_CHANNEL_OF_GROUP_PATH, Setting.sOrgId,channelGroupId);
                    mLogger.debug(TAG + " getAllChannelOfGroup "+" resourceUrl: "+resourceUrl + " isLoadMore: "+ isLoadMore);
                    mLogger.debug(TAG + " getAllChannelOfGroup "+ " mHasNextChannelOfGroup: " + mHasNextChannelOfGroup +" mPageIdChannelOfGroup: " + mPageIdChannelOfGroup);
                    if(isLoadMore){
                        if(!mHasNextChannelOfGroup){
                           return mChannelListOfGroup;
                        }else {
                            mPageIdChannelOfGroup++;
                        }
                    }else{
                        mChannelListOfGroup.clear();
                        mHasNextChannelOfGroup = false;
                        mPageIdChannelOfGroup = 0;
                    }
                    HashMap<String, String> params = new HashMap<String, String>();
                    params.put(Constants.PAGE_ID, ""+mPageIdChannelOfGroup);
                    params.put("page_size", ""+mPageSize);
                    params.put("sort_by", mSortBy);
                    params.put("sort_order", mSortOrder);
                    mLogger.debug(TAG + " getAllChannelOfGroup "+ " mSortBy: "+ mSortBy + " mSortOrder: "+ mSortOrder + " mPageSize " + mPageSize);
                    resourceUrl = appendPagingParameters(resourceUrl, params);
                    urlConnection = (HttpURLConnection) new URL(resourceUrl).openConnection();
                    urlConnection.setConnectTimeout(Constants.PREPARING_TIMEOUT);
                    JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader(urlConnection.getInputStream())));
                    parseChannelOfGroup(reader);
                }
                catch (MalformedURLException e) {
                    exception = new Exception("Invalid Thumbnail URL !");
                }
                catch (UnsupportedEncodingException e) {
                    exception = new Exception("Failed To Append Paging Parameter !");
                }
                catch (IOException e) {
                    exception = new Exception("Connection Error !");
                }
                finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    if(exception != null){
                        if(mPageIdChannelOfGroup != 0){
                            mPageIdChannelOfGroup--;
                        }
                        throw exception;
                    }
                }
                return mChannelListOfGroup;
            }
        }
    }

    /**
     * This method will fetch all the channels of a particular channel group.
     * This method does not requires authentication.
     * This is as async call and list of channels is returned in callback.
     * @param channelGroupId ChannelGroupId
     * @param isLoadMore True if load more called else false.
     * @param callback ChannelCallback
     */
    public void getAllChannelOfGroupAsync(final String channelGroupId, final boolean isLoadMore,final ChannelCallback callback) {

        if (Setting.sOrgId == null || Setting.sOrgId.trim().length() == 0) {
            callback.onError(new Throwable("Please Ensure Organization ID is Set."));
        } else {
            if(!Connection.isConnected(mContext)){
                callback.onError(new Throwable("Device Not Connected !"));
            }else{
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        HttpURLConnection urlConnection = null;
                        Throwable throwable = null;
                        try {
                            String resourceUrl = Setting.sApiEndPoint + String.format(Constants.FETCH_ALL_CHANNEL_OF_GROUP_PATH, Setting.sOrgId,channelGroupId);
                            mLogger.debug(TAG + " getAllChannelOfGroupAsync "+" resourceUrl: "+resourceUrl + " isLoadMore: "+ isLoadMore);
                            mLogger.debug(TAG + " getAllChannelOfGroupAsync "+ " mHasNextChannelOfGroup: " + mHasNextChannelOfGroup +" mPageIdChannelOfGroup: " + mPageIdChannelOfGroup);
                            if(isLoadMore){
                                if(!mHasNextChannelOfGroup){
                                    callback.onSuccess(mChannelListOfGroup);
                                    return;
                                }else {
                                    mPageIdChannelOfGroup++;
                                }
                            }else{
                                mChannelListOfGroup.clear();
                                mHasNextChannelOfGroup = false;
                                mPageIdChannelOfGroup = 0;
                            }
                            HashMap<String, String> params = new HashMap<String, String>();
                            params.put(Constants.PAGE_ID, ""+mPageIdChannelOfGroup);
                            params.put("page_size", ""+mPageSize);
                            params.put("sort_by", mSortBy);
                            params.put("sort_order", mSortOrder);
                            mLogger.debug(TAG + " getAllChannelOfGroupAsync "+ " mSortBy: "+ mSortBy + " mSortOrder: "+ mSortOrder + " mPageSize " + mPageSize);
                            resourceUrl = appendPagingParameters(resourceUrl, params);
                            urlConnection = (HttpURLConnection) new URL(resourceUrl).openConnection();
                            urlConnection.setConnectTimeout(Constants.PREPARING_TIMEOUT);
                            JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader(urlConnection.getInputStream())));
                            parseChannelOfGroup(reader);
                            callback.onSuccess(mChannelListOfGroup);
                        }
                        catch (UnsupportedEncodingException e) {
                            throwable = new Throwable("Failed To Append Paging Parameter !");
                        }
                        catch (MalformedURLException e) {
                            throwable = new Throwable("Invalid Thumbnail URL !");
                        }
                        catch (IOException e) {
                            throwable = new Throwable("Connection Error !");
                        }
                        finally {
                            if (urlConnection != null) {
                                urlConnection.disconnect();
                            }
                            if(throwable != null){
                                if(mPageIdChannelOfGroup != 0){
                                    mPageIdChannelOfGroup--;
                                }
                                callback.onError(throwable);
                            }
                        }
                    }
                };
                Thread thread = new Thread(runnable);
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

        if (!Setting.isAccountConfigured(Setting.sOrgId, Setting.sAccessKey, Setting.sSecret)) {
            throw new Exception("Please Ensure Organization ID, Access key And Secret Are Set.");
        } else {
            if(!Connection.isConnected(mContext)){
                throw new Exception("Device Not Connected !");
            }else{
                Exception exception = null;
                HttpURLConnection urlConnection = null;
                try {
                    String resourceUrl = Setting.sApiEndPoint + String.format(Constants.FETCH_ALL_CHANNEL_PATH, Setting.sOrgId);
                    mLogger.debug(TAG + " getAllChannel "+" resourceUrl: "+resourceUrl + " isLoadMore: "+ isLoadMore);
                    mLogger.debug(TAG + " getAllChannel "+ " mHasNextChannel: " + mHasNextChannel +" mPageIdChannel: " + mPageIdChannel);
                    if(isLoadMore){
                        if(!mHasNextChannel){
                           return mChannelList;
                        }else {
                            mPageIdChannel++;
                        }
                    }else{
                        mChannelList.clear();
                        mHasNextChannel = false;
                        mPageIdChannel = 0;
                    }
                    HashMap<String, String> params = new HashMap<String, String>();
                    params.put(Constants.PAGE_ID, ""+mPageIdChannel);
                    params.put("page_size", ""+mPageSize);
                    params.put("sort_by", mSortBy);
                    params.put("sort_order", mSortOrder);
                    mLogger.debug(TAG + " getAllChannel "+ " mSortBy: "+ mSortBy + " mSortOrder: "+ mSortOrder + " mPageSize " + mPageSize);
                    String url = URLAuthenticator.authenticateRequest(Constants.GET,resourceUrl, Setting.sAccessKey, Setting.sSecret, params);
                    urlConnection = (HttpURLConnection) new URL(url).openConnection();
                    urlConnection.setConnectTimeout(Constants.PREPARING_TIMEOUT);
                    JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader(urlConnection.getInputStream())));
                    parseChannels(reader);
                }
                catch (MalformedURLException e) {
                    exception = new Exception("Invalid URL !");
                }
                catch(InvalidKeyException e){
                    exception = new Exception("Authentication Failed !");
                }
                catch(UnsupportedEncodingException e){
                    exception = new Exception("Authentication Failed !");
                }
                catch(URISyntaxException e){
                    exception = new Exception("Authentication Failed !");
                }
                catch(IOException e){
                    exception = new Exception("Connection Error !");
                }
                finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    if(exception != null){
                        if(mPageIdChannel != 0){
                            mPageIdChannel--;
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
     * This is as async call and list of channels is returned in callback.
     * @param isLoadMore True if load more called else false.
     * @param callback ChannelCallback
     */
    public void getAllChannelAsync(boolean isLoadMore,final ChannelCallback callback) {

        if (!Setting.isAccountConfigured(Setting.sOrgId, Setting.sAccessKey, Setting.sSecret)) {
            callback.onError(new Throwable("Please Ensure Organization ID, Access key And Secret Are Set."));
        } else {
            if(!Connection.isConnected(mContext)){
                callback.onError(new Throwable("Device Not Connected !"));
            }else{
                try {
                    String resourceUrl = Setting.sApiEndPoint + String.format(Constants.FETCH_ALL_CHANNEL_PATH, Setting.sOrgId);
                    mLogger.debug(TAG + " getAllChannelAsync "+" resourceUrl: "+resourceUrl + " isLoadMore: "+ isLoadMore);
                    mLogger.debug(TAG + " getAllChannelAsync "+ " mHasNextChannel: " + mHasNextChannel +" mPageIdChannel: " + mPageIdChannel);
                    if(isLoadMore){
                        if(!mHasNextChannel){
                            callback.onSuccess(mChannelList);
                            return;
                        }else {
                            mPageIdChannel++;
                        }
                    }else{
                        mChannelList.clear();
                        mHasNextChannel = false;
                        mPageIdChannel = 0;
                    }
                    HashMap<String, String> params = new HashMap<String, String>();
                    params.put(Constants.PAGE_ID, ""+mPageIdChannel);
                    params.put("page_size", ""+mPageSize);
                    params.put("sort_by", mSortBy);
                    params.put("sort_order", mSortOrder);
                    mLogger.debug(TAG + " getAllChannelAsync "+ " mSortBy: "+ mSortBy + " mSortOrder: "+ mSortOrder + " mPageSize " + mPageSize);
                    final String url = URLAuthenticator.authenticateRequest(Constants.GET,resourceUrl, Setting.sAccessKey, Setting.sSecret, params);
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            Throwable throwable = null;
                            HttpURLConnection urlConnection = null;
                            try {
                                urlConnection = (HttpURLConnection) new URL(url).openConnection();
                                urlConnection.setConnectTimeout(Constants.PREPARING_TIMEOUT);
                                JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader(urlConnection.getInputStream())));
                                parseChannels(reader);
                                callback.onSuccess(mChannelList);
                            }
                            catch (MalformedURLException e) {
                                throwable = new Throwable("Invalid Thumbnail URL !");
                            }
                            catch (IOException e) {
                                throwable = new Throwable("Connection Error !");
                            }
                            finally {
                                if (urlConnection != null) {
                                    urlConnection.disconnect();
                                }
                                if(throwable != null){
                                    if(mPageIdChannel != 0){
                                        mPageIdChannel--;
                                    }
                                    callback.onError(throwable);
                                }
                            }
                        }
                    };
                    Thread thread = new Thread(runnable);
                    thread.start();
                }
                catch (Exception e) {
                    callback.onError(new Throwable("Authentication Failed !"));
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

        if (Setting.sOrgId == null || Setting.sOrgId.trim().length() == 0) {
            throw new Exception("Please Ensure Organization ID is Set.");
        } else {
            if(!Connection.isConnected(mContext)){
                throw new Exception("Device Not Connected !");
            }else{
                Exception exception = null;
                HttpURLConnection urlConnection = null;
                try {
                    String resourceUrl = Setting.sApiEndPoint + String.format(Constants.FETCH_ALL_MEDIA_OF_CHANNEL_PATH, Setting.sOrgId,channelId);
                    mLogger.debug(TAG + " getAllMediaOfChannel "+" resourceUrl: "+resourceUrl + " isLoadMore: "+ isLoadMore);
                    mLogger.debug(TAG + " getAllMediaOfChannel "+ " mHasNextMediaOfChannel: " + mHasNextMediaOfChannel +" mPageIdMediaOfChannel: " + mPageIdMediaOfChannel);
                    if(isLoadMore){
                        if(!mHasNextMediaOfChannel){
                           return mMediaListOfChannel;
                        }else {
                            mPageIdMediaOfChannel++;
                        }
                    }else{
                        mMediaListOfChannel.clear();
                        mHasNextMediaOfChannel = false;
                        mPageIdMediaOfChannel = 0;
                    }
                    HashMap<String, String> params = new HashMap<String, String>();
                    params.put(Constants.PAGE_ID, ""+mPageIdMediaOfChannel);
                    params.put("page_size", ""+mPageSize);
                    params.put("sort_by", mSortBy);
                    params.put("sort_order", mSortOrder);
                    mLogger.debug(TAG + " getAllMediaOfChannel "+ " mSortBy: "+ mSortBy + " mSortOrder: "+ mSortOrder + " mPageSize " + mPageSize);
                    resourceUrl = appendPagingParameters(resourceUrl, params);
                    urlConnection = (HttpURLConnection) new URL(resourceUrl).openConnection();
                    urlConnection.setConnectTimeout(Constants.PREPARING_TIMEOUT);
                    JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader(urlConnection.getInputStream())));
                    parseMediaOfChannel(reader);
                }
                catch (UnsupportedEncodingException e) {
                    exception = new Exception("Failed To Append Paging Parameter !");
                }
                catch (MalformedURLException e) {
                    exception = new Exception("Invalid Thumbnail URL !");
                }
                catch (IOException e) {
                    exception = new Exception("Connection Error !");
                }
                finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    if(exception != null){
                        if(mPageIdMediaOfChannel != 0){
                            mPageIdMediaOfChannel--;
                        }
                        throw exception;
                    }
                }
                return mMediaListOfChannel;
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

        if (Setting.sOrgId == null || Setting.sOrgId.trim().length() == 0) {
            callback.onError(new Throwable("Please Ensure Organization ID is Set."));
        } else {
            if(!Connection.isConnected(mContext)){
                callback.onError(new Throwable("Device Not Connected !"));
            }else{
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        Throwable throwable = null;
                        HttpURLConnection urlConnection = null;
                        try {
                            String resourceUrl = Setting.sApiEndPoint + String.format(Constants.FETCH_ALL_MEDIA_OF_CHANNEL_PATH, Setting.sOrgId,channelId);
                            mLogger.debug(TAG + " getAllMediaOfChannelAsync "+" resourceUrl: "+resourceUrl + " isLoadMore: "+ isLoadMore);
                            mLogger.debug(TAG + " getAllMediaOfChannelAsync "+ " mHasNextMediaOfChannel: " + mHasNextMediaOfChannel +" mPageIdMediaOfChannel: " + mPageIdMediaOfChannel);
                            if(isLoadMore){
                                if(!mHasNextMediaOfChannel){
                                    callback.onSuccess(mMediaListOfChannel);
                                    return;
                                }else {
                                    mPageIdMediaOfChannel++;
                                }
                            }else{
                                mMediaListOfChannel.clear();
                                mHasNextMediaOfChannel = false;
                                mPageIdMediaOfChannel = 0;
                            }
                            HashMap<String, String> params = new HashMap<String, String>();
                            params.put(Constants.PAGE_ID, ""+mPageIdMediaOfChannel);
                            params.put("page_size", ""+mPageSize);
                            params.put("sort_by", mSortBy);
                            params.put("sort_order", mSortOrder);
                            mLogger.debug(TAG + " getAllMediaOfChannelAsync "+ " mSortBy: "+ mSortBy + " mSortOrder: "+ mSortOrder + " mPageSize " + mPageSize);
                            resourceUrl = appendPagingParameters(resourceUrl, params);
                            urlConnection = (HttpURLConnection) new URL(resourceUrl).openConnection();
                            urlConnection.setConnectTimeout(Constants.PREPARING_TIMEOUT);
                            JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader(urlConnection.getInputStream())));
                            parseMediaOfChannel(reader);
                            callback.onSuccess(mMediaListOfChannel);
                        }
                        catch (UnsupportedEncodingException e) {
                            throwable = new Throwable("Failed To Append Paging Parameter !");
                        }
                        catch (MalformedURLException e) {
                            throwable = new Throwable("Invalid Thumbnail URL !");
                        }
                        catch (IOException e) {
                            throwable = new Throwable("Connection Error !");
                        }
                        finally {
                            if (urlConnection != null) {
                                urlConnection.disconnect();
                            }
                            if(throwable != null){
                                if(mPageIdMediaOfChannel != 0){
                                    mPageIdMediaOfChannel--;
                                }
                                callback.onError(throwable);
                            }
                        }
                    }
                };
                Thread thread = new Thread(runnable);
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

        if (!Setting.isAccountConfigured(Setting.sOrgId, Setting.sAccessKey, Setting.sSecret)) {
            throw new Exception("Please Ensure Organization ID, Access key And Secret Are Set.");
        } else {
            if(!Connection.isConnected(mContext)){
                throw new Exception("Device Not Connected !");
            }else{
                Exception exception = null;
                HttpURLConnection urlConnection = null;
                try {
                    String resourceUrl = Setting.sApiEndPoint + String.format(Constants.SEARCH_ALL_MEDIA_PATH, Setting.sOrgId);
                    mLogger.debug(TAG + " getAllMedia "+" resourceUrl: "+resourceUrl + " isLoadMore: "+ isLoadMore);
                    mLogger.debug(TAG + " getAllMedia "+ " mHasNextMediaOfChannel: " + mHasNextMedia +" mPageIdMedia: " + mPageIdMedia);
                    if(isLoadMore){
                        if(!mHasNextMedia){
                           return mMediaList;
                        }else {
                            mPageIdMedia++;
                        }
                    }else{
                        mMediaList.clear();
                        mHasNextMedia = false;
                        mPageIdMedia = 0;
                    }
                    HashMap<String, String> params = new HashMap<String, String>();
                    params.put(Constants.PAGE_ID, ""+mPageIdMedia);
                    params.put("page_size", ""+mPageSize);
                    params.put("sort_by", mSortBy);
                    params.put("sort_order", mSortOrder);
                    mLogger.debug(TAG + " getAllMedia "+ " mSortBy: "+ mSortBy + " mSortOrder: "+ mSortOrder + " mPageSize " + mPageSize);
                    String url = URLAuthenticator.authenticateRequest(Constants.GET,resourceUrl, Setting.sAccessKey, Setting.sSecret, params);
                    urlConnection = (HttpURLConnection) new URL(url).openConnection();
                    urlConnection.setConnectTimeout(Constants.PREPARING_TIMEOUT);
                    JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader(urlConnection.getInputStream())));
                    parseMedias(reader);
                }catch (MalformedURLException e) {
                    exception = new Exception("Invalid URL !");
                }
                catch(InvalidKeyException e){
                    exception = new Exception("Authentication Failed !");
                }
                catch(UnsupportedEncodingException e){
                    exception = new Exception("Authentication Failed !");
                }
                catch(URISyntaxException e){
                    exception = new Exception("Authentication Failed !");
                }
                catch(IOException e){
                    exception = new Exception("Connection Error !");
                }
                finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    if(exception != null){
                        if(mPageIdMedia != 0){
                            mPageIdMedia--;
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
     * This is as async call and list of media is returned in callback.
     * @param isLoadMore True if load more called else false.
     * @param callback MediaCallback
     */
    public void getAllMediaAsync(boolean isLoadMore,final MediaCallback callback) {

        if (!Setting.isAccountConfigured(Setting.sOrgId, Setting.sAccessKey, Setting.sSecret)) {
            callback.onError(new Throwable("Please Ensure Organization ID, Access key And Secret Are Set."));
        } else {
            if(!Connection.isConnected(mContext)){
                callback.onError(new Throwable("Device Not Connected !"));
            }else{
                try {
                    String resourceUrl = Setting.sApiEndPoint + String.format(Constants.SEARCH_ALL_MEDIA_PATH, Setting.sOrgId);
                    mLogger.debug(TAG + " getAllMediaAsync "+" resourceUrl: "+resourceUrl + " isLoadMore: "+ isLoadMore);
                    mLogger.debug(TAG + " getAllMediaAsync "+ " mHasNextMediaOfChannel: " + mHasNextMedia +" mPageIdMedia: " + mPageIdMedia);
                    if(isLoadMore){
                        if(!mHasNextMedia){
                            callback.onSuccess(mMediaList);
                            return;
                        }else {
                            mPageIdMedia++;
                        }
                    }else{
                        mMediaList.clear();
                        mHasNextMedia = false;
                        mPageIdMedia = 0;
                    }
                    HashMap<String, String> params = new HashMap<String, String>();
                    params.put(Constants.PAGE_ID, ""+mPageIdMedia);
                    params.put("page_size", ""+mPageSize);
                    params.put("sort_by", mSortBy);
                    params.put("sort_order", mSortOrder);
                    mLogger.debug(TAG + " getAllMediaAsync "+ " mSortBy: "+ mSortBy + " mSortOrder: "+ mSortOrder + " mPageSize " + mPageSize);
                    final String url = URLAuthenticator.authenticateRequest(Constants.GET,resourceUrl, Setting.sAccessKey, Setting.sSecret, params);
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            Throwable throwable = null;
                            HttpURLConnection urlConnection = null;
                            try {
                                urlConnection = (HttpURLConnection) new URL(url).openConnection();
                                urlConnection.setConnectTimeout(Constants.PREPARING_TIMEOUT);
                                JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader(urlConnection.getInputStream())));
                                parseMedias(reader);
                                callback.onSuccess(mMediaList);
                            }
                            catch (MalformedURLException e1) {
                                throwable = new Throwable("Invalid Thumbnail URL !");
                            }
                            catch (IOException e) {
                                throwable = new Throwable("Connection Error !");
                            }
                            finally {
                                if (urlConnection != null) {
                                    urlConnection.disconnect();
                                }
                                if(throwable != null){
                                    if(mPageIdMedia != 0){
                                        mPageIdMedia--;
                                    }
                                    callback.onError(throwable);
                                }
                            }
                        }
                    };
                    Thread thread = new Thread(runnable);
                    thread.start();
                } 
                catch (Exception e) {
                    callback.onError(new Throwable("Authentication Failed !"));
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
     * @param tag Tag of Media - Optional
     * @param state state of Media(published/unpublished) - Optional
     * @param mediaType Type of media {@link MediaType}- Optional
     * @param channelId channel id for the medias - Optional
     * @throws Exception
     */
    public ArrayList<Media> searchMedia(boolean isLoadMore,String operator,String title,
            String description,String tag,String state,String mediaType,String channelId) throws Exception{

        if (!Setting.isAccountConfigured(Setting.sOrgId, Setting.sAccessKey, Setting.sSecret)) {
            throw new Exception("Please Ensure Organization ID, Access key And Secret Are Set.");
        } else {
            if(!Connection.isConnected(mContext)){
                throw new Exception("Device Not Connected !");
            }else{
                Exception exception = null;
                HttpURLConnection urlConnection = null;
                mLogger.debug(TAG + " searchMedia "+ " mHasNextSearchMedia: " + mHasNextSearchMedia +" mPageIdSearchMedia: " + mPageIdSearchMedia);
                try {
                    if(isLoadMore){
                        if(!mHasNextSearchMedia){
                             return mMediaSearchList;
                        }else {
                            mPageIdSearchMedia++;
                        }
                    }else{
                        mMediaSearchList.clear();
                        mHasNextSearchMedia = false;
                        mPageIdSearchMedia = 0;
                    }
                    StringBuilder searchStr = new StringBuilder();
                    if(title!= null)
                        searchStr.append(String.format("%s:%s;", Constants.TITLE,title));
                    if(description!= null)
                        searchStr.append(String.format("%s:%s;", Constants.DESCRIPTION,description));
                    if(tag!= null)
                        searchStr.append(String.format("%s:%s;", Constants.TAG,tag));
                    if(state!= null)
                        searchStr.append(String.format("%s:%s;", Constants.STATE,state));
                    if(mediaType!= null)
                        searchStr.append(String.format("%s:%s;", Constants.MEDIA_TYPE,mediaType));
                    if(channelId!= null)
                        searchStr.append(String.format("%s:%s;", Constants.CHANNEL_ID,channelId));
                    if(searchStr!= null)
                        searchStr.deleteCharAt(searchStr.length()-1);
                    HashMap<String, String> params = new HashMap<String, String>();
                    params.put(Constants.PAGE_ID, ""+mPageIdSearchMedia);
                    params.put("page_size", ""+mPageSize);
                    params.put("sort_by", mSortBy);
                    params.put("sort_order", mSortOrder);
                    params.put(operator, searchStr.toString());
                    mLogger.debug(TAG +" searchMedia "+ " search String: "+ searchStr);
                    String resourceUrl = Setting.sApiEndPoint + String.format(Constants.SEARCH_ALL_MEDIA_PATH, Setting.sOrgId);
                    mLogger.debug(TAG + " searchMedia "+" resourceUrl: "+resourceUrl + " isLoadMore: "+ isLoadMore);
                    mLogger.debug(TAG + " searchMedia "+ " mSortBy: "+ mSortBy + " mSortOrder: "+ mSortOrder + " mPageSize " + mPageSize);
                    String url = URLAuthenticator.authenticateRequest(Constants.GET,resourceUrl, Setting.sAccessKey, Setting.sSecret, params);
                    urlConnection = (HttpURLConnection) new URL(url).openConnection();
                    urlConnection.setConnectTimeout(Constants.PREPARING_TIMEOUT);
                    JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader(urlConnection.getInputStream())));
                    parseSearchMedias(reader);
                }catch (MalformedURLException e) {
                    exception = new Exception("Invalid URL !");
                }
                catch(InvalidKeyException e){
                    exception = new Exception("Authentication Failed !");
                }
                catch(UnsupportedEncodingException e){
                    exception = new Exception("Authentication Failed !");
                }
                catch(URISyntaxException e){
                    exception = new Exception("Authentication Failed !");
                }
                catch(IOException e){
                    exception = new Exception("Connection Error !");
                }
                finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    if(exception != null){
                        if(mPageIdSearchMedia != 0){
                            mPageIdSearchMedia--;
                        }
                        throw exception;
                    }
                }
                return mMediaSearchList;
            }
        }
    }

    /**
     * This method searches for media on server as per the search parameters supplied.
     * This is as async call and list of media is returned in callback.
     * @param isLoadMore true if load next page
     * @param operator And / OR - Optional
     * @param title Title of Media - Optional
     * @param description Description of Media - Optional
     * @param tag Tag of Media - Optional
     * @param state state of Media(published/unpublished) - Optional
     * @param mediaType Type of media {@link MediaType}- Optional
     * @param channelId channel id for the medias - Optional
     * @param callback MediaCallback
     * @throws Exception
     */
    public void searchMediaAsync(boolean isLoadMore,String operator,String title,String description,String tag,String state,String mediaType,String channelId,final MediaCallback callback) {

        if (!Setting.isAccountConfigured(Setting.sOrgId, Setting.sAccessKey, Setting.sSecret)) {
            callback.onError(new Throwable("Please Ensure Organization ID, Access key And Secret Are Set."));
        } else {
            if(!Connection.isConnected(mContext)){
                callback.onError(new Throwable("Device Not Connected !"));
            }else{
                mLogger.debug(TAG + " searchMediaAsync "+ " mHasNextSearchMedia: " + mHasNextSearchMedia +" mPageIdSearchMedia: " + mPageIdSearchMedia);
                if(isLoadMore){
                    if(!mHasNextSearchMedia){
                        callback.onSuccess(mMediaSearchList);
                        return;
                    }else {
                        mPageIdSearchMedia++;
                    }
                }else{
                    mMediaSearchList.clear();
                    mHasNextSearchMedia = false;
                    mPageIdSearchMedia = 0;
                }
                StringBuilder searchStr = new StringBuilder();
                if(title!= null)
                    searchStr.append(String.format("%s:%s;", Constants.TITLE,title));
                if(description!= null)
                    searchStr.append(String.format("%s:%s;", Constants.DESCRIPTION,description));
                if(tag!= null)
                    searchStr.append(String.format("%s:%s;", "tag",tag));
                if(title!= null)
                    searchStr.append(String.format("%s:%s;", "state",state));
                if(mediaType!= null)
                    searchStr.append(String.format("%s:%s;", Constants.MEDIA_TYPE,mediaType));
                if(channelId!= null)
                    searchStr.append(String.format("%s:%s;", Constants.CHANNEL_ID,channelId));
                if(searchStr!= null)
                    searchStr.deleteCharAt(searchStr.length()-1);
                HashMap<String, String> params = new HashMap<String, String>();
                params.put(Constants.PAGE_ID, ""+mPageIdSearchMedia);
                params.put("page_size", ""+mPageSize);
                params.put("sort_by", mSortBy);
                params.put("sort_order", mSortOrder);
                params.put(operator, searchStr.toString());
                mLogger.debug(TAG +" searchMediaAsync "+ " search String: "+ searchStr);
                mLogger.debug(TAG +" searchMediaAsync "+ " mSortBy: "+ mSortBy + " mSortOrder: "+ mSortOrder + " mPageSize " + mPageSize);
                final String resourceUrl = Setting.sApiEndPoint + String.format(Constants.SEARCH_ALL_MEDIA_PATH, Setting.sOrgId);
                mLogger.debug(TAG + " searchMediaAsync "+" resourceUrl: "+resourceUrl + " isLoadMore: "+ isLoadMore);
                try {
                    final String url = URLAuthenticator.authenticateRequest(Constants.GET,resourceUrl, Setting.sAccessKey, Setting.sSecret, params);
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            Throwable throwable = null;
                            HttpURLConnection urlConnection = null;
                            try {
                                urlConnection = (HttpURLConnection) new URL(url).openConnection();
                                urlConnection.setConnectTimeout(Constants.PREPARING_TIMEOUT);
                                JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader(urlConnection.getInputStream())));
                                parseSearchMedias(reader);
                                callback.onSuccess(mMediaSearchList);
                            }
                            catch (MalformedURLException e1) {
                                throwable = new Throwable("Invalid Request URL !");
                            }
                            catch (IOException e) {
                                throwable = new Throwable("Connection Error !");
                            }
                            finally {
                                if (urlConnection != null) {
                                    urlConnection.disconnect();
                                }
                                if(throwable != null){
                                    if(mPageIdSearchMedia != 0){
                                        mPageIdSearchMedia--;
                                    }
                                    callback.onError(throwable);
                                }
                            }
                        }
                    };
                    Thread thread = new Thread(runnable);
                    thread.start();
                }
                catch (Exception e) {
                    callback.onError(new Throwable("Authentication Failed !"));
                }
            }
        }
    }

    /**
     * This method fetches the properties of a specific channel.
     * This is as async call and channel list with a single channel is returned in callback.
     * @param channelId ChannelId
     * @param callback ChannelCallback
     */
    public void getChannelAsync(final String channelId,final ChannelCallback callback) {
        if (Setting.sOrgId == null || Setting.sOrgId.trim().length() == 0) {
            callback.onError(new Throwable("Please Ensure Organization ID is Set."));
        } else {
            if(!Connection.isConnected(mContext)){
                callback.onError(new Throwable("Device Not Connected !"));
            }else{
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        HttpURLConnection urlConnection = null;
                        try {
                            String resourceUrl = Setting.sApiEndPoint + String.format(Constants.FETCH_CHANNEL_PROPERTY_PATH, Setting.sOrgId,channelId);
                            mLogger.debug(TAG + " getChannelAsync " + " resourceUrl "+ resourceUrl);
                            urlConnection = (HttpURLConnection) new URL(resourceUrl).openConnection();
                            urlConnection.setConnectTimeout(Constants.PREPARING_TIMEOUT);
                            JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader(urlConnection.getInputStream())));
                            ArrayList<Channel> channelList = new ArrayList<Channel>();
                            channelList.add(parseChannelProperty(reader));
                            callback.onSuccess(channelList);
                        }
                        catch (MalformedURLException e) {
                            callback.onError(new Throwable("Invalid Request URL !"));
                        } 
                        catch (IOException e) {
                            callback.onError(new Throwable("Connection Error !"));
                        }
                        finally {
                            if (urlConnection != null) {
                                urlConnection.disconnect();
                            }
                        }
                    }
                };
                Thread thread = new Thread(runnable);
                thread.start();
            }
        }
    }

    /**
     * To fetch the properties of a specific media.
     * It does not require authentication.
     * This is as async call and list of a single media is returned in callback.
     * @param mediaId mediaId
     * @param callback MediaCallback
     */
    public void getMediaAsync(final String mediaId,final MediaCallback callback) {

        if (Setting.sOrgId == null || Setting.sOrgId.trim().length() == 0) {
            callback.onError(new Throwable("Please Ensure Organization ID is Set."));
        } else {
            if(!Connection.isConnected(mContext)){
                callback.onError(new Throwable("Device Not Connected !"));
            }else{
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        HttpURLConnection urlConnection = null;
                        try {
                            String resourceUrl = Setting.sApiEndPoint + String.format(Constants.FETCH_MEDIA_PROPERTY_PATH, Setting.sOrgId,mediaId);
                            mLogger.debug(TAG + " getMediaAsync " + " resourceUrl "+ resourceUrl);
                            urlConnection = (HttpURLConnection) new URL(resourceUrl).openConnection();
                            urlConnection.setConnectTimeout(Constants.PREPARING_TIMEOUT);
                            JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader(urlConnection.getInputStream())));
                            ArrayList<Media> mediaList = new ArrayList<Media>();
                            mediaList.add(parseMediaProperty(reader));
                            callback.onSuccess(mediaList);
                        }
                        catch (MalformedURLException e1) {
                            callback.onError(new Throwable("Invalid Thumbnail URL !"));
                        }
                        catch (IOException e) {
                            callback.onError(new Throwable("Connection Error !"));
                        }
                        finally {
                            if (urlConnection != null) {
                                urlConnection.disconnect();
                            }
                        }
                    }
                };
                Thread thread = new Thread(runnable);
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
        HashMap<String, String> params = new HashMap<String, String>();
        params.put(Constants.PRIMARY_USE, "all");
        if (!Setting.isAccountConfigured(Setting.sOrgId, Setting.sAccessKey, Setting.sSecret)) {
            callback.onError(new Throwable("Please Ensure Organization ID, Access key And Secret Are Set."));
        } else {
            if(!Connection.isConnected(mContext)){
                callback.onError(new Throwable("Device Not Connected"));
            }else {
                try {
                    if(mediaId != null && mediaId.trim().length() == 0){
                        callback.onError(new Throwable("Invalid Media Id"));
                        return;
                    }
                    String resourceUrl = Setting.sApiEndPoint + String.format(Constants.ENCODING_PATH, Setting.sOrgId, mediaId);
                    mLogger.debug(TAG + " getAllEncodingsForMediaId " + " resourceUrl "+ resourceUrl);
                    final String url = URLAuthenticator.authenticateRequest(Constants.GET,resourceUrl, Setting.sAccessKey, Setting.sSecret, params);
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            HttpURLConnection urlConnection = null;
                            try {
                                urlConnection = (HttpURLConnection) new URL(url).openConnection();
                                urlConnection.setConnectTimeout(Constants.PREPARING_TIMEOUT);
                                InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                                JsonReader reader = new JsonReader(new InputStreamReader(inputStream));
                                parseEncodings(reader,mediaId);
                                callback.onSuccess(mEncodingList);
                            }
                            catch(JSONException e){
                                callback.onError(new Throwable("JSON Error !"));
                            }
                            catch (MalformedURLException e) {
                                callback.onError(new Throwable("MalformedURLException Error !"));
                            }
                            catch(FileNotFoundException e){
                                callback.onError(new Throwable("Invalid Media Id !"));
                            }
                            catch (IOException e) {
                                callback.onError(new Throwable("Connection Error !"));
                            }
                            finally {
                                if (urlConnection != null) {
                                    urlConnection.disconnect();
                                }
                            }
                        }
                    };
                    Thread thread = new Thread(runnable);
                    thread.start();

                }catch (Exception e) {
                    callback.onError(new Throwable("Authentication Failed !"));
                }
            }
        }
    }

    /**
     * This method parses channel groups from JSON data.
     * @param channelGroups channel group json array
     */
    private void parseChannelGroups(JsonReader reader){
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Date.class, new DateDeserializer());
        builder.registerTypeAdapter(Uri.class, new UriDeserializer());
        Gson gson = builder.create();
        JsonParser parser = new JsonParser();
        JsonObject channelGroupsObject = parser.parse(reader).getAsJsonObject();
        if(channelGroupsObject.isJsonObject()){
            mHasNextGroup = channelGroupsObject.get("has_next").getAsBoolean();
            JsonArray channelGroupList = channelGroupsObject.get(Constants.CHANNEL_GROUPS).getAsJsonArray();
            if(channelGroupList.isJsonArray()){
                for(int i=0; i< channelGroupList.size(); i++){
                    mChannelGroupList.add(gson.fromJson(channelGroupList.get(i),  ChannelGroup.class));
                }
            }
        }
    }

    /**
     * This method parses channels of a particular group from JSON data.
     * @param channels channels json array
     */
    private void parseChannelOfGroup(JsonReader reader){
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Date.class, new DateDeserializer());
        builder.registerTypeAdapter(Uri.class, new UriDeserializer());
        Gson gson = builder.create();
        JsonParser parser = new JsonParser();
        JsonObject channelsObject = parser.parse(reader).getAsJsonObject();
        if(channelsObject.isJsonObject()){
            mHasNextChannelOfGroup = channelsObject.get("has_next").getAsBoolean();
            JsonArray channelsList = channelsObject.get(Constants.CHANNELS).getAsJsonArray();
            if(channelsList.isJsonArray()){
                for(int i=0; i< channelsList.size(); i++){
                    mChannelListOfGroup.add(gson.fromJson(channelsList.get(i),  Channel.class));
                }
            }
        }
    }

    /**
     * This method parses channels from JSON data.
     * @param channels channels json array
     * @throws JSONException
     */
    private void parseChannels(JsonReader reader){
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Date.class, new DateDeserializer());
        builder.registerTypeAdapter(Uri.class, new UriDeserializer());
        Gson gson = builder.create();
        JsonParser parser = new JsonParser();
        JsonObject channelsObject = parser.parse(reader).getAsJsonObject();
        if(channelsObject.isJsonObject()){
            mHasNextChannel = channelsObject.get("has_next").getAsBoolean();
            JsonArray channelsList = channelsObject.get(Constants.CHANNELS).getAsJsonArray();
            if(channelsList.isJsonArray()){
                for(int i=0; i< channelsList.size(); i++){
                    mChannelList.add(gson.fromJson(channelsList.get(i),  Channel.class));
                }
            }
        }
    }

    /**
     * This method parses media of a particular channel from JSON data.
     * @param medias json array
     * @throws JSONException
     * @throws URISyntaxException
     */
    private void parseMediaOfChannel(JsonReader reader){
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Date.class, new DateDeserializer());
        builder.registerTypeAdapter(Time.class, new TimeDeserializer());
        builder.registerTypeAdapter(Uri.class, new UriDeserializer());
        builder.registerTypeAdapter(MediaThumbnail.class, new ThumbnailDeserializer());
        Gson gson = builder.create();
        JsonParser parser = new JsonParser();
        JsonObject mediasObject = parser.parse(reader).getAsJsonObject();
        if(mediasObject.isJsonObject()){
            mHasNextMediaOfChannel = mediasObject.get("has_next").getAsBoolean();
            JsonArray mediasList = mediasObject.get(Constants.MEDIAS).getAsJsonArray();
            if(mediasList.isJsonArray()){
                for(int i=0; i< mediasList.size(); i++){
                    mMediaListOfChannel.add(gson.fromJson(mediasList.get(i),  Media.class));
                }
            }
        }
    }

    /**
     * This method parses channels properties from JSON data.
     * @param channels channels json array
     * @throws JSONException
     */
    private Channel parseChannelProperty(JsonReader reader){
        Channel channel = new Channel();
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Date.class, new DateDeserializer());
        builder.registerTypeAdapter(Uri.class, new UriDeserializer());
        Gson gson = builder.create();
        JsonParser parser = new JsonParser();
        JsonObject channelsObject = parser.parse(reader).getAsJsonObject();
        if(channelsObject.isJsonObject()){
            channel = gson.fromJson(channelsObject,  Channel.class);
        }
        return channel;
    }

    /**
     * This method parses media properties from JSON data.
     * @param medias json array
     */
    private Media parseMediaProperty(JsonReader reader){
        Media media = new Media();
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Date.class, new DateDeserializer());
        builder.registerTypeAdapter(Time.class, new TimeDeserializer());
        builder.registerTypeAdapter(Uri.class, new UriDeserializer());
        builder.registerTypeAdapter(MediaThumbnail.class, new ThumbnailDeserializer());
        Gson gson = builder.create();
        JsonParser parser = new JsonParser();
        JsonObject mediasObject = parser.parse(reader).getAsJsonObject();
        if(mediasObject.isJsonObject()){
            media = gson.fromJson(mediasObject,  Media.class);
        }
        return media;
    }

    /**
     * This method parses media from JSON data.
     * @param medias json array
     */
    private void parseMedias(JsonReader reader){
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Date.class, new DateDeserializer());
        builder.registerTypeAdapter(Time.class, new TimeDeserializer());
        builder.registerTypeAdapter(Uri.class, new UriDeserializer());
        builder.registerTypeAdapter(MediaThumbnail.class, new ThumbnailDeserializer());
        Gson gson = builder.create();
        JsonParser parser = new JsonParser();
        JsonObject mediasObject = parser.parse(reader).getAsJsonObject();
        if(mediasObject.isJsonObject()){
            mHasNextMedia = mediasObject.get("has_next").getAsBoolean();
            JsonArray mediasList = mediasObject.get(Constants.MEDIAS).getAsJsonArray();
            if(mediasList.isJsonArray()){
                for(int i=0; i< mediasList.size(); i++){
                    mMediaList.add(gson.fromJson(mediasList.get(i),  Media.class));
                }
            }
        }
    }

    /**
     * This method parses media from JSON data.
     * @param medias json array
     */
    private void parseSearchMedias(JsonReader reader){
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Date.class, new DateDeserializer());
        builder.registerTypeAdapter(Time.class, new TimeDeserializer());
        builder.registerTypeAdapter(Uri.class, new UriDeserializer());
        builder.registerTypeAdapter(MediaThumbnail.class, new ThumbnailDeserializer());
        Gson gson = builder.create();
        JsonParser parser = new JsonParser();
        JsonObject mediasObject = parser.parse(reader).getAsJsonObject();
        if(mediasObject.isJsonObject()){
            mHasNextSearchMedia = mediasObject.get("has_next").getAsBoolean();
            JsonArray mediasList = mediasObject.get(Constants.MEDIAS).getAsJsonArray();
            if(mediasList.isJsonArray()){
                for(int i=0; i< mediasList.size(); i++){
                    mMediaSearchList.add(gson.fromJson(mediasList.get(i),  Media.class));
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
    private void parseEncodings(JsonReader reader, String mediaId) throws JSONException{
        mEncodingList.clear();
        JsonParser parser = new JsonParser();
        JsonObject object = parser.parse(reader).getAsJsonObject();
        JsonArray encodingsList = object.getAsJsonArray(Constants.ENCODINGS);
        for (int i = 0; i < encodingsList.size(); i++) {
            JsonObject encodingObject = encodingsList.get(i).getAsJsonObject();
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
                if(!(encodingObject.get(Constants.FILE_URL).isJsonNull()))
                    url = encodingObject.get(Constants.FILE_URL).getAsString();
            }else{
                if(!(encodingObject.get(Constants.URL).isJsonNull()))
                    url = encodingObject.get(Constants.URL).getAsString();
            }
            if("null".equalsIgnoreCase(url)|| url == null){
                url = encodingObject.get(Constants.MASTER_PLAYLIST_URL).getAsString();
            }
            mLogger.error(TAG + " Unsupported URL : "+url);
            //some cases only rtmp is present, dont add the encoding
            if("null".equalsIgnoreCase(url)|| url == null){
                continue;
            }
            encoding.mEncodingUrl = url!= null ?Uri.parse(url):null;
            //check if the encoding is already added based on url
            if(getEncodingFromUrl(url)!= null){
                continue;
            }
            encoding.mMediaID = mediaId;
            if(!(encodingObject.get(Constants.GROUP).isJsonNull()))
                encoding.mGroup = encodingObject.get(Constants.GROUP).getAsString();
            if(!(encodingObject.get(Constants.SIZE).isJsonNull()))
                encoding.mSizeInBytes = Integer.parseInt(encodingObject.get(Constants.SIZE).getAsString());
            if(!(encodingObject.get(Constants.HEIGHT).isJsonNull()))
                encoding.mHeight = Integer.parseInt(encodingObject.get(Constants.HEIGHT).getAsString());
            if(!(encodingObject.get(Constants.WIDTH).isJsonNull()))
                encoding.mWidth = Integer.parseInt(encodingObject.get(Constants.WIDTH).getAsString());
            if(!(encodingObject.get(Constants.AUDIO_BITRATE).isJsonNull()))
                encoding.mAudioBitRate = Integer.parseInt(encodingObject.get(Constants.AUDIO_BITRATE).getAsString());
            if(!(encodingObject.get(Constants.VIDEO_BITRATE).isJsonNull()))
                encoding.mVideoBitRate = Integer.parseInt(encodingObject.get(Constants.VIDEO_BITRATE).getAsString());
            mEncodingList.add(encoding);
        }
    }

    /**
     * This method returns the encoding associated with encoding url.
     * @param encodingUrl
     * @return Encoding
     */
    static Encoding getEncodingFromUrl(String encodingUrl) {
        for(Encoding encoding: mEncodingList ){
            if(encoding.mEncodingUrl.toString().equalsIgnoreCase(encodingUrl)){
                return encoding;
            }
        }
        return null;
    }

    /**
     * Method to set the paging parameters.
     * @param pageSize The number of results to return per page. The default and maximum page size is 500.
     * Example: page_size=100<br>
     * @param sortBy The field by which the results should be sorted. It has values like publish_date, create_date, update_date<br>
     * @param sortOrder The order in which the results should display. It has values like asc or  desc<br>
     */
    public void setPagingParameters(int pageSize,String sortBy, String sortOrder){
        mLogger.debug(TAG + " setPagingParameters "+ " pageSize: " + pageSize +" sortBy: " +sortBy+ " sortOrder: " + sortOrder);
        if(pageSize>= 50 && pageSize <=500)
            mPageSize = pageSize;
        if(sortBy!= null){
            if(sortBy.equalsIgnoreCase(mSortByValidValues[0])||sortBy.equalsIgnoreCase(mSortByValidValues[1]))
                mSortBy = sortBy;
        }
        if(sortOrder!= null){
            if(sortOrder.equalsIgnoreCase(mSortOrderValidValues[0])||sortOrder.equalsIgnoreCase(mSortOrderValidValues[1]))
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
    private String appendPagingParameters(String url,Map<String, String> params) throws UnsupportedEncodingException{
        StringBuilder urlBuilder = new StringBuilder(url);
        urlBuilder.append("?");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            urlBuilder.append(URLEncoder.encode(entry.getKey(),Constants.URL_CHARACTER_ENCODING_TYPE));
            urlBuilder.append("=");
            urlBuilder.append(URLEncoder.encode(entry.getValue(),Constants.URL_CHARACTER_ENCODING_TYPE));
            urlBuilder.append("&");
            urlBuilder.append(entry.getKey());
            urlBuilder.append("=");
            urlBuilder.append(entry.getValue());
            urlBuilder.append("&");
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
    Delivery getDeliveryForMedia(ArrayList<Encoding> encodingList) {
        if(encodingList!= null && encodingList.size() > 0){
        Map<Uri,ArrayList<Encoding>> encodingsMap = getEncodingsByUrl(encodingList);
        ArrayList<Delivery> deliveryList =  new ArrayList<Delivery>();
        for(Uri uri:encodingsMap.keySet()){
            ArrayList<Encoding> enc = encodingsMap.get(uri);
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
    private Map<Uri,ArrayList<Encoding>> getEncodingsByUrl(ArrayList<Encoding> encodingList){
        Map<Uri,ArrayList<Encoding>> map = new HashMap<Uri, ArrayList<Encoding>>();
        for(Encoding encoding:encodingList){
            if(!(map.containsKey(encoding.mEncodingUrl))){
                ArrayList<Encoding> enc = new ArrayList<Encoding>();
                enc.add(encoding);
                map.put(encoding.mEncodingUrl, enc);
            }else if((map.containsKey(encoding.mEncodingUrl))){
                ArrayList<Encoding> enc = map.get(encoding.mEncodingUrl);
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
    private Delivery getSuitableDelivery(ArrayList<Delivery> deliveryList){
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