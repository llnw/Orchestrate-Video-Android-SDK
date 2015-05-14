package com.limelight.videosdk.model;

/**
 * The types of deliveries.
 * @author Nagaraju
 *
 */
public enum DeliveryType {

    /**
     *  No type is associated with the delivery.
     */
    LVKDeliveryTypeNone,
    /**
     * The delivery type is Flash
     */
    LVKDeliveryTypeFlash,
    /**
     * The delivery type is Mobile H.264
     */
    LVKDeliveryTypeMobileH264,
    /**
     * The delivery type is HTTP Live Streaming (HLS)
     */
    LVKDeliveryTypeHTTPLiveStreaming,
    /**
     * The delivery type is Widevine
     */
    LVKDeliveryTypeWidevine,
    /**
     * The delivery type is Widevine Offline
     */
    LVKDeliveryTypeWidevineOffline
}
