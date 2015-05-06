package com.limelight.videosdk;
/**
 * This acts as an interface between PlayerFragment and VideoPlayerView.
 * The VideoPlayerView gets invoked for controller play, pause and seek events.
 * The VideoPlayerView will have the IMediaControllerCallback object using which it will pass the notification to PlayerSupportFragment.
 * @author kanchan
 *
 */
interface IMediaControllerCallback {
    /**
     * This method passes the controller Play notification from VideoPlayerView to Player
     * @param position
     */
    void onMediaControllerPlay(long position);
    /**
     * This method passes the controller Pause notification from VideoPlayerView to Player.
     * @param position
     */
    void onMediaControllerPause(long position);
    /**
     * This method passes the controller Seek notification from VideoPlayerView to Player
     * @param beforePosition
     * @param aftrerPosition
     */
    void onMediaControllerSeek(long beforePosition,long afterPosition);
    /**
     * This method passes the play completed notification from VideoPlayerView to Player
     */
    void onMediaControllerComplete();
}
