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
     * @param data media to play<br>
     * @param contentSrvc ContentService which fetched this media.<br>
     */
    void play(String data, ContentService contentSrvc);
    /**
     * To play all the media in a channel. Channel acts as a playlist.<br>
     * @param channelId Channel id to play.<br>
     * @param contentService ContentService which fetched this channelid<br>
     * @param playlist playlist callback<br>
     */
    void playChannel(String channelId, ContentService contentService,IPlaylistCallback playlist);
    /**
     * This method programmatically pause the player.
     */
    void pause();
    /**
     * This method programmatically stops the player.
     */
    void stop();
    /**
     * To set the autoplay for playlist.<br>
     * @param isAutoPlay true if autoplay playlist else false<br>
     */
    void setAutoPlay(boolean isAutoPlay);/**
     * To get the current play position in playlist.<br>
     * @return current play position<br>
     */
    int getPlaylistPosition();
    /**
     * To play a particular media in a channel. Channel acts as a playlist.<br>
     * @param position position in  playlist<br>
     */
    void playInPlaylist(int position);
}
