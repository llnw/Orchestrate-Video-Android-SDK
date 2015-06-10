Feature: Check only the publish media is showing up
  Scenario: Only the publish media should up
      Given the application has launched with following configuration in SETTINGS tab -
         |  name             |  value                            |
         | Organization ID   | a851c50193064ed6be08c7e75f8f4910  |
         | Access Key        | 6QDyPljwRS8L2w7Q7AnRo3sYIoQ=      |
         | Secret Key        | +d62cBI73hxWcRPpput4RR7a8v8=      |
      When I update "unpublish" as value for "state" in "Adobe" media
       And I update "unpublish" as value for "state" in "Avengers Age of Ultron" media
       And I refresh the ALL MEDIA tab
      Then the ALL MEDIA tab should not have following media -
         | media                   |
         | Adobe                   |
         | Avengers Age of Ultron  |
      When I update "publish" as value for "state" in "Adobe" media
       And I update "publish" as value for "state" in "Avengers Age of Ultron" media
       And I refresh the ALL MEDIA tab
      Then the ALL MEDIA tab should have following media -
         | media                   |
         | Adobe                   |
         | Avengers Age of Ultron  |

