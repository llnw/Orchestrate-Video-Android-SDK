package com.limelight.videosdk.model;

import java.sql.Time;
import java.util.Date;
import com.google.gson.annotations.SerializedName;
import android.net.Uri;

/**
 * Represents a media item.
 * @author kanchan
 */
public class Media {

    /**
     * The ID of the media item.
     */
    @SerializedName("media_id")
    public String mMediaID;

    /**
     * The name of the media item.
     */
    @SerializedName("title")
    public String mTitle;

    /**
     * The description of the media item.
     */
    @SerializedName("description")
    public String mSummary;

    /**
     * The type of media, type like Video, Audio and LiveStream.
     */
    @SerializedName("media_type")
    public String mMediaType;

    /**
     * The duration of the media.
     */
    // Time mDuration;
    @SerializedName("duration_in_milliseconds")
    public Time mDuration;

    /**
     * The date when the media was added to the platform.
     */
    @SerializedName("create_date")
    public Date mDateCreated;

    /**
     * The date the media was last updated.
     */
    @SerializedName("update_date")
    public Date mDateUpdated;

    /**
     * The thumbnail for this media.
     */
    @SerializedName("thumbnails")
    public MediaThumbnail mThumbnail;

    /**
     * Represents a Thumbnail item.
     * 
     */
    public class MediaThumbnail {
        /**
         * Represents a Thumbnail URL.
         * 
         */
        @SerializedName("url")
        public Uri mUrl;
    }

    /**
     * The captions array for this media.
     */
    @SerializedName("captions")
    public Captions[] mClosedCaptionsArray;

    /**
     * Represents a Caption item.
     * 
     */
    public class Captions {

        /**
         * Represents a Caption language code.
         */
        @SerializedName("language_code")
        public String mLanguageCode;

        /**
         * Represents a Caption language name.
         */
        @SerializedName("language_name")
        public String mLanguageName;

        /**
         * Represents a Caption native language name.
         */
        @SerializedName("native_language_name")
        public String mNativeLanguageName;

        /**
         * Represents a Caption URL.
         */
        @SerializedName("url")
        public Uri mCaptionUrl;
    }
}
