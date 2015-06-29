Feature: Play list feature test
     
    Scenario: I should be able to add and remove selected media in play-list
     Given the application has launched with following configuration in SETTINGS tab -
       |  name             |  value                            |
       | Organization ID   | a851c50193064ed6be08c7e75f8f4910  |
       | Access Key        | 6QDyPljwRS8L2w7Q7AnRo3sYIoQ=      |
       | Secret Key        | +d62cBI73hxWcRPpput4RR7a8v8=      |
     
     When I add following media in play list from ALL MEDIA tab -
       | media name      |
       | Adobe           |
       | IP MAN          |
       | The Dark Knight |
       | Inception       |
     Then following media from ALL MEDIA tab gets added in play list of PLAYER tab -
       | media name      |
       | Adobe           |
       | IP MAN          |
       | The Dark Knight |
       | Inception       |
     When I remove following media in play list from PLAYER tab -
       | media name      |
       | Adobe           |
       | IP MAN          |
     Then following media from ALL MEDIA tab gets removed in play list of PLAYER tab -
       | media name      |
       | Adobe           |
       | IP MAN          |
      And close the application
     
    Scenario: I should be able to add and remove all media in play-list
     Given the application has launched with following configuration in SETTINGS tab -
       |  name             |  value                            |
       | Organization ID   | a851c50193064ed6be08c7e75f8f4910  |
       | Access Key        | 6QDyPljwRS8L2w7Q7AnRo3sYIoQ=      |
       | Secret Key        | +d62cBI73hxWcRPpput4RR7a8v8=      |
     
     When I select News as value for channel in ALL CHANNELS tab
      And I add all media in play list from CURRENT tab 
     Then all media from MEDIA tab gets added in play list of PLAYER tab
     When I remove all media in play list from PLAYER tab 
     Then all media from MEDIA tab gets removed in play list of PLAYER tab
      And close the application
     
    Scenario: I should be able to add media of selected channel that are displayed in media tab to play-list.
     Given the application has launched with following configuration in SETTINGS tab -
       |  name             |  value                            |
       | Organization ID   | a851c50193064ed6be08c7e75f8f4910  |
       | Access Key        | 6QDyPljwRS8L2w7Q7AnRo3sYIoQ=      |
       | Secret Key        | +d62cBI73hxWcRPpput4RR7a8v8=      |
     When I select News as value for channel in ALL CHANNELS tab
      And I add following media in play list from MEDIA tab -
       | media name    |
       | Star Wars     |
       | Nadals Quest  |
     Then following media from ALL MEDIA tab gets added in play list of PLAYER tab -
       | media name    |
       | Star Wars     |
       | Nadals Quest  |
      And close the application

    Scenario: After adding some media to play list, play button and media id text box should not visible but play-list should be visible
     Given the application has launched with following configuration in SETTINGS tab -
       |  name             |  value                            |
       | Organization ID   | a851c50193064ed6be08c7e75f8f4910  |
       | Access Key        | 6QDyPljwRS8L2w7Q7AnRo3sYIoQ=      |
       | Secret Key        | +d62cBI73hxWcRPpput4RR7a8v8=      |
     When I add following media in play list from ALL MEDIA tab -
       | media name      |
       | Adobe           |
       | IP MAN          |
     Then the PLAYER tab should not have following controls -
       | controls           |
       | play-button        |
       | media-id-text-box  |
      And the PLAYER tab should have following controls -
       | controls           |
       | playlist-container |
      And close the application
     
    Scenario: I should be able to play media from play-list with autoplay on and off
     Given the application has launched with following configuration in SETTINGS tab -
       |  name             |  value                            |
       | Organization ID   | a851c50193064ed6be08c7e75f8f4910  |
       | Access Key        | 6QDyPljwRS8L2w7Q7AnRo3sYIoQ=      |
       | Secret Key        | +d62cBI73hxWcRPpput4RR7a8v8=      |
     When I add following media in play list from ALL MEDIA tab -
       | media name         |
       | Flash_Only         |
       | Flash_HLS          |
       | Widewine_Online_SD |
       | Widewine_Offline   |
     When I play the "Flash_Only" video from play list with auto playlist on
     Then player should play the playback from remote to duration 00:00 in play state
     When I play the "Flash_HLS" video from play list with auto playlist on
     Then player should play the playback from remote to duration 00:00 in play state
     When I play the "Widewine_Online_SD" video from play list with auto playlist off
     Then player should play the playback from remote to duration 00:00 in play state
     When I play the "Widewine_Offline" video from play list with auto playlist off
     Then player should play the playback from remote to duration 00:00 in play state
      And close the application

    Scenario: The media should be played sequentially when autoplay is checked.
     Given the application has launched with following configuration in SETTINGS tab -
       |  name             |  value                            |
       | Organization ID   | a851c50193064ed6be08c7e75f8f4910  |
       | Access Key        | 6QDyPljwRS8L2w7Q7AnRo3sYIoQ=      |
       | Secret Key        | +d62cBI73hxWcRPpput4RR7a8v8=      |
     When I add following media in play list from ALL MEDIA tab -
       | media name      |
       | Adobe           |
       | IP MAN          |
       | The Dark Knight |
       | Inception       |
     When I play the "Adobe" video from play list with auto playlist on
      And I completed the "Adobe" video from PLAY-LIST with "automatic" encoding
     Then player should play the IP MAN media from play-list at duration 00:00 in play state
      And close the application
     
    Scenario: The media should be played sequentially from current selected item when autoplay is checked.
     Given the application has launched with following configuration in SETTINGS tab -
       |  name             |  value                            |
       | Organization ID   | a851c50193064ed6be08c7e75f8f4910  |
       | Access Key        | 6QDyPljwRS8L2w7Q7AnRo3sYIoQ=      |
       | Secret Key        | +d62cBI73hxWcRPpput4RR7a8v8=      |
     When I add following media in play list from ALL MEDIA tab -
       | media name      |
       | Adobe           |
       | IP MAN          |
       | The Dark Knight |
       | Inception       |
     When I play the "IP MAN" video from play list with auto playlist on
      And I completed the "IP MAN" video from PLAY-LIST with "automatic" encoding
     Then player should play the The Dark Knight media from play-list at duration 00:00 in play state
      And close the application
     
    Scenario: The media should play normally after seeking and on completion should play the next item in list.
     Given the application has launched with following configuration in SETTINGS tab -
       |  name             |  value                            |
       | Organization ID   | a851c50193064ed6be08c7e75f8f4910  |
       | Access Key        | 6QDyPljwRS8L2w7Q7AnRo3sYIoQ=      |
       | Secret Key        | +d62cBI73hxWcRPpput4RR7a8v8=      |
     When I add following media in play list from ALL MEDIA tab -
       | media name      |
       | Adobe           |
       | IP MAN          |
       | The Dark Knight |
       | Inception       |
     When I play the "IP MAN" video from play list with auto playlist on
      And I seek-2:00 the "IP MAN" video from PLAY-LIST with "automatic" encoding
     Then player should seek the playback from remote to duration 2:00 in play state
     When I completed the "IP MAN" video from PLAY-LIST with "automatic" encoding
     Then player should play the The Dark Knight media from play-list at duration 00:00 in play state
      And close the application
     
    Scenario: Can add media to playlist while playing a media from playlist
     Given the application has launched with following configuration in SETTINGS tab -
       |  name             |  value                            |
       | Organization ID   | a851c50193064ed6be08c7e75f8f4910  |
       | Access Key        | 6QDyPljwRS8L2w7Q7AnRo3sYIoQ=      |
       | Secret Key        | +d62cBI73hxWcRPpput4RR7a8v8=      |
     When I add following media in play list from ALL MEDIA tab -
       | media name      |
       | Adobe           |
       | IP MAN          |
     When I play the "IP MAN" video from play list with auto playlist on
     When I add following media in play list from ALL MEDIA tab -
       | media name      |
       | The Dark Knight |
       | Inception       |
     Then following media from ALL MEDIA tab gets added in play list of PLAYER tab -
       | media name      |
       | The Dark Knight |
       | Inception       |
      And close the application
     
    Scenario: Remove from playlist when the media is playing and auto play list is off, the media should be removed and playback should stop
     Given the application has launched with following configuration in SETTINGS tab -
       |  name             |  value                            |
       | Organization ID   | a851c50193064ed6be08c7e75f8f4910  |
       | Access Key        | 6QDyPljwRS8L2w7Q7AnRo3sYIoQ=      |
       | Secret Key        | +d62cBI73hxWcRPpput4RR7a8v8=      |
     When I add following media in play list from ALL MEDIA tab -
       | media name      |
       | Adobe           |
       | IP MAN          |
       | The Dark Knight |
       | Inception       |
     When I play the "IP MAN" video from play list with auto playlist off
     Then player should play the playback from remote to duration 00:00 in play state
     When I remove following media in play list from PLAYER tab -
       | media name    |
       | IP MAN        |
     Then following media from ALL MEDIA tab gets removed in play list of PLAYER tab -
       | media name    |
       | IP MAN        |
      And the player should not have following controls -
       | controls              |
       | player-pause-button   |
       | player-rewind-button  |
       | player-forward-button |
       | player-seekbar        |
       | player-elapsed-time   |
       | player-video-duration |
      And close the application
      
    Scenario: Remove from playlist when media playback completed and auto play list is off, the media should be removed
     Given the application has launched with following configuration in SETTINGS tab -
       |  name             |  value                            |
       | Organization ID   | a851c50193064ed6be08c7e75f8f4910  |
       | Access Key        | 6QDyPljwRS8L2w7Q7AnRo3sYIoQ=      |
       | Secret Key        | +d62cBI73hxWcRPpput4RR7a8v8=      |
     When I add following media in play list from ALL MEDIA tab -
       | media name      |
       | Adobe           |
       | IP MAN          |
       | The Dark Knight |
       | Inception       |
     When I play the "IP MAN" video from play list with auto playlist off
     Then player should play the playback from remote to duration 00:00 in play state
     When I completed the "IP MAN" video from PLAY-LIST with "automatic" encoding
      And I remove following media in play list from PLAYER tab -
       | media name    |
       | IP MAN        |
     Then following media from ALL MEDIA tab gets removed in play list of PLAYER tab -
       | media name    |
       | IP MAN        |
      And close the application
      
    Scenario: On application relaunched the playlist should be cleared from player tab.
     Given the application has launched with following configuration in SETTINGS tab -
       |  name             |  value                            |
       | Organization ID   | a851c50193064ed6be08c7e75f8f4910  |
       | Access Key        | 6QDyPljwRS8L2w7Q7AnRo3sYIoQ=      |
       | Secret Key        | +d62cBI73hxWcRPpput4RR7a8v8=      |
     When I add following media in play list from ALL MEDIA tab -
       | media name      |
       | Adobe           |
       | IP MAN          |
       | The Dark Knight |
      And I play the "IP MAN" video from play list with auto playlist off
     Then close the application
     When I apply app-icon press on the device
     Then all media from MEDIA tab gets removed in play list of PLAYER tab
     And close the application
     
    Scenario: The playlist should be properly visible on the screen after orientation is changed.
     Given the application has launched with following configuration in SETTINGS tab -
       |  name             |  value                            |
       | Organization ID   | a851c50193064ed6be08c7e75f8f4910  |
       | Access Key        | 6QDyPljwRS8L2w7Q7AnRo3sYIoQ=      |
       | Secret Key        | +d62cBI73hxWcRPpput4RR7a8v8=      |
     When I add following media in play list from ALL MEDIA tab -
       | media name      |
       | Adobe           |
       | IP MAN          |
       | The Dark Knight |
       | Inception       |
      And I play the "IP MAN" video from play list with auto playlist on
      And I apply screen orientation on the device
     Then following media from ALL MEDIA tab gets added in play list of CURRENT tab -
       | media name      |
       | Adobe           |
       | IP MAN          |
       | The Dark Knight |
       | Inception       |
     When I apply screen orientation on the device
     Then following media from ALL MEDIA tab gets added in play list of PLAYER tab -
       | media name      |
       | Adobe           |
       | IP MAN          |
       | The Dark Knight |
       | Inception       |
      And close the application
     