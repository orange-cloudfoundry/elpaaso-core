@north_api-releases
Feature: should be able to manage release using api (WSDL)
As a paas soap ws user
I want to create a release
In order to easily use it

Scenario: a pass user creates a release
Given an application exists
Given testuser is a paas user
When testuser creates a release with version g1r0c0 with description aDescription with versionControlURL http://www.blabla.com of that application
Then the release creation should succeed
When testuser creates a release with version g1r0c1 with description aDescription with versionControlURL http://www.blabla.com of that application
Then the release creation should succeed
When testuser finds all releases for that application
Then testuser should get a release with version g1r0c0 with description aDescription with versionControlURL http://www.blabla.com
Then testuser should get a release with version g1r0c1 with description aDescription with versionControlURL http://www.blabla.com

Scenario: a pass user fails to create a release of an unknown application
Given testuser is a paas user
When testuser creates a release with version g1r0c0 of an unknown application
Then the release creation should fail
Then the system should raise a application not found error

Scenario: a pass user retrieves the releaseId of the release he has previously created
Given an application exists
Given testuser is a paas user
When testuser searches for a release with version g1r0c0
Then the research should succeed

Scenario: a pass user retrieves a Release object (holding the status of the logical model)
Given an application exists
Given testuser is a paas user
Given a release exists
When testuser searches for a release previously created
Then the research of the Release should succeed

Scenario: an unkwown user should not be able to authenticate
Given an application exists
Given dummy is a not a paas user
When dummy creates a release with version g1r0c0 with description aDescription with versionControlURL http://www.blabla.com of that application
Then she should not be able to authenticate

Scenario: a guest user should not be authorized to create a release
Given an application exists
Given guest is a paas user with no role
When guest creates a release with version g1r0c0 with owner testuser with description aDescription with versionControlURL http://www.blabla.com of that application
Then she should not be authorized to perform this action