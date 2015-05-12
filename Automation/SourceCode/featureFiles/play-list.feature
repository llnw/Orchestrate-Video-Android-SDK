Feature: Play list feature test
    Scenario: I should be able to add and remove selected media in play-list
     Given the application has launched with following configuration in SETTINGS tab -
     |  name             |  value                            |
     | Organization ID   | a851c50193064ed6be08c7e75f8f4910  |
     | Access Key        | 6QDyPljwRS8L2w7Q7AnRo3sYIoQ=      |
     | Secret Key        | +d62cBI73hxWcRPpput4RR7a8v8=      |
     
     When I add following media in play list from ALL MEDIA tab -
     | media name    |
     | Adobe         |
     | Iron Sky      |
     | Tom And Jerry |
     | The Cars      |
     Then following media from ALL MEDIA tab gets added in play list of PLAYER tab -
     | media name    |
     | Adobe         |
     | Iron Sky      |
     | Tom And Jerry |
     | The Cars      |
     When I remove following media in play list from PLAYER tab -
     | media name    |
     | Adobe         |
     | Tom And Jerry |
     Then following media from ALL MEDIA tab gets removed in play list of PLAYER tab -
     | media name    |
     | Adobe         |
     | Tom And Jerry |
     And exit from the application
     
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
     And exit from the application