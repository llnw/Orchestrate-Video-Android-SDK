package com.limelight.videosdk;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import android.content.Context;
import android.net.Uri;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.limelight.videosdk.model.Channel;
import com.limelight.videosdk.model.ChannelGroup;
import com.limelight.videosdk.model.Delivery;
import com.limelight.videosdk.model.DeliveryType;
import com.limelight.videosdk.model.Encoding;
import com.limelight.videosdk.model.Media;
import com.limelight.videosdk.model.Media.MediaThumbnail;
import com.limelight.videosdk.model.PrimaryUse;
import com.limelight.videosdk.utility.Connection;
import com.limelight.videosdk.utility.Setting;

/**
 * This class facilitates developer and SDK in making requests to Limelight server
 * and fetching various data like channel group, channels,media and encodings.
 * This exposes APIs for  the developers.
 * It uses connection class to check if the connection exists before making the 
 * requests.It also uses URLAutheticator to authenticate the requests by 
 * using developer details stored in setting.It stores the list of channel 
 * groups, channels, media, encodings and delivery.
 * @author kanchan
 */
public class ContentService {

    private static ArrayList<Channel> mChannelList = new ArrayList<Channel>();
    private static ArrayList<Channel> mChannelListOfGroup = new ArrayList<Channel>();
    private static ArrayList<ChannelGroup> mChannelGroupList = new ArrayList<ChannelGroup>();
    private static ArrayList<Media> mMediaList = new ArrayList<Media>();
    private static ArrayList<Media> mMediaListOfChannel = new ArrayList<Media>();
    private static ArrayList<Encoding> mEncodingList = new ArrayList<Encoding>();
    private static ArrayList<Media> mMediaSearchList = new ArrayList<Media>();

    private int mPageSize = 100;
    private String[] mSortByValidValues = {"update_date","create_date"};
    private String[] mSortOrderValidValues = {"asc","desc"};
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
               throw new Exception("Device Not Connected");
           }else{
               Exception exception = null;
               HttpURLConnection urlConnection = null;
               try{
                   String resourceUrl = Setting.sApiEndPoint + String.format(Constants.FETCH_ALL_CHANNEL_GROUP_PATH, Setting.sOrgId);
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
                   String url = URLAuthenticator.authenticateRequest(Constants.GET,resourceUrl, Setting.sAccessKey, Setting.sSecret, params);
                   urlConnection = (HttpURLConnection) new URL(url).openConnection();
                   InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                   parseChannelGroups(readString(inputStream));
               }
               catch (NoSuchAlgorithmException e) {
                   exception = new Exception("Signing Failed !");
               }
               catch (MalformedURLException e) {
                   exception =  new Exception("Invalid URL!");
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
                callback.onError(new Throwable("Device Not Connected"));
            }else{
                try {
                    String resourceUrl = Setting.sApiEndPoint + String.format(Constants.FETCH_ALL_CHANNEL_GROUP_PATH, Setting.sOrgId);
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
                    final String url = URLAuthenticator.authenticateRequest(Constants.GET,resourceUrl, Setting.sAccessKey, Setting.sSecret, params);
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            Throwable throwable = null;
                            HttpURLConnection urlConnection = null;
                            try {
                                urlConnection = (HttpURLConnection) new URL(url).openConnection();
                                InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                                parseChannelGroups(readString(inputStream));
                                callback.onSuccess(mChannelGroupList);
                            }
                            catch (MalformedURLException e) {
                                throwable = new Throwable("Invalid Thumbnail URL!");
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
                throw new Exception("Device Not Connected");
            }else{
                Exception exception = null;
                HttpURLConnection urlConnection = null;
                try {
                    String resourceUrl = Setting.sApiEndPoint + String.format(Constants.FETCH_ALL_CHANNEL_OF_GROUP_PATH, Setting.sOrgId,channelGroupId);
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
                    resourceUrl = appendPagingParameters(resourceUrl, params);
                    urlConnection = (HttpURLConnection) new URL(resourceUrl).openConnection();
                    InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                    parseChannelOfGroup(readString(inputStream));
                }
                catch (MalformedURLException e) {
                    exception = new Exception("Invalid Thumbnail URL!");
                }
                catch (UnsupportedEncodingException e) {
                    exception = new Exception("Failed To Append Paging Parameter!");
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
     * @param channelGroupId ChannelGroupId
     * @param isLoadMore True if load more called else false.
     * @param callback ChannelCallback
     */
    public void getAllChannelOfGroupAsync(final String channelGroupId, final boolean isLoadMore,final ChannelCallback callback) {

        if (Setting.sOrgId == null || Setting.sOrgId.trim().length() == 0) {
            callback.onError(new Throwable("Please Ensure Organization ID is Set."));
        } else {
            if(!Connection.isConnected(mContext)){
                callback.onError(new Throwable("Device Not Connected"));
            }else{
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        HttpURLConnection urlConnection = null;
                        Throwable throwable = null;
                        try {
                            String resourceUrl = Setting.sApiEndPoint + String.format(Constants.FETCH_ALL_CHANNEL_OF_GROUP_PATH, Setting.sOrgId,channelGroupId);
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
                            resourceUrl = appendPagingParameters(resourceUrl, params);
                            urlConnection = (HttpURLConnection) new URL(resourceUrl).openConnection();
                            InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                            parseChannelOfGroup(readString(inputStream));
                            callback.onSuccess(mChannelListOfGroup);
                        }
                        catch (UnsupportedEncodingException e) {
                            throwable = new Throwable("Failed To Append Paging Parameter!");
                        }
                        catch (MalformedURLException e) {
                            throwable = new Throwable("Invalid Thumbnail URL!");
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
     * @param isLoadMore True is loadmore called else false.
     * @return list of channels
     * @throws Exception
     */
    public ArrayList<Channel> getAllChannel(boolean isLoadMore) throws Exception{

        if (!Setting.isAccountConfigured(Setting.sOrgId, Setting.sAccessKey, Setting.sSecret)) {
            throw new Exception("Please Ensure Organization ID, Access key And Secret Are Set.");
        } else {
            if(!Connection.isConnected(mContext)){
                throw new Exception("Device Not Connected");
            }else{
                Exception exception = null;
                HttpURLConnection urlConnection = null;
                try {
                    String resourceUrl = Setting.sApiEndPoint + String.format(Constants.FETCH_ALL_CHANNEL_PATH, Setting.sOrgId);
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
                    String url = URLAuthenticator.authenticateRequest(Constants.GET,resourceUrl, Setting.sAccessKey, Setting.sSecret, params);
                    urlConnection = (HttpURLConnection) new URL(url).openConnection();
                    InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                    String body = readString(inputStream);
                    parseChannels(body);
                }
                catch (MalformedURLException e) {
                    exception = new Exception("Invalid URL!");
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
     * @param isLoadMore True is load more called else false.
     * @param callback ChannelCallback
     */
    public void getAllChannelAsync(boolean isLoadMore,final ChannelCallback callback) {

        if (!Setting.isAccountConfigured(Setting.sOrgId, Setting.sAccessKey, Setting.sSecret)) {
            callback.onError(new Throwable("Please Ensure Organization ID, Access key And Secret Are Set."));
        } else {
            if(!Connection.isConnected(mContext)){
                callback.onError(new Throwable("Device Not Connected"));
            }else{
                try {
                    String resourceUrl = Setting.sApiEndPoint + String.format(Constants.FETCH_ALL_CHANNEL_PATH, Setting.sOrgId);
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
                    final String url = URLAuthenticator.authenticateRequest(Constants.GET,resourceUrl, Setting.sAccessKey, Setting.sSecret, params);
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            Throwable throwable = null;
                            HttpURLConnection urlConnection = null;
                            try {
                                urlConnection = (HttpURLConnection) new URL(url).openConnection();
                                InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                                parseChannels(readString(inputStream));
                                callback.onSuccess(mChannelList);
                            }
                            catch (MalformedURLException e) {
                                throwable = new Throwable("Invalid Thumbnail URL!");
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
     * @param channelId ChannelId
     * @param isLoadMore True is loadmore called else false.
     * @return list of medias
     * @throws Exception
     */
    public ArrayList<Media> getAllMediaOfChannel(String channelId,boolean isLoadMore) throws Exception{

        if (Setting.sOrgId == null || Setting.sOrgId.trim().length() == 0) {
            throw new Exception("Please Ensure Organization ID is Set.");
        } else {
            if(!Connection.isConnected(mContext)){
                throw new Exception("Device Not Connected");
            }else{
                Exception exception = null;
                HttpURLConnection urlConnection = null;
                try {
                    String resourceUrl = Setting.sApiEndPoint + String.format(Constants.FETCH_ALL_MEDIA_OF_CHANNEL_PATH, Setting.sOrgId,channelId);
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
                    resourceUrl = appendPagingParameters(resourceUrl, params);
                    urlConnection = (HttpURLConnection) new URL(resourceUrl).openConnection();
                    InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                    String body = readString(inputStream);
                    parseMediaOfChannel(body);
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
     * @param channelId ChannelId
     * @param isLoadMore True is loadmore called else false.
     * @param callback MediaCallback
     */
    public void getAllMediaOfChannelAsync(final String channelId,final boolean isLoadMore,final MediaCallback callback) {

        if (Setting.sOrgId == null || Setting.sOrgId.trim().length() == 0) {
            callback.onError(new Throwable("Please Ensure Organization ID is Set."));
        } else {
            if(!Connection.isConnected(mContext)){
                callback.onError(new Throwable("Device Not Connected"));
            }else{
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        Throwable throwable = null;
                        HttpURLConnection urlConnection = null;
                        try {
                            String resourceUrl = Setting.sApiEndPoint + String.format(Constants.FETCH_ALL_MEDIA_OF_CHANNEL_PATH, Setting.sOrgId,channelId);
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
                            final HashMap<String, String> params = new HashMap<String, String>();
                            params.put(Constants.PAGE_ID, ""+mPageIdMediaOfChannel);
                            params.put("page_size", ""+mPageSize);
                            params.put("sort_by", mSortBy);
                            params.put("sort_order", mSortOrder);
                            resourceUrl = appendPagingParameters(resourceUrl, params);
                            urlConnection = (HttpURLConnection) new URL(resourceUrl).openConnection();
                            InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                            String body = readString(inputStream);
                            parseMediaOfChannel(body);
                            callback.onSuccess(mMediaListOfChannel);
                        }
                        catch (UnsupportedEncodingException e) {
                            throwable = new Throwable("Failed To Append Paging Parameter!");
                        }
                        catch (MalformedURLException e) {
                            throwable = new Throwable("Invalid Thumbnail URL!");
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
    * @param isLoadMore True is loadmore called else false.
    * @return list of media
    * @throws Exception
    */
    public ArrayList<Media> getAllMedia(boolean isLoadMore) throws Exception{

        if (!Setting.isAccountConfigured(Setting.sOrgId, Setting.sAccessKey, Setting.sSecret)) {
            throw new Exception("Please Ensure Organization ID, Access key And Secret Are Set.");
        } else {
            if(!Connection.isConnected(mContext)){
                throw new Exception("Device Not Connected");
            }else{
                Exception exception = null;
                HttpURLConnection urlConnection = null;
                try {
                    String resourceUrl = Setting.sApiEndPoint + String.format(Constants.SEARCH_ALL_MEDIA_PATH, Setting.sOrgId);
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
                    String url = URLAuthenticator.authenticateRequest(Constants.GET,resourceUrl, Setting.sAccessKey, Setting.sSecret, params);
                    urlConnection = (HttpURLConnection) new URL(url).openConnection();
//                    BufferedReader inputStream = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                    parseMedias(readString(inputStream));
                }catch (MalformedURLException e) {
                    exception = new Exception("Invalid URL!");
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
     * @param isLoadMore True is loadmore called else false.
     * @param callback MediaCallback
     */
    public void getAllMediaAsync(boolean isLoadMore,final MediaCallback callback) {

        if (!Setting.isAccountConfigured(Setting.sOrgId, Setting.sAccessKey, Setting.sSecret)) {
            callback.onError(new Throwable("Please Ensure Organization ID, Access key And Secret Are Set."));
        } else {
            if(!Connection.isConnected(mContext)){
                callback.onError(new Throwable("Device Not Connected"));
            }else{
                try {
                    String resourceUrl = Setting.sApiEndPoint + String.format(Constants.SEARCH_ALL_MEDIA_PATH, Setting.sOrgId);
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
                    final String url = URLAuthenticator.authenticateRequest(Constants.GET,resourceUrl, Setting.sAccessKey, Setting.sSecret, params);
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            Throwable throwable = null;
                            HttpURLConnection urlConnection = null;
                            try {
                                urlConnection = (HttpURLConnection) new URL(url).openConnection();
                                InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                                parseMedias(readString(inputStream));
                                callback.onSuccess(mMediaList);
                            }
                            catch (MalformedURLException e1) {
                                throwable = new Throwable("Invalid Thumbnail URL!");
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
     * @param operator And / OR
     * @param title Title of Media - Optional
     * @param description Description of Media - Optional
     * @param tag Tag of Media - Optional
     * @param state state of Media(published/unpublished)
     * @param mediaType Type of media (audio/video)
     * @param channelId channel id for the medias
     * @throws Exception
     */
    public ArrayList<Media> searchMedia(boolean isLoadMore,String operator,String title,
            String description,String tag,String state,String mediaType,String channelId) throws Exception{

        if (!Setting.isAccountConfigured(Setting.sOrgId, Setting.sAccessKey, Setting.sSecret)) {
            throw new Exception("Please Ensure Organization ID, Access key And Secret Are Set.");
        } else {
            if(!Connection.isConnected(mContext)){
                throw new Exception("Device Not Connected");
            }else{
                Exception exception = null;
                HttpURLConnection urlConnection = null;
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
                    String resourceUrl = Setting.sApiEndPoint + String.format(Constants.SEARCH_ALL_MEDIA_PATH, Setting.sOrgId);
                    String url = URLAuthenticator.authenticateRequest(Constants.GET,resourceUrl, Setting.sAccessKey, Setting.sSecret, params);
                    urlConnection = (HttpURLConnection) new URL(url).openConnection();
                    InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                    parseSearchMedias(readString(inputStream));
                }catch (MalformedURLException e) {
                    exception = new Exception("Invalid URL!");
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
     * @param isLoadMore true if load next page
     * @param operator And / OR
     * @param title Title of Media - Optional
     * @param description Description of Media - Optional
     * @param tag Tag of Media - Optional
     * @param state state of Media(published/unpublished)
     * @param mediaType Type of media (audio/video)
     * @param channelId channel id for the medias
     * @param callback MediaCallback
     * @throws Exception
     */
    public void searchMediaAsync(boolean isLoadMore,String operator,String title,String description,String tag,String state,String mediaType,String channelId,final MediaCallback callback) {

        if (!Setting.isAccountConfigured(Setting.sOrgId, Setting.sAccessKey, Setting.sSecret)) {
            callback.onError(new Throwable("Please Ensure Organization ID, Access key And Secret Are Set."));
        } else {
            if(!Connection.isConnected(mContext)){
                callback.onError(new Throwable("Device Not Connected"));
            }else{
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

                final String resourceUrl = Setting.sApiEndPoint + String.format(Constants.SEARCH_ALL_MEDIA_PATH, Setting.sOrgId);
                try {
                    final String url = URLAuthenticator.authenticateRequest(Constants.GET,resourceUrl, Setting.sAccessKey, Setting.sSecret, params);
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            Throwable throwable = null;
                            HttpURLConnection urlConnection = null;
                            try {
                                urlConnection = (HttpURLConnection) new URL(url).openConnection();
                                InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                                parseSearchMedias(readString(inputStream));
                                callback.onSuccess(mMediaSearchList);
                            }
                            catch (MalformedURLException e1) {
                                throwable = new Throwable("Invalid Request URL!");
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
     * @param channelId ChannelId
     * @param callback ChannelCallback
     */
    public void getChannelAsync(final String channelId,final ChannelCallback callback) {
        if (Setting.sOrgId == null || Setting.sOrgId.trim().length() == 0) {
            callback.onError(new Throwable("Please Ensure Organization ID is Set."));
        } else {
            if(!Connection.isConnected(mContext)){
                callback.onError(new Throwable("Device Not Connected"));
            }else{
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        HttpURLConnection urlConnection = null;
                        try {
                            String resourceUrl = Setting.sApiEndPoint + String.format(Constants.FETCH_CHANNEL_PROPERTY_PATH, Setting.sOrgId,channelId);
                            urlConnection = (HttpURLConnection) new URL(resourceUrl).openConnection();
                            InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                            ArrayList<Channel> channelList = new ArrayList<Channel>();
                            channelList.add(parseChannelProperty(readString(inputStream)));
                            callback.onSuccess(channelList);
                        }
                        catch (MalformedURLException e) {
                            callback.onError(new Throwable("Invalid Request URL!"));
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
     * @param mediaId mediaId
     * @param callback MediaCallback
     */
    public void getMediaAsync(final String mediaId,final MediaCallback callback) {

        if (Setting.sOrgId == null || Setting.sOrgId.trim().length() == 0) {
            callback.onError(new Throwable("Please Ensure Organization ID is Set."));
        } else {
            if(!Connection.isConnected(mContext)){
                callback.onError(new Throwable("Device Not Connected"));
            }else{
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        HttpURLConnection urlConnection = null;
                        try {
                            String resourceUrl = Setting.sApiEndPoint + String.format(Constants.FETCH_MEDIA_PROPERTY_PATH, Setting.sOrgId,mediaId);
                            urlConnection = (HttpURLConnection) new URL(resourceUrl).openConnection();
                            InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                            ArrayList<Media> mediaList = new ArrayList<Media>();
                            mediaList.add(parseMediaProperty(readString(inputStream)));
                            callback.onSuccess(mediaList);
                        }
                        catch (MalformedURLException e1) {
                            callback.onError(new Throwable("Invalid Thumbnail URL!"));
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
                    final String url = URLAuthenticator.authenticateRequest(Constants.GET,resourceUrl, Setting.sAccessKey, Setting.sSecret, params);
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            HttpURLConnection urlConnection = null;
                            try {
                                urlConnection = (HttpURLConnection) new URL(url).openConnection();
                                InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                                String body = readString(inputStream);
                                parseEncodings(body,mediaId);
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
    private void parseChannelGroups(String channelGroups){
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Date.class, new DateDeserializer());
        builder.registerTypeAdapter(Uri.class, new UriDeserializer());
        Gson gson = builder.create();
        JsonParser parser = new JsonParser();
        JsonObject channelGroupsObject = parser.parse(channelGroups).getAsJsonObject();
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
    private void parseChannelOfGroup(String channels){
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Date.class, new DateDeserializer());
        builder.registerTypeAdapter(Uri.class, new UriDeserializer());
        Gson gson = builder.create();
        JsonParser parser = new JsonParser();
        JsonObject channelsObject = parser.parse(channels).getAsJsonObject();
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
    private void parseChannels(String channels){
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Date.class, new DateDeserializer());
        builder.registerTypeAdapter(Uri.class, new UriDeserializer());
        Gson gson = builder.create();
        JsonParser parser = new JsonParser();
        JsonObject channelsObject = parser.parse(channels).getAsJsonObject();
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
    private void parseMediaOfChannel(String medias){
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Date.class, new DateDeserializer());
        builder.registerTypeAdapter(Time.class, new TimeDeserializer());
        builder.registerTypeAdapter(Uri.class, new UriDeserializer());
        builder.registerTypeAdapter(MediaThumbnail.class, new ThumbnailDeserializer());
        Gson gson = builder.create();
        JsonParser parser = new JsonParser();
        JsonObject mediasObject = parser.parse(medias).getAsJsonObject();
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
    private Channel parseChannelProperty(String channels){
        Channel channel = new Channel();
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Date.class, new DateDeserializer());
        builder.registerTypeAdapter(Uri.class, new UriDeserializer());
        Gson gson = builder.create();
        JsonParser parser = new JsonParser();
        JsonObject channelsObject = parser.parse(channels).getAsJsonObject();
        if(channelsObject.isJsonObject()){
            channel = gson.fromJson(channelsObject,  Channel.class);
        }
        return channel;
    }

    /**
     * This method parses media properties from JSON data.
     * @param medias json array
     */
    private Media parseMediaProperty(String medias){
        Media media = new Media();
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Date.class, new DateDeserializer());
        builder.registerTypeAdapter(Time.class, new TimeDeserializer());
        builder.registerTypeAdapter(Uri.class, new UriDeserializer());
        builder.registerTypeAdapter(MediaThumbnail.class, new ThumbnailDeserializer());
        Gson gson = builder.create();
        JsonParser parser = new JsonParser();
        JsonObject mediasObject = parser.parse(medias).getAsJsonObject();
        if(mediasObject.isJsonObject()){
            media = gson.fromJson(mediasObject,  Media.class);
        }
        return media;
    }

    /**
     * This method parses media from JSON data.
     * @param medias json array
     */
    private void parseMedias(String medias){
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Date.class, new DateDeserializer());
        builder.registerTypeAdapter(Time.class, new TimeDeserializer());
        builder.registerTypeAdapter(Uri.class, new UriDeserializer());
        builder.registerTypeAdapter(MediaThumbnail.class, new ThumbnailDeserializer());
        Gson gson = builder.create();
        JsonParser parser = new JsonParser();
        JsonObject mediasObject = parser.parse(medias).getAsJsonObject();
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
    private void parseSearchMedias(String medias){
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Date.class, new DateDeserializer());
        builder.registerTypeAdapter(Time.class, new TimeDeserializer());
        builder.registerTypeAdapter(Uri.class, new UriDeserializer());
        builder.registerTypeAdapter(MediaThumbnail.class, new ThumbnailDeserializer());
        Gson gson = builder.create();
        JsonParser parser = new JsonParser();
        JsonObject mediasObject = parser.parse(medias).getAsJsonObject();
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
     * @param encodings json array
     * @param mediaId
     * @throws JSONException
     */
    private void parseEncodings(String encodings, String mediaId) throws JSONException{
        mEncodingList.clear();
        JSONObject object = (JSONObject) new JSONTokener(encodings).nextValue();
        JSONArray encodingsList = object.getJSONArray(Constants.ENCODINGS);
        for (int i = 0; i < encodingsList.length(); i++) {
            JSONObject encodingObject = encodingsList.getJSONObject(i);
            Encoding encoding = new Encoding();
            String use = encodingObject.getString(Constants.PRIMARY_USE);
            try{
                encoding.primaryUse =PrimaryUse.valueOf(use);
            }catch(Exception e){
                mLogger.error("Unsupported Primary Use : "+use);
                continue;
            }
            String url;
            if(encoding.primaryUse.equals(PrimaryUse.Flash)){
                url = encodingObject.getString(Constants.FILE_URL);
            }else{
                url = encodingObject.getString(Constants.URL);
            }
            if(url.equalsIgnoreCase("null")){
                url = encodingObject.getString(Constants.MASTER_PLAYLIST_URL);
            }//some cases only rtmp is present, dont add the encoding
            if(url.equalsIgnoreCase("null")){
                continue;
            }
            encoding.mEncodingUrl = url!= null ?Uri.parse(url):null;
            encoding.mMediaID = mediaId;
            encoding.mGroup = encodingObject.getString(Constants.GROUP);
            encoding.mSizeInBytes = Integer.parseInt(encodingObject.getString(Constants.SIZE));
            encoding.mHeight = Integer.parseInt(encodingObject.getString(Constants.HEIGHT));
            encoding.mWidth = Integer.parseInt(encodingObject.getString(Constants.WIDTH));
            encoding.mAudioBitRate = Integer.parseInt(encodingObject.getString(Constants.AUDIO_BITRATE));
            encoding.mVideoBitRate = Integer.parseInt(encodingObject.getString(Constants.VIDEO_BITRATE));
            mEncodingList.add(encoding);
        }
    }

    private String readString(InputStream inputStream) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[8*1024];
        for (int n; 0 < (n = inputStream.read(buf));) {
            bos.write(buf, 0, n);
        }
        bos.close();
        return new String(bos.toByteArray(),Constants.URL_CHARACTER_ENCODING_TYPE); // Or whatever encoding
    }

    //Works fine but not much improvement compared to other readString()
/*    private String readString(BufferedReader inputStream) throws IOException {
        String data = "";
        String temp = null;
        temp = inputStream.readLine();
        while(temp != null) {
            data = data+ temp;
            temp = inputStream.readLine();
        }
        return data;
    }*/
    /**
     * This method returns the encoding associated with encoding url.
     * @param encodingUrl
     * @return Encoding
     */
    public static Encoding getEncodingFromUrl(String encodingUrl) {
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
     * Cursory checks are done on the encodings to ensure that the URL 
     * is the same for each one. If they are not, a NSInternalInconsistencyException is thrown.
     * @param encodings
     * @return Delivery
     */
    public Delivery getDeliveryForMedia(ArrayList<Encoding> encodingList) {
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
            return null;
        }
    }

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