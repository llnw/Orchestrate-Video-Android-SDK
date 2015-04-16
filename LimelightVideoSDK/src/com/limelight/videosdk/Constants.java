package com.limelight.videosdk;

/**
 * This class contains the constants being used in LimelightVideoSDK
 * @author kanchan
 *
 */
public class Constants {

    public static final String DEVICE_ID_KEY = "deviceId";
    public static final String URL_CHARACTER_ENCODING_TYPE = "UTF-8";
    public static final String SHA256_HASH_ALGORITHM = "HmacSHA256";
    public static final String MD5_HASH_ALGORITHM = "HmacMD5";
    public static final String GET = "GET";
    public static final String API_URL = "http://api.video.limelight.com/rest";
    public static final String ACCESS_KEY = "access_key";
    public static final String EXPIRES = "expires";
    public static final long BUFFERING_TIMEOUT = 5*60*1000;
    public static final int PREPARING_TIMEOUT = 1*60*1000;
    //URLS
    public static final String FETCH_ALL_CHANNEL_GROUP_PATH = "/organizations/%s/channelgroups/all.json";
    public static final String FETCH_ALL_CHANNEL_OF_GROUP_PATH = "/organizations/%s/channelgroups/%s/channels.json";
    public static final String FETCH_ALL_CHANNEL_PATH = "/organizations/%s/channels/all.json";
    public static final String FETCH_ALL_MEDIA_OF_CHANNEL_PATH = "/organizations/%s/channels/%s/media.json";
    public static final String SEARCH_ALL_MEDIA_PATH = "/organizations/%s/media/search.json";
    public static final String FETCH_CHANNEL_PROPERTY_PATH = "/organizations/%s/channel/%s/properties.json";
    public static final String FETCH_MEDIA_PROPERTY_PATH = "/organizations/%s/media/%s/properties.json";
    public static final String ENCODING_PATH = "/organizations/%s/media/%s/encodings.json";
    public static final String API_ENDPOINT_STAGING = "https://staging-api.lvp.llnw.net/rest";
    public static final String API_ENDPOINT_PROD = "https://play.video.limelight.com/rest";
    public static final String LICENSE_PROXY_STAGING = "https://staging-wlp.lvp.llnw.net/license";
    public static final String LICENSE_PROXY_PROD = "https://wlp.video.limelight.com/license";
    public static final String ANALYTICS_ENDPOINT_STAGING = "https://staging-mcs.lvp.llnw.net/r/MetricsCollectionService/recordMetricsEvent";
    public static final String ANALYTICS_ENDPOINT_PROD = "https://production-mcs.lvp.llnw.net/r/MetricsCollectionService/recordMetricsEvent";
    //URLS end
    //JSON Keys
    public static final String TITLE = "title";
    public static final String DESCRIPTION = "description";
    public static final String URL = "url";
    //Channel Group
    public static final String CHANNEL_GROUPS = "channelgroup_list";
    public static final String CHANNEL_GROUP_ID = "channelgroup_id";
    //Channel
    public static final String CHANNELS = "channel_list";
    public static final String CHANNEL_ID = "channel_id";
    //Media
    public static final String MEDIAS = "media_list";
    public static final String MEDIA_ID = "media_id";
    public static final String DURATION = "duration_in_milliseconds";
    public static final String MEDIA_TYPE = "media_type";
    public static final String TAG = "tag";
    public static final String STATE = "state";
    public static final String ORIGINAL_FILENAME = "original_filename";
    public static final String CREATED_AFTER = "created_after";
    public static final String UPDATED_AFTER = "updated_after";
    public static final String PUBLISHED_AFTER = "published_after";
    //Thumbnail
    public static final String THUMBNAIL = "thumbnails";
    //Encoding
    public static final String GROUP = "group";
    public static final String SIZE = "size_in_bytes";
    public static final String PRIMARY_USE = "primary_use";
    public static final String ENCODINGS = "encodings";
    public static final String VIDEO_BITRATE = "video_bitrate";
    public static final String AUDIO_BITRATE = "audio_bitrate";
    public static final String FILE_URL = "file_url";
    public static final String MASTER_PLAYLIST_URL = "master_playlist_url";
    public static final String THUMBNAIL_URL = "thumbnail_url";
    public static final String WIDTH = "width";
    public static final String HEIGHT = "height";
    public static final String PAGE_ID = "page_id";
    public static final String PAGE_SIZE = "page_size";
    public static final String SORT_BY = "sort_by";
    public static final String SORT_ORDER = "sort_order";
    //JSON Keys end
    public static final String SORT_BY_UPDATE_DATE = "update_date";
    public static final String SORT_BY_CREATE_DATE = "create_date";
    public static final String SORT_ORDER_ASC = "asc";
    public static final String SORT_ORDER_DESC = "desc";
    //Model type
    public static final int TYPE_CHANNEL_GROUP = 1;
    public static final int TYPE_CHANNEL = 2;
    public static final int TYPE_MEDIA = 3;
    /**
     * Various message states.<br>
     * status 0<br>
     * error 1<br>
     * progress 2<br>
     * @author kanchan
     */
    public enum Message{
        status,
        error,
        progress
    }
    /**
     * Various play states.<br>
     * Playing 0 <br>
     * Paused 1 <br>
     * Stopped 2 <br>
     * Completed 3 <br>
     */
    public enum PlayerState {
        playing,
        paused,
        stopped,
        completed
    }
    /**
     * Various Widevine status values
     * @author kanchan
     */
    enum WidevineStatus {
        OK,
        NotInitialized,
        AlreadyInitialized,
        FileNotPresent,
        NotRegistered,
        AlreadyRegistered,
        FileSystemError
    }
}