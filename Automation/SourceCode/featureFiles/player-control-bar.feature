Feature: Player Control Bar

    Scenario: The player should display the elapsed time.      
        Given the application has launched
        When I play the "Flash_HLS" video from ALL MEDIA with "automatic" encoding
        Then  the player should have following controls -
            | controls             |
            | player-elapsed-time  |

    Scenario: The player should display the video duration for VOD videos.            
        Given the application has launched
        When I play the "Flash_HLS" video from ALL MEDIA with "automatic" encoding
        Then  the player should have following controls -
            | controls              |
            | player-video-duration |

    Scenario: Player Control-bar support with Streaming Video Content in normal and device rotation
        Given the application has launched
        When I play the "Flash_HLS" video from ALL MEDIA with "automatic" encoding
        Then  the player should have following controls -
            | controls              |
            | player-pause-button   |
            | player-rewind-button  |
            | player-forward-button |
            | player-seekbar        |
            | player-elapsed-time   |
            | player-video-duration |
        When I apply screen orientation on the device
        Then  the player should have following controls -
            | controls              |
            | player-pause-button   |
            | player-rewind-button  |
            | player-forward-button |
            | player-seekbar        |
            | player-elapsed-time   |
            | player-video-duration |
        When I apply screen orientation on the device
        Then  the player should have following controls -
            | controls              |
            | player-pause-button   |
            | player-rewind-button  |
            | player-forward-button |
            | player-seekbar        |
            | player-elapsed-time   |
            | player-video-duration |
        
    Scenario: Player Control-bar support with Streaming Video Content and Home button Handling
        Given the application has launched
        When I play the "Flash_HLS" video from ALL MEDIA with "automatic" encoding
         And I apply home-button press on the application
         And I apply app-icon press on the device
        Then  the player should have following controls -
            | controls              |
            | player-pause-button   |
            | player-rewind-button  |
            | player-forward-button |
            | player-seekbar        |
            | player-elapsed-time   |
            | player-video-duration |
        
    Scenario: Player Control-bar support with Local Video Content in normal and device rotation
        Given the application has launched
        When I play the "local" video from Videos/Download/testLocalMedia.mp4 with "local" encoding
        Then  the player should have following controls -
            | controls              |
            | player-pause-button   |
            | player-rewind-button  |
            | player-forward-button |
            | player-seekbar        |
            | player-elapsed-time   |
            | player-video-duration |
        When I apply screen orientation on the device
        Then  the player should have following controls -
            | controls              |
            | player-pause-button   |
            | player-rewind-button  |
            | player-forward-button |
            | player-seekbar        |
            | player-elapsed-time   |
            | player-video-duration |
        When I apply screen orientation on the device
        Then  the player should have following controls -
            | controls              |
            | player-pause-button   |
            | player-rewind-button  |
            | player-forward-button |
            | player-seekbar        |
            | player-elapsed-time   |
            | player-video-duration |
            
    Scenario: Player Control-bar support with Local Video Content and Home button Handling
        Given the application has launched
        When I play the "local" video from Videos/Download/testLocalMedia.mp4 with "local" encoding
         And I apply home-button press on the application
         And I apply app-icon press on the device
        Then  the player should have following controls -
            | controls              |
            | player-pause-button   |
            | player-rewind-button  |
            | player-forward-button |
            | player-seekbar        |
            | player-elapsed-time   |
            | player-video-duration |
        
    Scenario: Player Control-bar support with Widevine Offline Video Content in normal and device rotation
        Given the application has launched
        When I play the "Widewine_Offline_SD_HD" video from ALL MEDIA with "WidevineOffline 10 X50kbps 240X320" encoding
        Then  the player should have following controls -
            | controls              |
            | player-pause-button   |
            | player-rewind-button  |
            | player-forward-button |
            | player-seekbar        |
            | player-elapsed-time   |
            | player-video-duration |
        When I apply screen orientation on the device
        Then  the player should have following controls -
            | controls              |
            | player-pause-button   |
            | player-rewind-button  |
            | player-forward-button |
            | player-seekbar        |
            | player-elapsed-time   |
            | player-video-duration |
        When I apply screen orientation on the device
        Then  the player should have following controls -
            | controls              |
            | player-pause-button   |
            | player-rewind-button  |
            | player-forward-button |
            | player-seekbar        |
            | player-elapsed-time   |
            | player-video-duration |
        
    Scenario: On relaunch the application, video playback should remain in play/pause state as it was when application moved to background.
        Given the application has launched
        When I play the "local" video from Videos/Download/testLocalMedia.mp4 with "local" encoding
         And I seek-2:00 the "local" video from Videos/Download/testLocalMedia.mp4 with "local" encoding
         And I apply home-button press on the application
         And I apply app-icon press on the device
        Then player should continue-playing the playback from file to duration xx:xx in play state
        When I pause the "local" video from Videos/Download/testLocalMedia.mp4 with "local" encoding
         And I apply home-button press on the application
         And I apply app-icon press on the device
        Then player should remain-pause the playback from file to duration xx:xx in pause state

    Scenario: The video player should display the entire video and as per the aspect ratio settings in all orientations.
        Given the application has launched
        When I play the "local" video from Videos/Download/testLocalMedia.mp4 with "local" encoding
         And I seek-2:00 the "local" video from Videos/Download/testLocalMedia.mp4 with "local" encoding
         And I apply screen orientation on the device
        Then player should play the playback from file to duration 00:00 in play state
        When I apply screen orientation on the device
        Then player should play the playback from file to duration 00:00 in play state
        When I pause the "local" video from Videos/Download/testLocalMedia.mp4 with "local" encoding
         And I seek-2:00 the "local" video from Videos/Download/testLocalMedia.mp4 with "local" encoding
         And I apply screen orientation on the device
        Then player should pause the playback from file to duration 00:00 in pause state
        When I resume the "local" video from Videos/Download/testLocalMedia.mp4 with "local" encoding
         And I apply screen orientation on the device
        Then player should play the playback from file to duration 00:00 in play state
         And exit from the application