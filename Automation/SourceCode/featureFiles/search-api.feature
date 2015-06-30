Feature: This will test all scenarios for search api
    Scenario: I should able to search by "Media Title" and reset the search
     Given the application has launched
     When I search the as value for Media Title in ALL MEDIA tab
     Then the CURRENT tab should have following media -
         | media           |
         | The Dark Knight |
         | The Chipettes   |
         | The Cars        |
     When I refresh the ALL MEDIA tab
     Then the CURRENT tab should have following media -
         | media                  |
         | Avengers Age of Ultron |
         | Underwater Marine Life |
         | The Dark Knight        |
         | The Chipettes          |
         | The Cars               |

    Scenario: I should able to search by "Description" and reset the search
     Given the application has launched
     When I search test as value for Description in ALL MEDIA tab
     Then the CURRENT tab should have following media -
         | media           |
         | Adobe           |
         | Act Of Caring   |
     When I refresh the ALL MEDIA tab
     Then the CURRENT tab should have following media -
         | media                  |
         | Adobe                  |
         | Act Of Caring          |
         | Avengers Age of Ultron |
         | Underwater Marine Life |

    Scenario: I should able to search by "Channel ID" and reset the search
     Given the application has launched
     When I search 821e89bb507346408a as value for Channel ID in ALL MEDIA tab
     Then the CURRENT tab should have following media -
         | media           |
         | Toy Story       |
         | The Chipettes   |
         | The Cars        |
         | Pixars Up       |
         | Tom And Jerry   |
     When I refresh the ALL MEDIA tab
     Then the CURRENT tab should have following media -
         | media                  |
         | Toy Story              |
         | The Chipettes          |
         | The Cars               |
         | Pixars Up              |
         | Tom And Jerry          |
         | Avengers Age of Ultron |
         | Underwater Marine Life |

    Scenario: I should see error message if no data found during search by "Media Title" and on reset the search message should disappear
     Given the application has launched
     When I search XXXX as value for Media Title in ALL MEDIA tab
     Then the CURRENT tab should have following error message -
         | error message    |
         | No Media Found   |
     When I refresh the ALL MEDIA tab
     Then the CURRENT tab should not have following error message -
         | error message    |
         | No Media Found   |
     When I search %$#@! as value for Media Title in ALL MEDIA tab
     Then the CURRENT tab should have following error message -
         | error message                          |
         | You supplied an invalid search field   |
     When I refresh the ALL MEDIA tab
     Then the CURRENT tab should not have following error message -
         | error message    |
         | No Media Found   |

    Scenario: I should see error message if no data found during search by "Description" and on reset the search message should disappear
     Given the application has launched
     When I search XXXX as value for Description in ALL MEDIA tab
     Then the CURRENT tab should have following error message -
         | error message    |
         | No Media Found   |
     When I refresh the ALL MEDIA tab
     Then the CURRENT tab should not have following error message -
         | error message    |
         | No Media Found   |
     When I search %$#@! as value for Description in ALL MEDIA tab
     Then the CURRENT tab should have following error message -
         | error message                          |
         | You supplied an invalid search field   |
     When I refresh the ALL MEDIA tab
     Then the CURRENT tab should not have following error message -
         | error message    |
         | No Media Found   |

    Scenario: I should see error message if no data found during search by "Channel ID" and on reset the search message should disappear
     Given the application has launched
     When I search XXXX as value for Channel ID in ALL MEDIA tab
     Then the CURRENT tab should have following error message -
         | error message    |
         | No Media Found   |
     When I refresh the ALL MEDIA tab
     Then the CURRENT tab should not have following error message -
         | error message    |
         | No Media Found   |
     When I search %$#@! as value for Channel ID in ALL MEDIA tab
     Then the CURRENT tab should have following error message -
         | error message                          |
         | You supplied an invalid search field   |
     When I refresh the ALL MEDIA tab
     Then the CURRENT tab should not have following error message -
         | error message    |
         | No Media Found   |