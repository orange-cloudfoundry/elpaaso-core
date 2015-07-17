@north_api-applications
Feature: should be able to manage application using api (WSDL)
As a paas soap ws user
I want to create an application
In order to easily use it

Scenario: a PaaS admin user creates an application
Given testuser is a paas admin user
When testuser creates an application with code aCode and label aLabel
Then the application creation should succeed
When testuser creates an application with code anotherCode and label anotherLabel
Then the application creation should succeed
When testuser searches for application with label aLabel
Then the research of the application should succeed
When testuser searches for all applications
Then she should get application with code aCode and label aLabel
Then she should get application with code anotherCode and label anotherLabel

Scenario: a paas admin user fails to create an application with null code
Given testuser is a paas admin user
When testuser creates an application with null code
Then the application creation should fail

Scenario: a paas admin user fails to create an application with empty code
Given testuser is a paas admin user
When testuser creates an application with empty code
Then the application creation should fail

Scenario: a paas admin user fails to create an application with null label
Given testuser is a paas admin user
When testuser creates an application with null label
Then the application creation should fail

Scenario: a paas admin user fails to create an application with empty label
Given testuser is a paas admin user
When testuser creates an application with empty label
Then the application creation should fail

Scenario: a paas admin user fails to create an application with existing code
Given testuser is a paas admin user
When testuser creates an application with code aCode and label newLabel
Then the application creation should fail because application already exists

Scenario: a paas admin user fails to create an application with existing label
Given testuser is a paas admin user
When testuser creates an application with code newCode and label aLabel
Then the application creation should fail because application already exists

Scenario: an unkwown user (not existing in LDAP) should not be able to authenticate 
Given dummy is a not a paas user
When dummy creates an application with code aCode and label aLabel
Then she should not be able to authenticate

Scenario: an anonymous user with no paas role should not be should not be able to authenticate
Given anonymous exists in Ldap with no paas role
When anonymous creates an application with code aNoRoleCode and label aNoRoleLabel
Then the application creation should fail as anonymous is unknown


Scenario: a guest user should  to create an application
Given guest is a paas user
When guest creates an application with code aGuestCode and label aGuestLabel
Then the application creation should succeed
