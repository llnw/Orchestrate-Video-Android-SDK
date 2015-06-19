Feature: Check the update media
  Scenario: The updated media should come at the top of the list
      Given the application has launched with following configuration in SETTINGS tab -
         |  name             |  value                            |
         | Organization ID   | a851c50193064ed6be08c7e75f8f4910  |
         | Access Key        | 6QDyPljwRS8L2w7Q7AnRo3sYIoQ=      |
         | Secret Key        | +d62cBI73hxWcRPpput4RR7a8v8=      |
      When I update "update description" as value for "description" in "Adobe" media
       And I update "update description" as value for "description" in "Avengers Age of Ultron" media
       And I refresh the ALL MEDIA tab
      Then the ALL MEDIA tab should have following media at top -
         | media                   |
         | Avengers Age of Ultron  |
         | Adobe                   |
      When I update "new update description" as value for "description" in "Avengers Age of Ultron" media
       And I update "new update test description" as value for "description" in "Adobe" media
       And I refresh the ALL MEDIA tab
      Then the ALL MEDIA tab should have following media at top -
         | media                   |
         | Adobe                   |
         | Avengers Age of Ultron  |

