package com.limelight.videosdk.model;

import java.util.Date;
import com.google.gson.annotations.SerializedName;
import android.net.Uri;

/**
 * This class holds information related to a single channel group.
 * Channel groups are used to organize channels into logical groupings.
 * The Meta data properties associated with channel groups are stored in this class.
 * LimelightVideoSDK will perform API query request using Content Service class,
 * the response received will have the channel group properties information.
 * The response received will be processed and properties information will be stored in this class.
 * The list of channel groups will be stored in channel group list in ContentService.
 * Channel group contains attributes like channel group id, title, date created and date updated etc.
 * @author Nagaraju
 */
public class ChannelGroup {

    /**
     * The unique identifier for channel group.
     */
    @SerializedName("channelgroup_id")
    public String mChannelGroupId;

    /**
     * The name of the channel group.
     */
    @SerializedName("title")
    public String mTitle;

    /**
     * The thumbnail url for this channel group.
     */
    @SerializedName("thumbnail_url")
    public Uri mThumbnailUrl;

    /**
     * The date the channel group was created.
     */
    @SerializedName("create_date")
    public Date mDateCreated;

    /**
     * The date the channel group was updated.
     */
    @SerializedName("update_date")
    public Date mDateUpdated;
}
