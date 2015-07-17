@gui-environments
Feature: should be able to manage environment using gui
As a paas gui user
I want to create an environment
In order to easily use it

  @gui-env-jeeprobe @jonas-buildpack @ear-deployed
  Scenario: a pass user creates an environment for release MyJeeProbeSample - G00R01
    Given testuser is a paas user
    And testuser has created a jeeProbe application with its MyJeeProbeSample - G00R01 release using populate page
    When testuser creates an environment of type DEVELOPMENT for release MyJeeProbeSample - G00R01 with label test-jeeprobe
    Then the environment should be operational within 10 minutes


  @gui-env-simpleprobe @jonas-buildpack @jar-deployed
  Scenario: a pass user creates an environment for release MySimpleProbeSample - G00R01
    Given testuser is a paas user
    And testuser has created a SimpleProbe application with its MySimpleProbeSample - G00R01 release using populate page
    When testuser creates an environment of type DEVELOPMENT for release MySimpleProbeSample - G00R01 with label test-simple-probe
    Then the environment should be operational within 10 minutes
