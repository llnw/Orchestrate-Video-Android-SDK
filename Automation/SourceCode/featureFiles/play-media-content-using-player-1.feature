Feature: Play, Pause, Move Forward, Move Backward, Resume and Seek Media Content Using Player

    Scenario: I should be able to play, pause, move forward, move backward, resume Widevine online encoded video
      Given the application has launched
      When I play the "Code Rush" video from ALL MEDIA with "Widevine 128 X2400kbps 360X640" encoding
      Then player should play the playback from remote to duration 00:00 in play state
      When I pause the "Code Rush" video from ALL MEDIA with "Widevine 128 X2400kbps 360X640" encoding
      Then player should pause the playback from remote to duration 00:00 in pause state
      When I forwarded the "Code Rush" video from ALL MEDIA with "Widevine 128 X2400kbps 360X640" encoding
      Then player should forwarded the playback from remote to duration 00:00 in pause state
      When I reversed the "Code Rush" video from ALL MEDIA with "Widevine 128 X2400kbps 360X640" encoding
      Then player should reversed the playback from remote to duration 00:00 in pause state
      When I resume the "Code Rush" video from ALL MEDIA with "Widevine 128 X2400kbps 360X640" encoding
      Then player should resume the playback from remote to duration 00:00 in play state

    Scenario: I should be able to play, pause, move forward, move backward, resume Widevine offline encoded video
      Given the application has launched
      When I play the "Jaguar_All" video from ALL MEDIA with "WidevineOffline 128 X520kbps 270X480" encoding
      Then player should play the playback from remote to duration 00:00 in play state
      When I pause the "Jaguar_All" video from ALL MEDIA with "WidevineOffline 128 X520kbps 270X480" encoding
      Then player should pause the playback from remote to duration 00:00 in pause state
      When I forwarded the "Jaguar_All" video from ALL MEDIA with "WidevineOffline 128 X520kbps 270X480" encoding
      Then player should forwarded the playback from remote to duration 00:00 in pause state
      When I reversed the "Jaguar_All" video from ALL MEDIA with "WidevineOffline 128 X520kbps 270X480" encoding
      Then player should reversed the playback from remote to duration 00:00 in pause state
      When I resume the "Jaguar_All" video from ALL MEDIA with "WidevineOffline 128 X520kbps 270X480" encoding
      Then player should resume the playback from remote to duration 00:00 in play state

    Scenario: I should be able to play, pause, move forward, move backward, resume media by selecting media from the media list in media tab and with encoding automatically selected 
      Given the application has launched
      When I play the "Jaguar_All" video from ALL MEDIA with "automatic" encoding
      Then player should play the playback from remote to duration 00:00 in play state
      When I pause the "Jaguar_All" video from ALL MEDIA with "automatic" encoding
      Then player should pause the playback from remote to duration 00:00 in pause state
      When I forwarded the "Jaguar_All" video from ALL MEDIA with "automatic" encoding
      Then player should forwarded the playback from remote to duration 00:00 in pause state
      When I reversed the "Jaguar_All" video from ALL MEDIA with "automatic" encoding
      Then player should reversed the playback from remote to duration 00:00 in pause state
      When I resume the "Jaguar_All" video from ALL MEDIA with "automatic" encoding
      Then player should resume the playback from remote to duration 00:00 in play state
       And exit from the application
       
    Scenario: I should be able to play, pause, move forward, move backward, resume media by providing media id in player tab and with encoding automatically selected 
      Given the application has launched
      When I play the "remote" video from 6fe75d0081224f74966d9a2e2f40b7da with "automatic" encoding
      Then player should play the playback from remote to duration 00:00 in play state
      When I pause the "remote" video from 6fe75d0081224f74966d9a2e2f40b7da with "automatic" encoding
      Then player should pause the playback from remote to duration 00:00 in pause state
      When I forwarded the "remote" video from 6fe75d0081224f74966d9a2e2f40b7da with "automatic" encoding
      Then player should forwarded the playback from remote to duration 00:00 in pause state
      When I reversed the "remote" video from 6fe75d0081224f74966d9a2e2f40b7da with "automatic" encoding
      Then player should reversed the playback from remote to duration 00:00 in pause state
      When I resume the "remote" video from 6fe75d0081224f74966d9a2e2f40b7da with "automatic" encoding
      Then player should resume the playback from remote to duration 00:00 in play state
      Then exit from the application