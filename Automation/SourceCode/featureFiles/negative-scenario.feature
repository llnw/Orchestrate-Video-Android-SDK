Feature: checking the negative scenario

    Scenario: After providing wrong organization id, error message should shown up on all tabs
    Given the application has launched
    When I select Rebaca Channel Group as value for channel-group in CHANNEL GROUPS page
    And I select Some Sample Videos as value for channel in CHANNELS page

    When I set xxxxxxxxxxxxxxxxxxx as value for Organization ID in SETTINGS page
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
    Given the application has launched
    When I set xxxxxxxxxxxxxxxxxxx as value for Organization ID in SETTINGS page
    And I play the "Code Rush" video from ALL MEDIA with "Flash 128 X472kbps 270X480" encoding
    Then player should not play the playback from remote to duration 00:00 in play state
    And exit from the application
    
    Scenario: If we not select any channel group then the channel tab should show the message
    Given the application has launched
    When I select the CHANNELS tab
    Then the CURRENT tab should have following error message -
     | error message               |
     | No Channel Group Selected   |
    When I refresh the CHANNELS tab
    Then the CURRENT tab should have following error message -
     | error message               |
     | No Channel Group Selected   |
     
    Scenario: If we not select any channel then the media tab should show the message
    Given the application has launched
    When I select the MEDIA tab
    Then the CURRENT tab should have following error message -
     | error message         |
     | No Channel Selected   |
    When I refresh the MEDIA tab
    Then the CURRENT tab should have following error message -
     | error message         |
     | No Channel Selected   |
     
    Scenario: The video playback should get paused when we move to another tab.
    Given the application has launched
    When I play the "Code Rush" video from ALL MEDIA with "Flash 128 X472kbps 270X480" encoding
    And I select the MEDIA tab
    And I select the PLAYER tab
    Then player should pause the playback from remote to duration 00:00 in pause state
    When I resume the "Code Rush" video from ALL MEDIA with "Flash 128 X472kbps 270X480" encoding
    And I pause the "Code Rush" video from ALL MEDIA with "Flash 128 X472kbps 270X480" encoding
    And I select the MEDIA tab
    And I select the PLAYER tab
    Then player should pause the playback from remote to duration 00:00 in pause state
    And exit from the application

    Scenario: I should be able to see the icon.         
     Given the application has launched

     When I select the CHANNEL GROUPS tab
     Then the CURRENT tab should have following icon set -
     | name                 | icon                     |
     | Rebaca Channel Group | Rebaca-Channel-Group.png |
     | Limelight            | Limelight.png            |
     
     When I select the ALL CHANNELS tab
     Then the CURRENT tab should have following icon set -
     | name                | icon                    |
     | Some Sample Videos1 | Some-Sample-Videos1.png |
     | Maru Madnes         | Maru-Madnes.png         |
     
     When I select the ALL MEDIA tab
     Then the CURRENT tab should have following icon set -
     | name           | icon              |
     | Late For Work  | Late-For-Work.png |
     | Code Rush      | Code-Rush.png     |