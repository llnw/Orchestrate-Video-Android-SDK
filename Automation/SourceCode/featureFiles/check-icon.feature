Feature: Check icon
    Scenario: I should be able to see the icon.         
     Given the application has launched
     When I select the CHANNEL GROUPS tab
     Then the CURRENT tab should have following icon set -
     | name          | icon                     |
     | ENTERTAINMENT | Limelight.png            |
     | MY GROUP      | Rebaca-Channel-Group.png |
     When I select the ALL CHANNELS tab
     Then the CURRENT tab should have following icon set -
     | name       | icon           |
     | Cartoon    | Cartoon.png    |
     | Education  | education.png  |
     | Finance    | finance.png    |
     | Sports     | sports.png     |
     | Technology | technology.png |
     | TV         | tv.png         |