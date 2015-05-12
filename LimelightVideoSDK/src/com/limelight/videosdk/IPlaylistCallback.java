package com.limelight.videosdk;

import java.util.ArrayList;

import com.limelight.videosdk.model.Media;

/**
 * This interface is the callback for playlist.
 * @author kanchan
 *
 */
public interface IPlaylistCallback {
void getChannelPlaylist(ArrayList<Media> playlist);
}
