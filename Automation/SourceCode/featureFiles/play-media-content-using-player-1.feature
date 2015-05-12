Feature: Play, Pause, Move Forward, Move Backward, Resume and Seek Media Content Using Player

    Scenario: I should be able to play, pause, move forward, move backward, resume Widevine online encoded video
     Given the application has launched with following configuration in SETTINGS tab -
     |  name             |  value                            |
     | Organization ID   | a851c50193064ed6be08c7e75f8f4910  |
     | Access Key        | 6QDyPljwRS8L2w7Q7AnRo3sYIoQ=      |
     | Secret Key        | +d62cBI73hxWcRPpput4RR7a8v8=      |
      When I play the "How To Film" video from ALL MEDIA with "Widevine 128 X2400kbps 360X640" encoding
      Then player should play the playback from remote to duration 00:00 in play state
      When I pause the "How To Film" video from ALL MEDIA with "Widevine 128 X2400kbps 360X640" encoding
      Then player should pause the playback from remote to duration 00:00 in pause state
      When I forwarded the "How To Film" video from ALL MEDIA with "Widevine 128 X2400kbps 360X640" encoding
      Then player should forwarded the playback from remote to duration 00:00 in pause state
      When I reversed the "How To Film" video from ALL MEDIA with "Widevine 128 X2400kbps 360X640" encoding
      Then player should reversed the playback from remote to duration 00:00 in pause state
      When I resume the "How To Film" video from ALL MEDIA with "Widevine 128 X2400kbps 360X640" encoding
      Then player should resume the playback from remote to duration 00:00 in play state

    Scenario: I should be able to play, pause, move forward, move backward, resume Widevine offline encoded video
      Given the application has launched with following configuration in SETTINGS tab -
     |  name             |  value                            |
     | Organization ID   | a851c50193064ed6be08c7e75f8f4910  |
     | Access Key        | 6QDyPljwRS8L2w7Q7AnRo3sYIoQ=      |
     | Secret Key        | +d62cBI73hxWcRPpput4RR7a8v8=      |
      When I play the "Football Commercial" video from ALL MEDIA with "WidevineOffline 128 X520kbps 270X480" encoding
      Then player should play the playback from remote to duration 00:00 in play state
      When I pause the "Football Commercial" video from ALL MEDIA with "WidevineOffline 128 X520kbps 270X480" encoding
      Then player should pause the playback from remote to duration 00:00 in pause state
      When I forwarded the "Football Commercial" video from ALL MEDIA with "WidevineOffline 128 X520kbps 270X480" encoding
      Then player should forwarded the playback from remote to duration 00:00 in pause state
      When I reversed the "Football Commercial" video from ALL MEDIA with "WidevineOffline 128 X520kbps 270X480" encoding
      Then player should reversed the playback from remote to duration 00:00 in pause state
      When I resume the "Football Commercial" video from ALL MEDIA with "WidevineOffline 128 X520kbps 270X480" encoding
      Then player should resume the playback from remote to duration 00:00 in play state

    Scenario: I should be able to play, pause, move forward, move backward, resume media by selecting media from the media list in media tab and with encoding automatically selected 
      Given the application has launched with following configuration in SETTINGS tab -
     |  name             |  value                            |
     | Organization ID   | a851c50193064ed6be08c7e75f8f4910  |
     | Access Key        | 6QDyPljwRS8L2w7Q7AnRo3sYIoQ=      |
     | Secret Key        | +d62cBI73hxWcRPpput4RR7a8v8=      |
      When I play the "Football Commercial" video from ALL MEDIA with "automatic" encoding
      Then player should play the playback from remote to duration 00:00 in play state
      When I pause the "Football Commercial" video from ALL MEDIA with "automatic" encoding
      Then player should pause the playback from remote to duration 00:00 in pause state
      When I forwarded the "Football Commercial" video from ALL MEDIA with "automatic" encoding
      Then player should forwarded the playback from remote to duration 00:00 in pause state
      When I reversed the "Football Commercial" video from ALL MEDIA with "automatic" encoding
      Then player should reversed the playback from remote to duration 00:00 in pause state
      When I resume the "Football Commercial" video from ALL MEDIA with "automatic" encoding
      Then player should resume the playback from remote to duration 00:00 in play state
       And exit from the application
       
    Scenario: I should be able to play, pause, move forward, move backward, resume media by providing media id in player tab and with encoding automatically selected 
     Given the application has launched with following configuration in SETTINGS tab -
     |  name             |  value                            |
     | Organization ID   | a851c50193064ed6be08c7e75f8f4910  |
     | Access Key        | 6QDyPljwRS8L2w7Q7AnRo3sYIoQ=      |
     | Secret Key        | +d62cBI73hxWcRPpput4RR7a8v8=      |
      When I play the "remote" video from 663d65d7379847e680f8eb084ddd222b with "automatic" encoding
      Then player should play the playback from remote to duration 00:00 in play state
      When I pause the "remote" video from 663d65d7379847e680f8eb084ddd222b with "automatic" encoding
      Then player should pause the playback from remote to duration 00:00 in pause state
      When I forwarded the "remote" video from 663d65d7379847e680f8eb084ddd222b with "automatic" encoding
      Then player should forwarded the playback from remote to duration 00:00 in pause state
      When I reversed the "remote" video from 663d65d7379847e680f8eb084ddd222b with "automatic" encoding
      Then player should reversed the playback from remote to duration 00:00 in pause state
      When I resume the "remote" video from 663d65d7379847e680f8eb084ddd222b with "automatic" encoding
      Then player should resume the playback from remote to duration 00:00 in play state
      Then exit from the application