package com.limelight.videosdk;

/**
 * This interface is the control to the Player.<br>
 * A child object of this interface is supplied to the Activity 
 * which requested for the Player.
 * It is used to control the player like playing,pausing etc.
 * It also sets the video content for player.
 * @author kanchan
 *
 */
public interface IPlayerControl {
    /**
     * To play player by program.<br>
     * Call play with media id to fetch the encoding, get the delivery and play the media.<br>
     * Call play with encoding url to play the media for the selected encoding.
     */
    void play(String data);
    /**
     * This method programmatically pause the player.
     */
    void pause();
    /**
     * This method programmatically resume the player.
     */
    void resume();
    /**
     * This method programmatically stops the player.
     */
    void stop();
    /**
     * To programmatically go to next in player.
     */
    void next();
    /**
     * To programmatically go to previous in player.
     */
    void previous();
}
