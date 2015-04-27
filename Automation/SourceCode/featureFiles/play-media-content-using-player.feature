Feature: Play, Pause, Move Forward, Move Backward, Resume and Seek Media Content Using Player

    Scenario: I should be able to play, pause, move forward, move backward, resume flash encoded video
      Given the application has launched
      When I play the "Code Rush" video from ALL MEDIA with "Flash 128 X472kbps 270X480" encoding
      Then player should play the playback from remote to duration 00:00 in play state
      When I pause the "Code Rush" video from ALL MEDIA with "Flash 128 X472kbps 270X480" encoding
      Then player should pause the playback from remote to duration 00:00 in pause state
      When I forwarded the "Code Rush" video from ALL MEDIA with "Flash 128 X472kbps 270X480" encoding
      Then player should forwarded the playback from remote to duration 00:00 in pause state
      When I reversed the "Code Rush" video from ALL MEDIA with "Flash 128 X472kbps 270X480" encoding
      Then player should reversed the playback from remote to duration 00:00 in pause state
      When I resume the "Code Rush" video from ALL MEDIA with "Flash 128 X472kbps 270X480" encoding
      Then player should resume the playback from remote to duration 00:00 in play state

    Scenario: I should be able to play, pause, move forward, move backward, resume and seek the HLS encoded video
      Given the application has launched
      When I play the "Asiabanking" video from ALL MEDIA with "HttpLiveStreaming 96 X600kbps 360X640" encoding
      Then player should play the playback from remote to duration 00:00 in play state    
      When I pause the "Asiabanking" video from ALL MEDIA with "HttpLiveStreaming 96 X600kbps 360X640" encoding
      Then player should pause the playback from remote to duration 00:00 in pause state
      When I forwarded the "Asiabanking" video from ALL MEDIA with "HttpLiveStreaming 96 X600kbps 360X640" encoding
      Then player should forwarded the playback from remote to duration 00:00 in pause state
      When I reversed the "Asiabanking" video from ALL MEDIA with "HttpLiveStreaming 96 X600kbps 360X640" encoding
      Then player should reversed the playback from remote to duration 00:00 in pause state
      When I seek-1:00 the "Asiabanking" video from ALL MEDIA with "HttpLiveStreaming 96 X600kbps 360X640" encoding
      Then player should seek the playback from file to duration 1:00 in pause state
      When I resume the "Asiabanking" video from ALL MEDIA with "HttpLiveStreaming 96 X600kbps 360X640" encoding
      Then player should resume the playback from remote to duration 00:00 in play state

    Scenario: I should be able to play, pause, move forward, move backward, resume and seek the local video content.
      Given the application has launched
      When I play the "local" video from Downloads/tGsMo2013-1.mp4 with "local" encoding
      Then player should play the playback from file to duration xx:xx in play state
      When I pause the "local" video from Downloads/tGsMo2013-1.mp4 with "local" encoding
      Then player should pause the playback from file to duration xx:xx in pause state
      When I forwarded the "local" video from Downloads/tGsMo2013-1.mp4 with "local" encoding
      Then player should forwarded the playback from file to duration xx:xx in pause state
      When I reversed the "local" video from Downloads/tGsMo2013-1.mp4 with "local" encoding
      Then player should reversed the playback from file to duration xx:xx in pause state
      When I seek-2:00 the "local" video from Downloads/tGsMo2013-1.mp4 with "local" encoding
      Then player should seek the playback from file to duration 2:00 in pause state
      When I resume the "local" video from Downloads/tGsMo2013-1.mp4 with "local" encoding
      Then player should resume the playback from file to duration xx:xx in play state
      
    Scenario: I should be able to play, pause, move forward, move backward, resume and seek the remote video content using media id.
      Given the application has launched
      When I play the "remote" video from 6fe75d0081224f74966d9a2e2f40b7da with "Flash 128 X472kbps 270X480" encoding
      Then player should play the playback from remote to duration 00:00 in play state
      When I pause the "remote" video from 6fe75d0081224f74966d9a2e2f40b7da with "Flash 128 X472kbps 270X480" encoding
      Then player should pause the playback from remote to duration 00:00 in pause state
      When I forwarded the "remote" video from 6fe75d0081224f74966d9a2e2f40b7da with "Flash 128 X472kbps 270X480" encoding
      Then player should forwarded the playback from remote to duration 00:00 in pause state
      When I reversed the "remote" video from 6fe75d0081224f74966d9a2e2f40b7da with "Flash 128 X472kbps 270X480" encoding
      Then player should reversed the playback from remote to duration 00:00 in pause state
      When I resume the "remote" video from 6fe75d0081224f74966d9a2e2f40b7da with "Flash 128 X472kbps 270X480" encoding
      Then player should resume the playback from remote to duration 00:00 in play state
      Then exit from the application