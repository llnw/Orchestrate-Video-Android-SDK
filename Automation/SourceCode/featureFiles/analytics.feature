Feature: This will test all scenarios for analytic
    Scenario: On playing flash encoded video, events should get generated on player load, play, pause, seek, resume and on-complete play
     Given the application has launched with following configuration in SETTINGS tab -
         |  name             |  value                            |
         | Organization ID   | a851c50193064ed6be08c7e75f8f4910  |
         | Access Key        | 6QDyPljwRS8L2w7Q7AnRo3sYIoQ=      |
         | Secret Key        | +d62cBI73hxWcRPpput4RR7a8v8=      |
     When I play the "Flash_Only" video from ALL MEDIA with "Flash 10 X40kbps 240X580" encoding
     And I pause the "Flash_Only" video from ALL MEDIA with "Flash 10 X40kbps 240X580" encoding
     And I resume the "Flash_Only" video from ALL MEDIA with "Flash 10 X40kbps 240X580" encoding
     And I seek-2:00 the "Flash_Only" video from ALL MEDIA with "Flash 10 X40kbps 240X580" encoding
     And I pause the "Flash_Only" video from ALL MEDIA with "Flash 10 X40kbps 240X580" encoding
     And I resume the "Flash_Only" video from ALL MEDIA with "Flash 10 X40kbps 240X580" encoding
     And I completed the "Flash_Only" video from ALL MEDIA with "Flash 10 X40kbps 240X580" encoding
     Then the player should send expected notification
     
     
     
    Scenario: On playing HLS encoded video, events should get generated on player load, play, pause, seek, resume and on-complete play
     Given the application has launched with following configuration in SETTINGS tab -
         |  name             |  value                            |
         | Organization ID   | a851c50193064ed6be08c7e75f8f4910  |
         | Access Key        | 6QDyPljwRS8L2w7Q7AnRo3sYIoQ=      |
         | Secret Key        | +d62cBI73hxWcRPpput4RR7a8v8=      |
     When I play the "Flash_HLS" video from ALL MEDIA with "HttpLiveStreaming 96 X409kbps 264X640" encoding
     And I pause the "Flash_HLS" video from ALL MEDIA with "HttpLiveStreaming 96 X409kbps 264X640" encoding
     And I resume the "Flash_HLS" video from ALL MEDIA with "HttpLiveStreaming 96 X409kbps 264X640" encoding
     And I seek-2:00 the "Flash_HLS" video from ALL MEDIA with "HttpLiveStreaming 96 X409kbps 264X640" encoding
     And I pause the "Flash_HLS" video from ALL MEDIA with "HttpLiveStreaming 96 X409kbps 264X640" encoding
     And I resume the "Flash_HLS" video from ALL MEDIA with "HttpLiveStreaming 96 X409kbps 264X640" encoding
     And I completed the "Flash_HLS" video from ALL MEDIA with "HttpLiveStreaming 96 X409kbps 264X640" encoding
     Then the player should send expected notification
     
     
    Scenario: On playing Widevine online encoded video, events should get generated on player load, play, pause, seek, resume and on-complete play
     Given the application has launched with following configuration in SETTINGS tab -
         |  name             |  value                            |
         | Organization ID   | a851c50193064ed6be08c7e75f8f4910  |
         | Access Key        | 6QDyPljwRS8L2w7Q7AnRo3sYIoQ=      |
         | Secret Key        | +d62cBI73hxWcRPpput4RR7a8v8=      |
     When I play the "Widewine_Online_SD" video from ALL MEDIA with "Widevine 40 X286kbps 360X640" encoding
     And I pause the "Widewine_Online_SD" video from ALL MEDIA with "Widevine 40 X286kbps 360X640" encoding
     And I resume the "Widewine_Online_SD" video from ALL MEDIA with "Widevine 40 X286kbps 360X640" encoding
     And I seek-2:00 the "Widewine_Online_SD" video from ALL MEDIA with "Widevine 40 X286kbps 360X640" encoding
     And I pause the "Widewine_Online_SD" video from ALL MEDIA with "Widevine 40 X286kbps 360X640" encoding
     And I resume the "Widewine_Online_SD" video from ALL MEDIA with "Widevine 40 X286kbps 360X640" encoding
     And I completed the "Widewine_Online_SD" video from ALL MEDIA with "Widevine 40 X286kbps 360X640" encoding
     Then the player should send expected notification
     
     
    Scenario: On playing Widevine offline encoded video, events should get generated on player load, play, pause, seek, resume and on-complete play
     Given the application has launched with following configuration in SETTINGS tab -
         |  name             |  value                            |
         | Organization ID   | a851c50193064ed6be08c7e75f8f4910  |
         | Access Key        | 6QDyPljwRS8L2w7Q7AnRo3sYIoQ=      |
         | Secret Key        | +d62cBI73hxWcRPpput4RR7a8v8=      |
     When I play the "Widewine_Offline" video from ALL MEDIA with "WidevineOffline 128 X500kbps 268X640" encoding
     And I pause the "Widewine_Offline" video from ALL MEDIA with "WidevineOffline 128 X500kbps 268X640" encoding
     And I resume the "Widewine_Offline" video from ALL MEDIA with "WidevineOffline 128 X500kbps 268X640" encoding
     And I seek-2:00 the "Widewine_Offline" video from ALL MEDIA with "WidevineOffline 128 X500kbps 268X640" encoding
     And I pause the "Widewine_Offline" video from ALL MEDIA with "WidevineOffline 128 X500kbps 268X640" encoding
     And I resume the "Widewine_Offline" video from ALL MEDIA with "WidevineOffline 128 X500kbps 268X640" encoding
     And I completed the "Widewine_Offline" video from ALL MEDIA with "WidevineOffline 128 X500kbps 268X640" encoding
     Then the player should send expected notification
     