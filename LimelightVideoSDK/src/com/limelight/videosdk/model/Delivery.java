package com.limelight.videosdk.model;

import java.util.ArrayList;



import android.net.Uri;

/**
 * A Delivery represents a single, playable resource.
 * The most basic element of media is the encoding, but some delivery 
 * mechanisms can package multiple encodings into one URL. As such, the 
 * delivery represents a collection of one-to-n encodings that are packaged together.
 * As an example, Widevine streaming does automatic bit-rate switching 
 * (as does Apple’s HLS), while Widevine offline only has a single bit 
 * rate (and, as such, a single encoding).
 * @author Nagaraju
 */
public class Delivery {

    /**
     * A UUID representing the delivery, in the form of 32 hexadecimal characters.
     */
    public String mDeliveryId;

    /**
     * The type of delivery.
     * Possible values: - HTTP Live Streaming - Mobile H.264 - Widevine - Widevine Offline
     */
    public DeliveryType mDeliveryType;


    /**
     * A boolean flag representing whether the delivery is downloadable.
     */
    public boolean mDownloadable;

    /**
     * The encodings that make up this delivery.
     */
    public ArrayList<Encoding> mEncodings;

    /**
     * The group to which the delivery belongs.
     */
    public String mGroup;

    /**
     * The URL of the delivery on disk.
     */
    public Uri mLocalURL;

    /**
     * The ID of the media to which the delivery belongs.
     */
    public String mMediaId;

    /**
     * A boolean flag representing whether the delivery has digital rights management
     */
    public boolean mProtected;

    /**
     * The URL of the delivery where it can be streamed or downloaded.
     */
    public Uri mRemoteURL;
}
