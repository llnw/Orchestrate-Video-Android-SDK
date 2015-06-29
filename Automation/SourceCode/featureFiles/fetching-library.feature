Feature: Fetching Library 
    Scenario: I should be able to see the list of channel groups (associated with the organization id) when I go to channel group tab.         
    Given the application has launched with following configuration in SETTINGS tab -
     |  name             |  value                            |
     | Organization ID   | a851c50193064ed6be08c7e75f8f4910  |
     | Access Key        | 6QDyPljwRS8L2w7Q7AnRo3sYIoQ=      |
     | Secret Key        | +d62cBI73hxWcRPpput4RR7a8v8=      |
     
     When I select the CHANNEL GROUPS tab
     Then the CHANNEL GROUPS tab should have following channel group -
     | channel group     |
     | ENTERTAINMENT     |
     | MY GROUP          |

    Scenario: I should be able to see the list of all channels when I go to all channels tab.          
    Given the application has launched with following configuration in SETTINGS tab -
     |  name             |  value                            |
     | Organization ID   | a851c50193064ed6be08c7e75f8f4910  |
     | Access Key        | 6QDyPljwRS8L2w7Q7AnRo3sYIoQ=      |
     | Secret Key        | +d62cBI73hxWcRPpput4RR7a8v8=      |
     
     When I select the ALL CHANNELS tab
     Then the ALL CHANNELS tab should have following channel(s) -
     | channel(s)  |
     | Cartoon     |
     | Education   |
     | News        |
     | TV          |

    Scenario: I should be able to see the list of all media when I go to all media tab.   
    Given the application has launched with following configuration in SETTINGS tab -
     |  name             |  value                            |
     | Organization ID   | a851c50193064ed6be08c7e75f8f4910  |
     | Access Key        | 6QDyPljwRS8L2w7Q7AnRo3sYIoQ=      |
     | Secret Key        | +d62cBI73hxWcRPpput4RR7a8v8=      |
     
     When I select the ALL MEDIA tab
     Then the ALL MEDIA tab should have following media -
     | media                  |
     | Avengers Age of Ultron |
     | Underwater Marine Life |
     | Tom And Jerry          |
     | Act Of Caring          |

    Scenario: I should be able to see the list of channels belonging to channel group, on selecting that channel group in channel group tab.        
    Given the application has launched with following configuration in SETTINGS tab -
     |  name             |  value                            |
     | Organization ID   | a851c50193064ed6be08c7e75f8f4910  |
     | Access Key        | 6QDyPljwRS8L2w7Q7AnRo3sYIoQ=      |
     | Secret Key        | +d62cBI73hxWcRPpput4RR7a8v8=      |

     When I select ENTERTAINMENT as value for channel-group in CHANNEL GROUPS tab
     Then the CHANNELS tab should have following channel(s) -
     | channel(s)   |
     | Cartoon      |
     | Education    |

    Scenario: I should be able to see the list of media belonging to channel, on selecting that channel in channels tab.              
    Given the application has launched with following configuration in SETTINGS tab -
     |  name             |  value                            |
     | Organization ID   | a851c50193064ed6be08c7e75f8f4910  |
     | Access Key        | 6QDyPljwRS8L2w7Q7AnRo3sYIoQ=      |
     | Secret Key        | +d62cBI73hxWcRPpput4RR7a8v8=      |
     When I select ENTERTAINMENT as value for channel-group in CHANNEL GROUPS tab
      And I select Cartoon as value for channel in CHANNELS tab
     Then the MEDIA tab should have following media -
     | media          |
     | Toy Story      |
     | The Chipettes  |
     | The Cars       |
     | Pixars Up      |
     | Tom And Jerry  |

    Scenario: I should be able to refresh media list from all media tab.             
    Given the application has launched with following configuration in SETTINGS tab -
     |  name             |  value                            |
     | Organization ID   | a851c50193064ed6be08c7e75f8f4910  |
     | Access Key        | 6QDyPljwRS8L2w7Q7AnRo3sYIoQ=      |
     | Secret Key        | +d62cBI73hxWcRPpput4RR7a8v8=      |

     When I refresh the ALL MEDIA tab
     Then the ALL MEDIA tab should have following media -
     | media                  |
     | Avengers Age of Ultron |
     | Underwater Marine Life |
     | Tom And Jerry          |
     | Act Of Caring          |

    Scenario: I should be able to refresh channel list from all channels tab.     
    Given the application has launched with following configuration in SETTINGS tab -
     |  name             |  value                            |
     | Organization ID   | a851c50193064ed6be08c7e75f8f4910  |
     | Access Key        | 6QDyPljwRS8L2w7Q7AnRo3sYIoQ=      |
     | Secret Key        | +d62cBI73hxWcRPpput4RR7a8v8=      |

     When I refresh the ALL CHANNELS tab
     Then the ALL CHANNELS tab should have following channel(s) -
     | channel(s) |
     | Cartoon    |
     | Education  |
     | News       |
     | TV         |
     
    Scenario: I should be able to refresh channel group list from all channel groups tab.           
    Given the application has launched with following configuration in SETTINGS tab -
     |  name             |  value                            |
     | Organization ID   | a851c50193064ed6be08c7e75f8f4910  |
     | Access Key        | 6QDyPljwRS8L2w7Q7AnRo3sYIoQ=      |
     | Secret Key        | +d62cBI73hxWcRPpput4RR7a8v8=      |
     
     When I refresh the CHANNEL GROUPS tab
     Then the CHANNEL GROUPS tab should have following channel group -
     | channel group     |
     | ENTERTAINMENT     |
     | MY GROUP          |

    Scenario: I should be able to refresh channel list from channel tab.
    Given the application has launched with following configuration in SETTINGS tab -
     |  name             |  value                            |
     | Organization ID   | a851c50193064ed6be08c7e75f8f4910  |
     | Access Key        | 6QDyPljwRS8L2w7Q7AnRo3sYIoQ=      |
     | Secret Key        | +d62cBI73hxWcRPpput4RR7a8v8=      |
     
     When I select ENTERTAINMENT as value for channel-group in CHANNEL GROUPS tab
     And I refresh the CHANNELS tab
     Then the CHANNELS tab should have following channel(s) -
     | channel(s)   |
     | Cartoon      |
     | Education    |

    Scenario: I should be able to refresh  media list from media tab.           
    Given the application has launched with following configuration in SETTINGS tab -
     |  name             |  value                            |
     | Organization ID   | a851c50193064ed6be08c7e75f8f4910  |
     | Access Key        | 6QDyPljwRS8L2w7Q7AnRo3sYIoQ=      |
     | Secret Key        | +d62cBI73hxWcRPpput4RR7a8v8=      |

     When I select Cartoon as value for channel in ALL CHANNELS tab
     And I refresh the MEDIA tab       
     Then the MEDIA tab should have following media -
     | media          |
     | Toy Story      |
     | The Chipettes  |
     | The Cars       |
     | Pixars Up      |
     | Tom And Jerry  |

    Scenario: I should be able to fetch next set of media after scrolling down in all-media list.     
    Given the application has launched with following configuration in SETTINGS tab -
     |  name             |  value                            |
     | Organization ID   | a851c50193064ed6be08c7e75f8f4910  |
     | Access Key        | 6QDyPljwRS8L2w7Q7AnRo3sYIoQ=      |
     | Secret Key        | +d62cBI73hxWcRPpput4RR7a8v8=      |

     When I scroll-down the ALL MEDIA tab
     Then the ALL MEDIA tab should have following media -
     | media                   |
     | Now You See Me          |
     | Act Of Caring           |
     | Flash_MSS               |
     | Widewine_Offline        |
     | Widewine_Online_SD      |
     | Flash(DRM_30m)_HLS      |

    Scenario: I should be able to fetch next set of channel after scrolling down in all-channel list.              
    Given the application has launched with following configuration in SETTINGS tab -
     |  name             |  value                            |
     | Organization ID   | a851c50193064ed6be08c7e75f8f4910  |
     | Access Key        | 6QDyPljwRS8L2w7Q7AnRo3sYIoQ=      |
     | Secret Key        | +d62cBI73hxWcRPpput4RR7a8v8=      |
     
     When I scroll-down the ALL CHANNELS tab
     Then the ALL CHANNELS tab should have following media -
     | media      |
     | Cartoon    |
     | Education  |
     | News       |
     | TV         |

    Scenario: I should be able to fetch next set of channel group after scrolling down in channel-group list.    
    Given the application has launched with following configuration in SETTINGS tab -
     |  name             |  value                            |
     | Organization ID   | a851c50193064ed6be08c7e75f8f4910  |
     | Access Key        | 6QDyPljwRS8L2w7Q7AnRo3sYIoQ=      |
     | Secret Key        | +d62cBI73hxWcRPpput4RR7a8v8=      |

     When I scroll-down the CHANNEL GROUPS tab
     Then the CHANNEL GROUPS tab should have following channel group -
     | channel group  |
     | ENTERTAINMENT  |
     | MY GROUP       |

    Scenario: I should be able to fetch next set of channel after scrolling down in channel list.              
    Given the application has launched with following configuration in SETTINGS tab -
     |  name             |  value                            |
     | Organization ID   | a851c50193064ed6be08c7e75f8f4910  |
     | Access Key        | 6QDyPljwRS8L2w7Q7AnRo3sYIoQ=      |
     | Secret Key        | +d62cBI73hxWcRPpput4RR7a8v8=      |
     
     When I select MY GROUP as value for channel-group in CHANNEL GROUPS tab
     And I scroll-down the CHANNELS tab
     Then the CHANNELS tab should have following channel(s) -
     | channel(s) |
     | News       |
     | TV         |


    Scenario: I should be able to fetch next set of media after scrolling down in media list.     
    Given the application has launched with following configuration in SETTINGS tab -
     |  name             |  value                            |
     | Organization ID   | a851c50193064ed6be08c7e75f8f4910  |
     | Access Key        | 6QDyPljwRS8L2w7Q7AnRo3sYIoQ=      |
     | Secret Key        | +d62cBI73hxWcRPpput4RR7a8v8=      |
     
     When I select TV as value for channel in ALL CHANNELS tab
     And I scroll-down the MEDIA tab
     Then the MEDIA tab should have following media -
     | media          |
     | IP MAN         |
     | Jack Reacher   |
     | Fast Furious 6 |
     | Iron Sky       |
     | Gravity Scene  |
     
    Scenario: I should be able to see list of valid available encoding for a media.          
    Given the application has launched with following configuration in SETTINGS tab -
     |  name             |  value                            |
     | Organization ID   | a851c50193064ed6be08c7e75f8f4910  |
     | Access Key        | 6QDyPljwRS8L2w7Q7AnRo3sYIoQ=      |
     | Secret Key        | +d62cBI73hxWcRPpput4RR7a8v8=      |
     
     When I attempt-to-play the "Flash_HLS" video from ALL MEDIA with "no-select" encoding
     Then the POPUP should have following valid available encoding -
     | valid available encoding              |
     | Flash 128 X322kbps 264X640            |
     | HttpLiveStreaming 96 X409kbps 264X640 |
     | Flash 96 X128kbps 264X640             |
     | Flash 128 X409kbps 264X640            |
     And exit from the application