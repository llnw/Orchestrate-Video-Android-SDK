Feature: Setting up the configurations for the application
 
  Scenario: Should be able to set the organization details 
    Given the application has launched
     When I set 17fbbde5ce1e43e087bcbfffbf0680ca as value for Organization ID in SETTINGS page
     Then value of Organization ID in SETTINGS page should be 17fbbde5ce1e43e087bcbfffbf0680ca
     When I set sB8TWEftj8ZhYXol7pVWFuutJ4M= as value for Access Key in SETTINGS page
     Then value of Access Key in SETTINGS page should be sB8TWEftj8ZhYXol7pVWFuutJ4M=
     When I set lR46aeyMmlgTOod17Kl9XqNgaYE= as value for Secret Key in SETTINGS page
     Then value of Secret Key in SETTINGS page should be lR46aeyMmlgTOod17Kl9XqNgaYE=
     
   Scenario: Should be able to set web service end point
    Given the application has launched
     When I set https://staging-api.lvp.llnw.net/rest as value for URL in SETTINGS page
     Then value of URL in SETTINGS page should be https://staging-api.lvp.llnw.net/rest
  
   Scenario: Should be able to set Widevine service license proxy URL and portal key
    Given the application has launched
     When I set https://staging-wlp.lvp.llnw.net/license as value for License Proxy in SETTINGS page
     Then value of License Proxy in SETTINGS page should be https://staging-wlp.lvp.llnw.net/license
     When I set limelight as value for Portal Key in SETTINGS page
     Then value of Portal Key in SETTINGS page should be limelight
   
   Scenario: Should be able to set others data
    Given the application has launched
     When I select INFO as value for Log level in SETTINGS page
     Then value of Log level in SETTINGS page should be INFO
     Then exit from the application