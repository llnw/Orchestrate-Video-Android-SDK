package com.limelight.videosdk.model;

import java.util.Date;
import com.google.gson.annotations.SerializedName;
import android.net.Uri;

/**
 * Represents a channel item.Channel contains a list of media.
 * It is logical representation of medias.
 * @author Nagaraju
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
