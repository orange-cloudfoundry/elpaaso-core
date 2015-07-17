@north_api-middleware_profile
Feature: should be able to manage middleware profile using north api
As a paas soap ws user
I want to be able to manipulate middleware profile in my ws calls
In order to activate different profiles on ElPaaso

Scenario: a pass user can list all available middleware profiles
Given testuser is a paas user
When testuser list all middleware profiles
Then he should see profile 2.0.0-cf with status SUPPORTED

Scenario: a pass user create a release specifying a middleware profile version
Given testuser is a paas user
Given an application exists
When testuser creates a release with version g1r0c0 with owner testuser with middleware profile 1.4.0 of that application
Then the release creation should succeed

Scenario: a pass user can create a release with unknown profile version
Given testuser is a paas user
Given an application exists with description application for bad profile 0.0.0
When testuser creates a release with version g1r0c0 with owner testuser with middleware profile 0.0.0 of that application
Then the release creation should succeed