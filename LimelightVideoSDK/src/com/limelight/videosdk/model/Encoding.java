package com.limelight.videosdk.model;

import com.google.gson.annotations.SerializedName;
import android.net.Uri;

/**
 * Encoding is the basic element of media. this class represents the details of single encoding.
 * @author Nagaraju
 */
public class Encoding {

    /**
     * The unique identifier for media.
     */
    @SerializedName("media_id")
    public String mMediaID;

    /**
     * The audio bitrate.
     */
    @SerializedName("audio_bitrate")
    public int mAudioBitRate;

    /**
     * The video bitrate.
     */
    @SerializedName("video_bitrate")
    public int mVideoBitRate;

    /**
     * The data size in bytes.
     */
    @SerializedName("size_in_bytes")
    public long mSizeInBytes;

    /**
     * The primary use, The type of use the encoding is primarily targeted
     * toward.
     */
    @SerializedName("primary_use")
    public PrimaryUse primaryUse;

    /**
     * The url for encoding media data.
     */
    @SerializedName("url")
    public Uri mEncodingUrl;

    /**
     * The url for flash using http .
     */
    @SerializedName("file_url")
    public Uri mFileUrl;

    /**
     * The playlist url.
     */
    @SerializedName("master_playlist_url")
    public Uri mPlaylistUrl;

    /**
     * The encoding media group name.
     */
    @SerializedName("group")
    public String mGroup;

    /**
     * The encoding media height.
     */
    @SerializedName("height")
    public int mHeight;

    /**
     * The encoding media width.
     */
    @SerializedName("width")
    public int mWidth;
}