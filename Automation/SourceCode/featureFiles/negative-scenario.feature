Feature: checking the negative scenario

    Scenario: After providing wrong organization id, error message should shown up on all tabs
    Given the application has launched with following configuration in SETTINGS tab -
     |  name             |  value                            |
     | Organization ID   | a851c50193064ed6be08c7e75f8f4910  |
     | Access Key        | 6QDyPljwRS8L2w7Q7AnRo3sYIoQ=      |
     | Secret Key        | +d62cBI73hxWcRPpput4RR7a8v8=      |
     
    When I select ENTERTAINMENT as value for channel-group in CHANNEL GROUPS tab
     And I select Cartoon as value for channel in CHANNELS tab

    When I set xxxxxxxxxxxxxxxxxxx as value for Organization ID in SETTINGS tab
    And I refresh the CHANNEL GROUPS tab
    Then the CURRENT tab should have following error message -
     | error message                                                         |
     | Organization either does not exist or you do not have access to it.   |
    
    When I refresh the ALL CHANNELS tab
    Then the CURRENT tab should have following error message -
     | error message                                                         |
     | Organization either does not exist or you do not have access to it.   |
     
    When I refresh the CHANNELS tab
    Then the CURRENT tab should have following error message -
     | error message                                                         |
     | Organization either does not exist or you do not have access to it.   |
     
    When I refresh the ALL MEDIA tab
    Then the CURRENT tab should have following error message -
     | error message                                                         |
     | Organization either does not exist or you do not have access to it.   |
     
    When I refresh the MEDIA tab
    Then the CURRENT tab should have following error message -
     | error message                                                         |
     | Organization either does not exist or you do not have access to it.   |
    And exit from the application

    Scenario: After providing wrong organization id, I should not be able to play a media from media tab 
    Given the application has launched with following configuration in SETTINGS tab -
     |  name             |  value                            |
     | Organization ID   | a851c50193064ed6be08c7e75f8f4910  |
     | Access Key        | 6QDyPljwRS8L2w7Q7AnRo3sYIoQ=      |
     | Secret Key        | +d62cBI73hxWcRPpput4RR7a8v8=      |
     
    When I set xxxxxxxxxxxxxxxxxxx as value for Organization ID in SETTINGS tab
    And I play the "Flash_Only" video from ALL MEDIA with "Flash 96 X128kbps 264X640" encoding
    Then player should not play the playback from remote to duration 00:00 in play state
    And exit from the application
    
    Scenario: If we not select any channel group then the channel tab should show the message
    Given the application has launched with following configuration in SETTINGS tab -
     |  name             |  value                            |
     | Organization ID   | a851c50193064ed6be08c7e75f8f4910  |
     | Access Key        | 6QDyPljwRS8L2w7Q7AnRo3sYIoQ=      |
     | Secret Key        | +d62cBI73hxWcRPpput4RR7a8v8=      |
     
    When I select the CHANNELS tab
    Then the CURRENT tab should have following error message -
     | error message               |
     | No Channel Group Selected   |
    When I refresh the CHANNELS tab
    Then the CURRENT tab should have following error message -
     | error message               |
     | No Channel Group Selected   |
     
    Scenario: If we not select any channel then the media tab should show the message
    Given the application has launched with following configuration in SETTINGS tab -
     |  name             |  value                            |
     | Organization ID   | a851c50193064ed6be08c7e75f8f4910  |
     | Access Key        | 6QDyPljwRS8L2w7Q7AnRo3sYIoQ=      |
     | Secret Key        | +d62cBI73hxWcRPpput4RR7a8v8=      |
     
    When I select the MEDIA tab
    Then the CURRENT tab should have following error message -
     | error message         |
     | No Channel Selected   |
    When I refresh the MEDIA tab
    Then the CURRENT tab should have following error message -
     | error message         |
     | No Channel Selected   |
     
    Scenario: The video playback should get paused when we move to another tab.
    Given the application has launched with following configuration in SETTINGS tab -
     |  name             |  value                            |
     | Organization ID   | a851c50193064ed6be08c7e75f8f4910  |
     | Access Key        | 6QDyPljwRS8L2w7Q7AnRo3sYIoQ=      |
     | Secret Key        | +d62cBI73hxWcRPpput4RR7a8v8=      |
     
    When I play the "Flash_Only" video from ALL MEDIA with "Flash 10 X40kbps 240X580" encoding
    And I select the MEDIA tab
    And I select the PLAYER tab
    Then player should pause the playback from remote to duration 00:00 in pause state
    When I resume the "Flash_Only" video from ALL MEDIA with "Flash 10 X40kbps 240X580" encoding
    And I pause the "Flash_Only" video from ALL MEDIA with "Flash 10 X40kbps 240X580" encoding
    And I select the MEDIA tab
    And I select the PLAYER tab
    Then player should pause the playback from remote to duration 00:00 in pause state
    And exit from the application
