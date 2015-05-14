package com.limelight.videosdk.model;

import com.google.gson.annotations.SerializedName;
import android.net.Uri;

/**
 * This class holds information related to a single Encoding.
 * Media is organized as different encoding formats.
 * The Meta data properties associated with Encoding are stored in this class.
 * LimelightVideoSDK will perform API query request using Content Service class to fetch the Encodings,
 * the response received will have the Encoding properties information.
 * The response received will be processed and properties information will be stored in this class.
 * The list of Encodings will be stored in encoding list in ContentService.
 * Encoding contains attributes like media id, audio bit rate, video bit rate, size in bytes,
 * primary use, play list Uri and encoding Uri etc.
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