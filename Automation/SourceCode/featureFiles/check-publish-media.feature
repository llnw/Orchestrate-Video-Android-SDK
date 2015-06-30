Feature: Check only the publish media is showing up
  Scenario: Only the publish media should up
      Given the application has launched
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

