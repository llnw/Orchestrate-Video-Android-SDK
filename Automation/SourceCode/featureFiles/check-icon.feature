Feature: Check icon
    Scenario: I should be able to see the icon.         
     Given the application has launched with following configuration in SETTINGS tab -
     |  name             |  value                            |
     | Organization ID   | a851c50193064ed6be08c7e75f8f4910  |
     | Access Key        | 6QDyPljwRS8L2w7Q7AnRo3sYIoQ=      |
     | Secret Key        | +d62cBI73hxWcRPpput4RR7a8v8=      |
    
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