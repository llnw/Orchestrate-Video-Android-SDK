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
     * Method will be implemented in application activity and it will be used to send messages from SDK to application.
     * @param messageType The type of message. Message type can be error or player status or download progress.
     * @param value Extra code, specific to the error or download percent finished or player state. 
     * @param message Any message that has to be sent from SDK to Application.
     */
    void playerMessage(int messageType,int value,String message);

    /**
     * Method will be implemented in activity.
     * This callback method is called when player is prepared to play.
     * @param control {@link IPlayerControl}
     */
    void playerPrepared(IPlayerControl control);
}