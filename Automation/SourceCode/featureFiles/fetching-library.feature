Feature: Fetching Library 
    Scenario: I should be able to see the list of channel groups (associated with the organization id) when I go to channel group tab.         
    Given the application has launched
     When I select the CHANNEL GROUPS tab
     Then the CURRENT tab should have following channel group -
     | channel group        |
     | Rebaca Channel Group |
     | Entertainment        |

    Scenario: I should be able to see the list of all channels when I go to all channels tab.          
    Given the application has launched
     When I select the ALL CHANNELS tab
     Then the CURRENT tab should have following channel(s) -
     | channel(s)         |
     | Some Sample Videos |
     | Protected Content  |

    Scenario: I should be able to see the list of all media when I go to all media tab.   
    Given the application has launched
     When I select the ALL MEDIA tab
     Then the CURRENT tab should have following media -
     | media                  |
     | Tears of Steel Trailer |
     | Late For Work          |

    Scenario: I should be able to see the list of channels belonging to channel group, on selecting that channel group in channel group tab.        
    Given the application has launched
     When I select Rebaca Channel Group as value for channel-group in CHANNEL GROUPS page
     Then the CURRENT tab should have following channel(s) -
     | channel(s)           |
     | Some Sample Videos   |
     | Maru Madnes          |

    Scenario: I should be able to see the list of media belonging to channel, on selecting that channel in channels tab.              
    Given the application has launched
     When I select Some Sample Videos as value for channel in CHANNELS page
     Then the CURRENT tab should have following media -
     | media                   |
     | Sintel Trailer          |
     | Tears of Steel Trailer  |
     | 5ms of Black            |     

    Scenario: I should be able to refresh media list from all media tab.             
    Given the application has launched
     When I refresh the ALL MEDIA tab
     Then the CURRENT tab should have following media -
     | media                  |
     | Tears of Steel Trailer |
     | Late For Work          |
     | Code Rush              |
     | Sintel Trailer         |

    Scenario: I should be able to refresh channel list from all channels tab.     
    Given the application has launched
     When I refresh the ALL CHANNELS tab
     Then the CURRENT tab should have following channel(s) -
     | channel(s)         |
     | Some Sample Videos |
     | Protected Content  |
     | Maru Madnes        |

    Scenario: I should be able to refresh channel group list from all channel groups tab.           
    Given the application has launched
     When I refresh the CHANNEL GROUPS tab
     Then the CURRENT tab should have following channel group -
     | channel group        |
     | Rebaca Channel Group |
     | Entertainment        |

    Scenario: I should be able to refresh channel list from channel tab.
    Given the application has launched
     When I select Rebaca Channel Group as value for channel-group in CHANNEL GROUPS page
     And I refresh the CHANNELS tab
     Then the CURRENT tab should have following channel(s) -
     | channel(s)           |
     | Some Sample Videos   |
     | Maru Madnes          |

    Scenario: I should be able to refresh  media list from media tab.           
    Given the application has launched
     When I select Some Sample Videos as value for channel in ALL CHANNELS page
     And I refresh the MEDIA tab       
     Then the CURRENT tab should have following media -
     | media                   |
     | Sintel Trailer          |
     | Tears of Steel Trailer  |
     | 5ms of Black            |

    Scenario: I should be able to fetch next set of media after scrolling down in all-media list.     
    Given the application has launched
     When I scroll-down the ALL MEDIA tab
     Then the CURRENT tab should have following media -
     | media                   |
     | Asiabanking             |
     | Jaguar_All              |
     | 5ms of Black            |

    Scenario: I should be able to fetch next set of channel after scrolling down in all-channel list.              
    Given the application has launched
     When I scroll-down the ALL CHANNELS tab
     Then the CURRENT tab should have following media -
     | media                   |
     | Some Sample Videos      |
     | Protected Content       |
     | Maru Madnes             |

    Scenario: I should be able to fetch next set of channel group after scrolling down in channel-group list.    
    Given the application has launched
     When I scroll-down the CHANNEL GROUPS tab
     Then the CURRENT tab should have following channel group -
     | channel group        |
     | Rebaca Channel Group |
     | Entertainment        |

    Scenario: I should be able to fetch next set of media after scrolling down in media list.     
    Given the application has launched
     When I select Some Sample Videos as value for channel in ALL CHANNELS page
     And I scroll-down the MEDIA tab
     Then the CURRENT tab should have following media -
     | media                   |
     | Sintel Trailer          |
     | Tears of Steel Trailer  |
     | 5ms of Black            |

    Scenario: I should be able to fetch next set of channel after scrolling down in channel list.              
    Given the application has launched
     When I select Rebaca Channel Group as value for channel-group in CHANNEL GROUPS page
     And I scroll-down the CHANNELS tab
     Then the CURRENT tab should have following channel(s) -
     | channel(s)           |
     | Some Sample Videos   |
     | Maru Madnes          |

    Scenario: I should be able to see list of valid available encoding for a media.          
    Given the application has launched
     When I attempt-to-play the "Sintel Trailer" video from ALL MEDIA with "no-select" encoding
     Then the POPUP should have following valid available encoding -
     | valid available encoding              |
     | Flash 128 X322kbps 270X480            |
     | Flash 128 X472kbps 270X480            |
     | Flash 128 X538kbps 480X854            |
     | Flash 96 X128kbps 270X480             |
     | Widevine 128 X538kbps 480X854         |
     | Widevine 40 X538kbps 360X640          |
     | Widevine 40 X400kbps 360X640          |
     | WidevineOffline 128 X538kbps 480X854  |
     | WidevineOffline 128 X538kbps 360X640  |
     | HttpLiveStreaming 96 X200kbps 224X398 |
     | HttpLiveStreaming 96 X400kbps 224X398 |
     | HttpLiveStreaming 96 X538kbps 360X640 |
     | HttpLiveStreaming 64 X0kbps 0X0       |
     | HttpLiveStreaming 96 X110kbps 224X398 |
     | Mobile3gp 64 X128kbps 144X256         |
     | MobileH264 128 X322kbps 360X640       |
     And exit from the application