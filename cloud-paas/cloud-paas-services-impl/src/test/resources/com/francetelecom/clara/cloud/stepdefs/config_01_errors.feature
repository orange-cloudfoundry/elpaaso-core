@config
Feature: error cases

  Scenario Outline: initial env creation with invalid release config override (overriding unknown key) generates warnings.
    Values are not validated, have no associated type. An empty value is valid.
    If some config could be dangerous to enable, architecture should not expose them in the contract.
    
    As an app team,
    In order to avoid mistakes while config properties are renamed across releases
    I need to be warned if I try to override a config key which does not exist

    Given the following contract in the architecture:
      | config_set  | key                         | value | comment                                           |
      | feature     | monitoring.features.enabled | true  | (De)Activate risky features from monitoring       |
      | credentials | maven.pull.repository.url   |       | Main repo to pull artefact from to deploy on PaaS |
    When instantiating a new environment, I request to override the <config_set> with key named <key> and an overriden value <value> and an associated comment <comment>
    Then my config override request is <request_status> with the potential <warning> message

    Examples: 
      | config_set          | key                         | value          | comment                                      | request_status | warning                                                           |
      | feature             | monitoring.features.enabled | false          | speeding up env instantiation                | accepted       |                                                                   |
      | feature             | monitoring.features.enabled |                | testing empty value breaks at paas startup   | accepted       |                                                                   |
      | feature             | monitoring.features.enabled | 1234           | testing integer value breaks at paas startup | accepted       |                                                                   |
      | feature             | MONITORING.FEATURES.ENABLED | https://n.org/ |                                              | rejected       | no such key 'MONITORING.FEATURES.ENABLED' in config set 'feature' |
      | credentials         | maven.repositoryUrl         | https://n.org/ |                                              | rejected       | no such key 'maven.repositoryUrl' in config set 'credentials'     |
#      | unexistentconfigset | maven.repositoryUrl         | https://n.org/ |                                              | rejected       | no such config set 'unexistentconfigset'                          |
#      |                     | maven.repositoryUrl         | https://n.org/ |                                              | rejected       | no such config set ''                                             |
      | feature             | dangerous.feature.enabled   | true           | trying to enable beta feature in production  | rejected       | no such key 'dangerous.feature.enabled' in config set 'feature'   |
      | feature             | foo                         | bar            | trying to enable beta feature in production  | rejected       | no such key 'foo' in config set 'feature'                         |
