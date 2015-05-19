Feature: Play, Pause, Move Forward, Move Backward, Resume and Seek Media Content Using Player

    Scenario: I should be able to play, pause, move forward, move backward, resume flash encoded video
     Given the application has launched with following configuration in SETTINGS tab -
         |  name             |  value                            |
         | Organization ID   | a851c50193064ed6be08c7e75f8f4910  |
         | Access Key        | 6QDyPljwRS8L2w7Q7AnRo3sYIoQ=      |
         | Secret Key        | +d62cBI73hxWcRPpput4RR7a8v8=      |
      When I play the "Flash_Only" video from ALL MEDIA with "Flash 10 X40kbps 240X580" encoding
      Then player should play the playback from remote to duration 00:00 in play state
      When I pause the "Flash_Only" video from ALL MEDIA with "Flash 10 X40kbps 240X580" encoding
      Then player should pause the playback from remote to duration 00:00 in pause state
      When I forwarded the "Flash_Only" video from ALL MEDIA with "Flash 10 X40kbps 240X580" encoding
      Then player should forwarded the playback from remote to duration 00:00 in pause state
      When I reversed the "Flash_Only" video from ALL MEDIA with "Flash 10 X40kbps 240X580" encoding
      Then player should reversed the playback from remote to duration 00:00 in pause state
      When I resume the "Flash_Only" video from ALL MEDIA with "Flash 10 X40kbps 240X580" encoding
      Then player should resume the playback from remote to duration 00:00 in play state

    Scenario: I should be able to play, pause, move forward, move backward, resume and seek the HLS encoded video
      Given the application has launched with following configuration in SETTINGS tab -
         |  name             |  value                            |
         | Organization ID   | a851c50193064ed6be08c7e75f8f4910  |
         | Access Key        | 6QDyPljwRS8L2w7Q7AnRo3sYIoQ=      |
         | Secret Key        | +d62cBI73hxWcRPpput4RR7a8v8=      |
      When I play the "Flash_HLS" video from ALL MEDIA with "HttpLiveStreaming 96 X409kbps 264X640" encoding
      Then player should play the playback from remote to duration 00:00 in play state    
      When I pause the "Flash_HLS" video from ALL MEDIA with "HttpLiveStreaming 96 X409kbps 264X640" encoding
      Then player should pause the playback from remote to duration 00:00 in pause state
      When I forwarded the "Flash_HLS" video from ALL MEDIA with "HttpLiveStreaming 96 X409kbps 264X640" encoding
      Then player should forwarded the playback from remote to duration 00:00 in pause state
      When I reversed the "Flash_HLS" video from ALL MEDIA with "HttpLiveStreaming 96 X409kbps 264X640" encoding
      Then player should reversed the playback from remote to duration 00:00 in pause state
      When I seek-1:00 the "Flash_HLS" video from ALL MEDIA with "HttpLiveStreaming 96 X409kbps 264X640" encoding
      Then player should seek the playback from file to duration 1:00 in pause state
      When I resume the "Flash_HLS" video from ALL MEDIA with "HttpLiveStreaming 96 X409kbps 264X640" encoding
      Then player should resume the playback from remote to duration 00:00 in play state

    Scenario: I should be able to play, pause, move forward, move backward, resume and seek the local video content.
      Given the application has launched with following configuration in SETTINGS tab -
         |  name             |  value                            |
         | Organization ID   | a851c50193064ed6be08c7e75f8f4910  |
         | Access Key        | 6QDyPljwRS8L2w7Q7AnRo3sYIoQ=      |
         | Secret Key        | +d62cBI73hxWcRPpput4RR7a8v8=      |
      When I play the "local" video from Downloads/testLocalMedia.mp4 with "local" encoding
      Then player should play the playback from file to duration xx:xx in play state
      When I pause the "local" video from Downloads/testLocalMedia.mp4 with "local" encoding
      Then player should pause the playback from file to duration xx:xx in pause state
      When I forwarded the "local" video from Downloads/testLocalMedia.mp4 with "local" encoding
      Then player should forwarded the playback from file to duration xx:xx in pause state
      When I reversed the "local" video from Downloads/testLocalMedia.mp4 with "local" encoding
      Then player should reversed the playback from file to duration xx:xx in pause state
      When I seek-2:00 the "local" video from Downloads/testLocalMedia.mp4 with "local" encoding
      Then player should seek the playback from file to duration 2:00 in pause state
      When I resume the "local" video from Downloads/testLocalMedia.mp4 with "local" encoding
      Then player should resume the playback from file to duration xx:xx in play state
      
    Scenario: I should be able to play, pause, move forward, move backward, resume and seek the remote video content using media id.
      Given the application has launched with following configuration in SETTINGS tab -
         |  name             |  value                            |
         | Organization ID   | a851c50193064ed6be08c7e75f8f4910  |
         | Access Key        | 6QDyPljwRS8L2w7Q7AnRo3sYIoQ=      |
         | Secret Key        | +d62cBI73hxWcRPpput4RR7a8v8=      |
      When I play the "remote" video from 936288a1457b4bb2acbdb18ed5cd58c1 with "Flash 128 X472kbps 270X480" encoding
      Then player should play the playback from remote to duration 00:00 in play state
      When I pause the "remote" video from 936288a1457b4bb2acbdb18ed5cd58c1 with "Flash 128 X472kbps 270X480" encoding
      Then player should pause the playback from remote to duration 00:00 in pause state
      When I forwarded the "remote" video from 936288a1457b4bb2acbdb18ed5cd58c1 with "Flash 128 X472kbps 270X480" encoding
      Then player should forwarded the playback from remote to duration 00:00 in pause state
      When I reversed the "remote" video from 936288a1457b4bb2acbdb18ed5cd58c1 with "Flash 128 X472kbps 270X480" encoding
      Then player should reversed the playback from remote to duration 00:00 in pause state
      When I resume the "remote" video from 936288a1457b4bb2acbdb18ed5cd58c1 with "Flash 128 X472kbps 270X480" encoding
      Then player should resume the playback from remote to duration 00:00 in play state
      Then exit from the application