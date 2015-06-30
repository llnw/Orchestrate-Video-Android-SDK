Feature: Checking the full screen feature on the player

    Scenario: Player should able to switch to full screen and switch back to normal screen, also able to play the local media
      Given the application has launched
      When I play the "local" video from Videos/Download/testLocalMedia.mp4 with "local" encoding
       And I apply full-screen ON on the player
      Then player should play the playback from file to duration xx:xx in play state with full-screen mode
      When I apply full-screen OFF on the player
      Then player should play the playback from file to duration xx:xx in play state with normal-screen mode

    Scenario: Player should able to switch to full screen and switch back to normal screen, also able to play the remote media
      Given the application has launched
      When I play the "Flash_HLS" video from ALL MEDIA with "HttpLiveStreaming 96 X409kbps 264X640" encoding
       And I apply full-screen ON on the player
      Then player should play the playback from remote to duration 00:00 in play state with full-screen mode
      When I apply full-screen OFF on the player
      Then player should play the playback from remote to duration 00:00 in play state with normal-screen mode

    Scenario: After pause a local video, switch on to full screen, player should switch to full screen and video should be in paused state.
      Given the application has launched
      When I play the "local" video from Videos/Download/testLocalMedia.mp4 with "local" encoding
       And I pause the "local" video from Videos/Download/testLocalMedia.mp4 with "local" encoding
       And I apply full-screen ON on the player
      Then player should pause the playback from file to duration xx:xx in pause state with full-screen mode
      When I apply full-screen OFF on the player
      Then player should pause the playback from file to duration xx:xx in pause state with normal-screen mode

    Scenario: After pause a remote video, switch on to full screen, player should switch to full screen and video should be in paused state.
      Given the application has launched
      When I play the "remote" video from 936288a1457b4bb2acbdb18ed5cd58c1 with "Flash 128 X472kbps 270X480" encoding
       And I pause the "remote" video from 936288a1457b4bb2acbdb18ed5cd58c1 with "Flash 128 X472kbps 270X480" encoding
       And I apply full-screen ON on the player
      Then player should pause the playback from remote to duration 00:00 in pause state with full-screen mode
      When I apply full-screen OFF on the player
      Then player should pause the playback from remote to duration 00:00 in pause state with normal-screen mode

    Scenario: I can pause and resume a media when it is playing in full screen mode
      Given the application has launched
      When I play the "Flash_HLS" video from ALL MEDIA with "HttpLiveStreaming 96 X409kbps 264X640" encoding
       And I apply full-screen ON on the player
       And I pause the "Flash_HLS" video from ALL MEDIA with "HttpLiveStreaming 96 X409kbps 264X640" encoding
      Then player should pause the playback from remote to duration 00:00 in pause state with full-screen mode
      When I resume the "Flash_HLS" video from ALL MEDIA with "HttpLiveStreaming 96 X409kbps 264X640" encoding
      Then player should resume the playback from remote to duration 00:00 in play state with full-screen mode
      When I apply full-screen OFF on the player

    Scenario: Player should be able to seek properly while in full screen mode
      Given the application has launched
      When I play the "local" video from Videos/Download/testLocalMedia.mp4 with "local" encoding
       And I apply full-screen ON on the player
       And I pause the "local" video from Videos/Download/testLocalMedia.mp4 with "local" encoding
       And I seek-2:00 the "local" video from Videos/Download/testLocalMedia.mp4 with "local" encoding
      Then player should seek the playback from file to duration 2:00 in pause state with full-screen mode
      When I seek-4:00 the "local" video from Videos/Download/testLocalMedia.mp4 with "local" encoding
       And I resume the "local" video from Videos/Download/testLocalMedia.mp4 with "local" encoding
       And I seek-2:00 the "local" video from Videos/Download/testLocalMedia.mp4 with "local" encoding
       And I apply full-screen OFF on the player
      Then player should seek the playback from file to duration 2:00 in play state with normal-screen mode

    Scenario: Check the player controls while in full screen mode
      Given the application has launched
      When I play the "local" video from Videos/Download/testLocalMedia.mp4 with "local" encoding
       And I apply full-screen ON on the player
      Then  the player should have following controls -
         | controls              |
         | player-pause-button   |
         | player-rewind-button  |
         | player-forward-button |
         | player-seekbar        |
         | player-elapsed-time   |
         | player-video-duration |
         | full-screen-button    |
      When I apply full-screen OFF on the player

    Scenario: While playing a media in full screen mode, if we press home button, then on restore it should restore in full screen mode
      Given the application has launched
      When I play the "local" video from Videos/Download/testLocalMedia.mp4 with "local" encoding
       And I apply full-screen ON on the player
       And I apply home-button press on the application
       And I apply app-icon press on the device
      Then player should play the playback from file to duration xx:xx in play state with full-screen mode
      When I apply full-screen OFF on the player