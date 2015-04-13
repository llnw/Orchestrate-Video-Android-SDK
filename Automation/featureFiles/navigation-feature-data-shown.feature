Feature: Navigation Feature and data validation
   Scenario: Should be able to show the appropriate data
    Given the application has launched
      When I select the CHANNEL GROUPS tab
      Then the CURRENT tab should have following channel group -
        | channel group        |
        | Entertainment        |
        | Protected Channel    |
        | Limelight            |
        | Rebaca Channel Group |
      When I select the ALL CHANNELS tab
      Then the CURRENT tab should have following channel -
        | channel              |
        | Some Sample Videos   |
        | Kanchan              |
        | Protected Content    |
        | Maru Madness         |
      When I select Rebaca Channel Group as value for channel-group in CHANNEL GROUPS page
      Then the CURRENT tab should have following channel -
        | channel              |
        | Some Sample Videos   |
        | Maru Madness         |
      When I select Some Sample Videos as value for channel in CHANNELS page
      Then the CURRENT tab should have following media -
        | media                   |
        | Sintel Trailer          |
        | Tears of Steel Trailer  |
        | 5ms of Black            |
      Then exit from the application