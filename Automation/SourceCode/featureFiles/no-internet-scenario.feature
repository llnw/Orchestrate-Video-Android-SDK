Feature: Check the no internet scenarios
    Scenario: Error Message should be displayed when we select channel group tab while no connectivity.
      Given the application has launched with following configuration in SETTINGS tab -
         |  name             |  value                            |
         | Organization ID   | a851c50193064ed6be08c7e75f8f4910  |
         | Access Key        | 6QDyPljwRS8L2w7Q7AnRo3sYIoQ=      |
         | Secret Key        | +d62cBI73hxWcRPpput4RR7a8v8=      |
      When I apply internet off on the device
       And I refresh the CHANNEL GROUPS tab
      Then the CURRENT tab should have following error message -
         | error message            |
         | Device Not Connected !   |
      When I apply internet on on the device
       And I refresh the CHANNEL GROUPS tab
      Then the CURRENT tab should not have following error message -
         | error message            |
         | Device Not Connected !   |

    Scenario: Error Message should be displayed when we select all channel tab while no connectivity.
      Given the application has launched with following configuration in SETTINGS tab -
         |  name             |  value                            |
         | Organization ID   | a851c50193064ed6be08c7e75f8f4910  |
         | Access Key        | 6QDyPljwRS8L2w7Q7AnRo3sYIoQ=      |
         | Secret Key        | +d62cBI73hxWcRPpput4RR7a8v8=      |
      When I apply internet off on the device
       And I refresh the ALL CHANNELS tab
      Then the CURRENT tab should have following error message -
         | error message            |
         | Device Not Connected !   |
      When I apply internet on on the device
       And I refresh the ALL CHANNELS tab
      Then the CURRENT tab should not have following error message -
         | error message            |
         | Device Not Connected !   |

    Scenario: Error Message should be displayed when we select all media tab while no connectivity.
      Given the application has launched with following configuration in SETTINGS tab -
         |  name             |  value                            |
         | Organization ID   | a851c50193064ed6be08c7e75f8f4910  |
         | Access Key        | 6QDyPljwRS8L2w7Q7AnRo3sYIoQ=      |
         | Secret Key        | +d62cBI73hxWcRPpput4RR7a8v8=      |
      When I apply internet off on the device
       And I refresh the ALL MEDIA tab
      Then the CURRENT tab should have following error message -
         | error message            |
         | Device Not Connected !   |
      When I apply internet on on the device
       And I refresh the ALL MEDIA tab
      Then the CURRENT tab should not have following error message -
         | error message            |
         | Device Not Connected !   |

    Scenario: Error Message should be displayed on channel tab when we select a channel group tab while no connectivity.
      Given the application has launched with following configuration in SETTINGS tab -
         |  name             |  value                            |
         | Organization ID   | a851c50193064ed6be08c7e75f8f4910  |
         | Access Key        | 6QDyPljwRS8L2w7Q7AnRo3sYIoQ=      |
         | Secret Key        | +d62cBI73hxWcRPpput4RR7a8v8=      |

      When I apply internet off on the device
       And I select ENTERTAINMENT as value for channel-group in CHANNEL GROUPS tab
      Then the CURRENT tab should have following error message -
         | error message            |
         | Device Not Connected !   |
      When I apply internet on on the device
       And I select ENTERTAINMENT as value for channel-group in CHANNEL GROUPS tab
      Then the CURRENT tab should not have following error message -
         | error message            |
         | Device Not Connected !   |

    Scenario: Error Message should be displayed on media tab when we select a channel while no connectivity.
      Given the application has launched with following configuration in SETTINGS tab -
         |  name             |  value                            |
         | Organization ID   | a851c50193064ed6be08c7e75f8f4910  |
         | Access Key        | 6QDyPljwRS8L2w7Q7AnRo3sYIoQ=      |
         | Secret Key        | +d62cBI73hxWcRPpput4RR7a8v8=      |

      When I apply internet off on the device
       And I select Cartoon as value for channel in ALL CHANNELS tab
      Then the CURRENT tab should have following error message -
         | error message            |
         | Device Not Connected !   |
      When I apply internet on on the device
       And I select Cartoon as value for channel in ALL CHANNELS tab
      Then the CURRENT tab should not have following error message -
         | error message            |
         | Device Not Connected !   |

    Scenario: Player should able to play the media after the internet connection has been on
     Given the application has launched with following configuration in SETTINGS tab -
         |  name             |  value                            |
         | Organization ID   | a851c50193064ed6be08c7e75f8f4910  |
         | Access Key        | 6QDyPljwRS8L2w7Q7AnRo3sYIoQ=      |
         | Secret Key        | +d62cBI73hxWcRPpput4RR7a8v8=      |
      When I play the "Adobe" video from ALL MEDIA with "automatic" encoding
       And I pause the "Adobe" video from ALL MEDIA with "automatic" encoding
       And I apply internet off on the device
       And I apply internet on on the device
       And I resume the "Adobe" video from ALL MEDIA with "automatic" encoding
      Then player should resume the playback from remote to duration 00:00 in play state

    Scenario: Analytics server should not show events which are performed during the internet off
     Given the application has launched with following configuration in SETTINGS tab -
         |  name             |  value                            |
         | Organization ID   | a851c50193064ed6be08c7e75f8f4910  |
         | Access Key        | 6QDyPljwRS8L2w7Q7AnRo3sYIoQ=      |
         | Secret Key        | +d62cBI73hxWcRPpput4RR7a8v8=      |
      When I play the "Adobe" video from ALL MEDIA with "automatic" encoding
       And I apply internet off on the device
       And I pause the "Adobe" video from ALL MEDIA with "automatic" encoding
       And I resume the "Adobe" video from ALL MEDIA with "automatic" encoding
      Then the player should send expected notification

    Scenario: Analytics server should show events which are performed during the internet off after when it is on
     Given the application has launched with following configuration in SETTINGS tab -
         |  name             |  value                            |
         | Organization ID   | a851c50193064ed6be08c7e75f8f4910  |
         | Access Key        | 6QDyPljwRS8L2w7Q7AnRo3sYIoQ=      |
         | Secret Key        | +d62cBI73hxWcRPpput4RR7a8v8=      |
      When I play the "Widewine_Offline" video from ALL MEDIA with "WidevineOffline 10 X50kbps 240X572" encoding
       And I apply internet off on the device
       And I pause the "Widewine_Offline" video from ALL MEDIA with "WidevineOffline 10 X50kbps 240X572" encoding
       And I resume the "Widewine_Offline" video from ALL MEDIA with "WidevineOffline 10 X50kbps 240X572" encoding
       And I apply internet on on the device
      Then the player should send expected notification