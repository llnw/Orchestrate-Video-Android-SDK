package com.limelight.videosdk.model;

import java.util.Date;
import com.google.gson.annotations.SerializedName;
import android.net.Uri;

/**
 * Represent a channel Group item. Channel group contains a list of channels.
 * It is logical representation of channels.
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
