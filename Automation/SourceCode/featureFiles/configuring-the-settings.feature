Feature: Configuring the Settings
    Scenario: Check settings page has default values for Organisation Id, Access Key,Secret key,URL,License Proxy, Portal Key when it is launched.
        Given the application has launched
        Then value of Organization ID in SETTINGS tab should be a851c50193064ed6be08c7e75f8f4910
         And value of Access Key in CURRENT tab should be 6QDyPljwRS8L2w7Q7AnRo3sYIoQ=
         And value of Secret Key in CURRENT tab should be +d62cBI73hxWcRPpput4RR7a8v8=
         And value of URL in CURRENT tab should be https://staging-api.lvp.llnw.net/rest
         And value of License Proxy in CURRENT tab should be https://staging-wlp.lvp.llnw.net/license
         And value of Portal Key in CURRENT tab should be limelight

        
    Scenario: I should be able to specify the organization id, Access key and Secret key on the settings tab.
        Given the application has launched
        When I set a851c50193064ed6be08c7e75f8f4910 as value for Organization ID in SETTINGS tab
        Then value of Organization ID in CURRENT tab should be a851c50193064ed6be08c7e75f8f4910
        When I set 6QDyPljwRS8L2w7Q7AnRo3sYIoQ= as value for Access Key in SETTINGS tab
        Then value of Access Key in CURRENT tab should be 6QDyPljwRS8L2w7Q7AnRo3sYIoQ=
        When I set +d62cBI73hxWcRPpput4RR7a8v8= as value for Secret Key in SETTINGS tab
        Then value of Secret Key in CURRENT tab should be +d62cBI73hxWcRPpput4RR7a8v8=   

    Scenario: Should be able to set web service end point
        Given the application has launched
        When I set https://staging-api.lvp.llnw.net/rest as value for URL in SETTINGS tab
        Then value of URL in CURRENT tab should be https://staging-api.lvp.llnw.net/rest

    Scenario: Should be able to set Widevine service license proxy URL and portal key
        Given the application has launched
        When I set https://staging-wlp.lvp.llnw.net/license as value for License Proxy in SETTINGS tab
        Then value of License Proxy in CURRENT tab should be https://staging-wlp.lvp.llnw.net/license
        When I set limelight as value for Portal Key in SETTINGS tab
        Then value of Portal Key in CURRENT tab should be limelight

    Scenario: I should be able to set the log level in the settings tab.
        Given the application has launched
        When I select INFO as value for Log level in SETTINGS tab
        Then value of Log level in CURRENT tab should be INFO
        And exit from the application