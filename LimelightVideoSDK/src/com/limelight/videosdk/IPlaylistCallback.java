package com.limelight.videosdk;

import java.util.ArrayList;
import com.limelight.videosdk.model.Media;

/**
 * This interface is the callback for playlist.
 *
 */
public interface IPlaylistCallback {
    /**
     * This callback return the media in the selected channel to the caller.
     * @param playlist
     */
    void getChannelPlaylist(ArrayList<Media> playlist);
}
