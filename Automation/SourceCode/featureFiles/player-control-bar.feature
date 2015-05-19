Feature: Player Control Bar

    Scenario: The player should display the elapsed time.      
        Given the application has launched with following configuration in SETTINGS tab -
         |  name             |  value                            |
         | Organization ID   | a851c50193064ed6be08c7e75f8f4910  |
         | Access Key        | 6QDyPljwRS8L2w7Q7AnRo3sYIoQ=      |
         | Secret Key        | +d62cBI73hxWcRPpput4RR7a8v8=      |
        When I play the "Flash_HLS" video from ALL MEDIA with "automatic" encoding
        Then  the player should have following controls -
            | controls             |
            | player-elapsed-time  |
        And exit from the application
      
    Scenario: The player should display the video duration for VOD videos.            
        Given the application has launched with following configuration in SETTINGS tab -
         |  name             |  value                            |
         | Organization ID   | a851c50193064ed6be08c7e75f8f4910  |
         | Access Key        | 6QDyPljwRS8L2w7Q7AnRo3sYIoQ=      |
         | Secret Key        | +d62cBI73hxWcRPpput4RR7a8v8=      |
        When I play the "Flash_HLS" video from ALL MEDIA with "automatic" encoding
        Then  the player should have following controls -
            | controls              |
            | player-video-duration |
        And exit from the application

    Scenario: Player should display the basic controls like: play button, forward button, backward button, seek bar.  
        Given the application has launched with following configuration in SETTINGS tab -
         |  name             |  value                            |
         | Organization ID   | a851c50193064ed6be08c7e75f8f4910  |
         | Access Key        | 6QDyPljwRS8L2w7Q7AnRo3sYIoQ=      |
         | Secret Key        | +d62cBI73hxWcRPpput4RR7a8v8=      |
        When I play the "Flash_HLS" video from ALL MEDIA with "automatic" encoding
        Then  the player should have following controls -
            | controls              |
            | player-pause-button   |
            | player-rewind-button  |
            | player-forward-button |
            | player-seekbar        |
            | player-elapsed-time   |
            | player-video-duration |
        And exit from the application

    Scenario: On relaunch the application, video playback should remain in play/pause state as it was when application moved to background.
      Given the application has launched with following configuration in SETTINGS tab -
     |  name             |  value                            |
     | Organization ID   | a851c50193064ed6be08c7e75f8f4910  |
     | Access Key        | 6QDyPljwRS8L2w7Q7AnRo3sYIoQ=      |
     | Secret Key        | +d62cBI73hxWcRPpput4RR7a8v8=      |
      When I play the "local" video from Downloads/testLocalMedia.mp4 with "local" encoding
       And I seek-2:00 the "local" video from Downloads/testLocalMedia.mp4 with "local" encoding
       And I apply home-button press on the application
       And I apply app-icon press on the device
      Then player should continue-playing the playback from file to duration xx:xx in play state
      When I pause the "local" video from Downloads/testLocalMedia.mp4 with "local" encoding
       And I apply home-button press on the application
       And I apply app-icon press on the device
      Then player should remain-pause the playback from file to duration xx:xx in pause state

    Scenario: The video player should display the entire video and as per the aspect ratio settings in all orientations.
      Given the application has launched with following configuration in SETTINGS tab -
     |  name             |  value                            |
     | Organization ID   | a851c50193064ed6be08c7e75f8f4910  |
     | Access Key        | 6QDyPljwRS8L2w7Q7AnRo3sYIoQ=      |
     | Secret Key        | +d62cBI73hxWcRPpput4RR7a8v8=      |
      When I play the "local" video from Downloads/testLocalMedia.mp4 with "local" encoding
       And I seek-2:00 the "local" video from Downloads/testLocalMedia.mp4 with "local" encoding
       And I apply screen orientation on the device
      Then player should play the playback from file to duration 00:00 in play state
      When I apply screen orientation on the device
      Then player should play the playback from file to duration 00:00 in play state
      When I pause the "local" video from Downloads/testLocalMedia.mp4 with "local" encoding
       And I seek-2:00 the "local" video from Downloads/testLocalMedia.mp4 with "local" encoding
       And I apply screen orientation on the device
      Then player should pause the playback from file to duration 00:00 in pause state
      When I resume the "local" video from Downloads/testLocalMedia.mp4 with "local" encoding
       And I apply screen orientation on the device
      Then player should play the playback from file to duration 00:00 in play state
       And exit from the application