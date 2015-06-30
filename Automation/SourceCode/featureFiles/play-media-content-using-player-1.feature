Feature: Play, Pause, Move Forward, Move Backward, Resume and Seek Media Content Using Player

    Scenario: I should be able to play, pause, move forward, move backward, resume Widevine online encoded video
     Given the application has launched
      When I play the "Widewine_Online_SD" video from ALL MEDIA with "Widevine 40 X286kbps 360X640" encoding
      Then player should play the playback from remote to duration 00:00 in play state
      When I pause the "Widewine_Online_SD" video from ALL MEDIA with "Widevine 40 X286kbps 360X640" encoding
      Then player should pause the playback from remote to duration 00:00 in pause state
      When I forwarded the "Widewine_Online_SD" video from ALL MEDIA with "Widevine 40 X286kbps 360X640" encoding
      Then player should forwarded the playback from remote to duration 00:00 in pause state
      When I reversed the "Widewine_Online_SD" video from ALL MEDIA with "Widevine 40 X286kbps 360X640" encoding
      Then player should reversed the playback from remote to duration 00:00 in pause state
      When I resume the "Widewine_Online_SD" video from ALL MEDIA with "Widevine 40 X286kbps 360X640" encoding
      Then player should resume the playback from remote to duration 00:00 in play state

    Scenario: I should be able to play, pause, move forward, move backward, resume Widevine offline encoded video
      Given the application has launched
      When I play the "Widewine_Offline" video from ALL MEDIA with "WidevineOffline 10 X50kbps 240X572" encoding
      Then player should play the playback from remote to duration 00:00 in play state
      When I pause the "Widewine_Offline" video from ALL MEDIA with "WidevineOffline 10 X50kbps 240X572" encoding
      Then player should pause the playback from remote to duration 00:00 in pause state
      When I forwarded the "Widewine_Offline" video from ALL MEDIA with "WidevineOffline 10 X50kbps 240X572" encoding
      Then player should forwarded the playback from remote to duration 00:00 in pause state
      When I reversed the "Widewine_Offline" video from ALL MEDIA with "WidevineOffline 10 X50kbps 240X572" encoding
      Then player should reversed the playback from remote to duration 00:00 in pause state
      When I resume the "Widewine_Offline" video from ALL MEDIA with "WidevineOffline 10 X50kbps 240X572" encoding
      Then player should resume the playback from remote to duration 00:00 in play state

    Scenario: I should be able to play, pause, move forward, move backward, resume media by selecting media from the media list in media tab and with encoding automatically selected 
      Given the application has launched
      When I play the "Widewine_Online_HD" video from ALL MEDIA with "automatic" encoding
      Then player should play the playback from remote to duration 00:00 in play state
      When I pause the "Widewine_Online_HD" video from ALL MEDIA with "automatic" encoding
      Then player should pause the playback from remote to duration 00:00 in pause state
      When I forwarded the "Widewine_Online_HD" video from ALL MEDIA with "automatic" encoding
      Then player should forwarded the playback from remote to duration 00:00 in pause state
      When I reversed the "Widewine_Online_HD" video from ALL MEDIA with "automatic" encoding
      Then player should reversed the playback from remote to duration 00:00 in pause state
      When I resume the "Widewine_Online_HD" video from ALL MEDIA with "automatic" encoding
      Then player should resume the playback from remote to duration 00:00 in play state
       And exit from the application
       
    Scenario: I should be able to play, pause, move forward, move backward, resume media by providing media id in player tab and with encoding automatically selected 
     Given the application has launched
      When I play the "remote" video from f3f77d7b56dd4f56a1b1f1b06e4944f7 with "automatic" encoding
      Then player should play the playback from remote to duration 00:00 in play state
      When I pause the "remote" video from f3f77d7b56dd4f56a1b1f1b06e4944f7 with "automatic" encoding
      Then player should pause the playback from remote to duration 00:00 in pause state
      When I forwarded the "remote" video from f3f77d7b56dd4f56a1b1f1b06e4944f7 with "automatic" encoding
      Then player should forwarded the playback from remote to duration 00:00 in pause state
      When I reversed the "remote" video from f3f77d7b56dd4f56a1b1f1b06e4944f7 with "automatic" encoding
      Then player should reversed the playback from remote to duration 00:00 in pause state
      When I resume the "remote" video from f3f77d7b56dd4f56a1b1f1b06e4944f7 with "automatic" encoding
      Then player should resume the playback from remote to duration 00:00 in play state
      Then exit from the application