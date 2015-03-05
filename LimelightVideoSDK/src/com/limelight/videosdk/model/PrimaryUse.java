package com.limelight.videosdk.model;

/**
 * Primary use of an encoding.
 * @author kanchan
 *
 */
public enum PrimaryUse {
    
    /**
     * No primary use has been set.
     */
    All,
    /**
     * The primary use is intended for flash.
     */
    Flash,
    /**
     * The primary use is intended for mobile 3gp.
     */
    Mobile3gp,
    
    /**
     * The primary use is intended for mobile H.264.
     */
    MobileH264,
    /**
     * The primary use is intended for HLS.
     */
    HttpLiveStreaming,
    /**
     * The primary use is intended for Widevine online.
     */
    Widevine,
    /**
     * The primary use is intended for Widevine offline.
     */
    WidevineOffline
}
