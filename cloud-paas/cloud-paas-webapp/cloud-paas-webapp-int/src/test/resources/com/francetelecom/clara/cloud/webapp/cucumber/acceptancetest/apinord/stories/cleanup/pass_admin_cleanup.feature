@north_api-cleanup
Feature: should be able to cleanup an existing ElPaaso platform using api (WSDL)
As a paas admin ws user
I want to delete all existing environments
I want to delete all existing releases
I want to delete all existing applications
In order to run all tests on a clean platform

Scenario: a pass admin deletes any existing environments, releases and applications
Given adminuser is a paas user
When adminuser deletes all environments,releases and applications
Then application count is 0 and release count is 0 and environment count is 0

