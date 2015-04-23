Feature: Configuring the Settings           
    Scenario: I should be able to specify the organization id, Access key and Secret key on the settings page.
    Given the application has launched
     When I set 17fbbde5ce1e43e087bcbfffbf0680ca as value for Organization ID in SETTINGS page
     Then value of Organization ID in SETTINGS page should be 17fbbde5ce1e43e087bcbfffbf0680ca
     When I set sB8TWEftj8ZhYXol7pVWFuutJ4M= as value for Access Key in SETTINGS page
     Then value of Access Key in SETTINGS page should be sB8TWEftj8ZhYXol7pVWFuutJ4M=
     When I set lR46aeyMmlgTOod17Kl9XqNgaYE= as value for Secret Key in SETTINGS page
     Then value of Secret Key in SETTINGS page should be lR46aeyMmlgTOod17Kl9XqNgaYE=   

    Scenario: I should be able to set the log level in the settings page.
    Given the application has launched
     When I select INFO as value for Log level in SETTINGS page
     Then value of Log level in SETTINGS page should be INFO
     Then exit from the application