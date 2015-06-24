package com.limelight.videosdk.model;

import java.util.Date;
import com.google.gson.annotations.SerializedName;
import android.net.Uri;

/**
 * This class holds information related to a single channel.
 * Channels are used to organize media into logical groupings.
 * The Meta data properties associated with channels are stored in this class.
 * LimelightVideoSDK will perform API query request using Content Service class to fetch the channel,
 * the response received will have the channel properties information.
 * The response received will be processed and properties information will be stored in this class.
 * The list of channels will be stored in channel list in ContentService.
 * Channel contains attributes like channel id, title, description, media id and thumbnail Uri etc.
 */
public class Channel {

    /**
     * The unique identifier of the channel.
     */
    @SerializedName("channel_id")
    public String mChannelId;

    /**
     * The name of the channel.
     */
    @SerializedName("title")
    public String mTitle;

    /**
     * The description of the channel.
     */
    @SerializedName("description")
    public String mDescription;

    /**
     * The URL of the thumbnail image associated with this channel.
     */
    @SerializedName("thumbnail_url")
    public Uri mThumbnailUrl;

    /**
     * The date the channel was created.
     */
    @SerializedName("create_date")
    public Date mDateCreated;

    /**
     * The current state of the channel.
     */
    @SerializedName("state")
    public String mState;

    /**
     * The flag that enables/disables autoplay functionality.
     */
    @SerializedName("autoplay_enabled")
    public boolean mAutoPlayEnabled;

    @SerializedName("update_date")
    /**
     * The date the channel was last updated.
     */
    public Date mDateUpdated;
}
