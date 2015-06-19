Feature: Channel Play list feature test

    Scenario: I should be able to add all media of a channel by clicking the play button beside the channel, and no delete button should be there
     Given the application has launched with following configuration in SETTINGS tab -
       |  name             |  value                            |
       | Organization ID   | a851c50193064ed6be08c7e75f8f4910  |
       | Access Key        | 6QDyPljwRS8L2w7Q7AnRo3sYIoQ=      |
       | Secret Key        | +d62cBI73hxWcRPpput4RR7a8v8=      |
     When I add following channel in play list from ALL CHANNELS tab -
       | channel name  |
       | TV            |
     Then all media from MEDIA tab gets added in play list of PLAYER tab
      And the play-list should not have following controls -
       | controls      |
       | delete-button |
      And close the application

    Scenario: Playlist should cleared out when new channel added and also when new media added
     Given the application has launched with following configuration in SETTINGS tab -
       |  name             |  value                            |
       | Organization ID   | a851c50193064ed6be08c7e75f8f4910  |
       | Access Key        | 6QDyPljwRS8L2w7Q7AnRo3sYIoQ=      |
       | Secret Key        | +d62cBI73hxWcRPpput4RR7a8v8=      |
     When I add following channel in play list from ALL CHANNELS tab -
       | channel name  |
       | TV            |
      And I add following channel in play list from ALL CHANNELS tab -
       | channel name  |
       | News          |
     Then following media from MEDIA tab gets removed in play list of PLAYER tab -
       | media name      |
       | IP MAN          |
       | Jack Reacher    |
       | The Dark Knight |
     When I add following media in play list from ALL MEDIA tab -
       | media name      |
       | Adobe           |
       | IP MAN          |
     Then following media from MEDIA tab gets removed in play list of PLAYER tab -
       | media name      |
       | Salman News     |
       | Star Wars       |
       | Nadals Quest    |
      And close the application

    Scenario: I should be able to play any media from channel playlist
     Given the application has launched with following configuration in SETTINGS tab -
       |  name             |  value                            |
       | Organization ID   | a851c50193064ed6be08c7e75f8f4910  |
       | Access Key        | 6QDyPljwRS8L2w7Q7AnRo3sYIoQ=      |
       | Secret Key        | +d62cBI73hxWcRPpput4RR7a8v8=      |
     When I add following channel in play list from ALL CHANNELS tab -
       | channel name  |
       | TV            |
     When I play the "The Dark Knight" video from play list with auto playlist off
     Then player should play the playback from remote to duration 00:00 in play state
      And close the application

    Scenario: I should be able to use the previous and next button to play the previous and next media
     Given the application has launched with following configuration in SETTINGS tab -
       |  name             |  value                            |
       | Organization ID   | a851c50193064ed6be08c7e75f8f4910  |
       | Access Key        | 6QDyPljwRS8L2w7Q7AnRo3sYIoQ=      |
       | Secret Key        | +d62cBI73hxWcRPpput4RR7a8v8=      |
     When I add following channel in play list from ALL CHANNELS tab -
       | channel name  |
       | News          |
     When I apply player-next-button click on the player
     Then player should play the Star Wars media from play-list at duration 00:00 in play state
     When I apply player-previous-button click on the player
     Then player should play the Salman News media from play-list at duration 00:00 in play state
      And close the application

    Scenario: I should not be able to resume or play a media if it is not in playlist
     Given the application has launched with following configuration in SETTINGS tab -
       |  name             |  value                            |
       | Organization ID   | a851c50193064ed6be08c7e75f8f4910  |
       | Access Key        | 6QDyPljwRS8L2w7Q7AnRo3sYIoQ=      |
       | Secret Key        | +d62cBI73hxWcRPpput4RR7a8v8=      |
     When I add following channel in play list from ALL CHANNELS tab -
       | channel name  |
       | TV            |
     When I play the "The Dark Knight" video from play list with auto playlist off
      And I pause the "The Dark Knight" video from PLAY-LIST with "automatic" encoding
      And I add following media in play list from ALL MEDIA tab -
       | media name      |
       | Adobe           |
      And I select the PLAYER tab
      And I resume the "The Dark Knight" video from PLAY-LIST with "automatic" encoding
      Then player should not play the playback from remote to duration 00:00 in play state
      And close the application