@north_api-environments
Feature: should be able to manage environment using api (WSDL)
As a paas soap ws user
I want to manage an environment
In order to easily use it

@api-env-wicketjpa @jonas-buildpack @war-deployed
Scenario: a pass user creates a cfWicketCxfJpa like environment
Given testuser is a paas user
Given a release with a cfWicketCxfJpa like logical deployment exists
When testuser creates an environment of type DEVELOPMENT with label aLabel for that release
Then that environment should be running within a delay of 10 minutes
When testuser gets details of all environments for that release
Then she should get environment details containing a label aLabel, a type DEVELOPMENT and a status RUNNING
When testuser stops that environment
Then that environment should be stopped within a delay of 5 minutes
When testuser starts that environment
Then that environment should be running within a delay of 5 minutes
When testuser stops that environment
Then that environment should be stopped within a delay of 5 minutes
When testuser deletes that environment
Then that environment should be removed within a delay of 5 minutes

Scenario: a pass user fails to create an environment for an unknow release
Given testuser is a paas user
When testuser creates an environment for an unknown release
Then she should fail to create that environment

Scenario: an unkwown user should not be able to authenticate
Given dummy is a not a paas user
Given a release with a cfWicketCxfJpa like logical deployment exists
When dummy creates an environment of type DEVELOPMENT with label aLabel for that release
Then she should not be able to authenticate

Scenario: a guest user should not be authorized to create a cfWicketCxfJpa like environment
Given guest is a paas user with no role
Given a release with a cfWicketCxfJpa like logical deployment exists
When guest creates an environment of type DEVELOPMENT with label aLabel for that release
Then she should not be authorized to perform this action





