package com.limelight.videosdk;

/**
 * This is the callback to be sent to activity from Player. This is the 
 * communication mechanism between activity in application and player in SDK.
 * Once Activity holding the player fragment gets created, callback is sent to 
 * activity. Activity can then use playerAttached() to start interacting with Player.
 * The rationale behind this approach is that customer application should start 
 * using Player only after activity holding the player fragment gets created.
 * Player can also send error information or some status message back to the 
 * activity in Application.Activity then can use these informations.
 * @author kanchan
 * 
 */
public interface IPlayerCallback {
    /**
     * Callback sent from Player when player is attached to activity.
     * Method will be implemented in activity.IPlayerControl will be used to control Player.
     * @param control {@link IPlayerControl}
     */
    void playerAttached(IPlayerControl control);
    /**
     * Method will be implemented in activity.
     * Player will send error information or some status message.
     * Error information may be like type of error that has occurred.
     * Extra can have value like progress information in case of downloader.
     * @param what the type of error/status that has occurred
     * @param extra an extra code, specific to the error. Typically implementation dependent. 
     * @param msg any message that has to be sent from SDK to Application.
     */
    void playerMessage(int what,int extra,String msg);

    /**
     * Method will be implemented in activity.
     * This callback method is called when player is prepared to play.
     * @param control {@link IPlayerControl}
     */
    void playerPrepared(IPlayerControl control);
}