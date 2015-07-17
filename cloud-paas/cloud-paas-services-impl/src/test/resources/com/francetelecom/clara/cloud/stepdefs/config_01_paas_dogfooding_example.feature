@config
Feature: initial env creation with release config override (simple single jeeprocessing and config set), paas dogfooding example

  As a app-dev team,
  In order to workaround the lack of support in the service catalogs for external WSC targetting REST API, or TCP protocols
  I need to specify different remote dependent component ip/port/credentials on each environment

  As a app-qa team,
  In order to QA some features that run on a periodic basis
  I need to tune technical properties so that they occur sooner on QA environment that on prod environment

  Background:
    Given the following contract in the architecture:
      | config_set | key                         | value          | comment                                                                                                       |
      | config     | splunk.portal.ip            |                | the splunk instance which collects logs of new environment instantiated from this platform. No default value. |
      | config     | monitoring.features.enabled | true           | (De)Activate risky features from monitoring                                                                   |
      | config     | schedule.databasePurge.cron | 0 0 */08 * * ? | purge schedule : default is repeating every 8 hours                                                           |

  Scenario: dev-env
    When instantiating environment "dev-env", I request the following override:
      | configSet  | key                         | value                            | comment                                                                          |
      | config     | splunk.portal.ip            | log.elpaaso.org | using elpaaso-logs so dev credentials don't leak to other app-dev through splunk |
      | config     | monitoring.features.enabled | false                            | speed up environment instantiations in qa                                        |
    Then the following environment config is available for review, and bound to the application:
      | is_overriden | config_set | key                         | value                            | comment                                                                                                                |
      | true         | config     | splunk.portal.ip            | log.elpaaso.org | not using splunk.elpaaso.org to not expose other credentials in logs until splunk ACL get put in place |
      | true         | config     | monitoring.features.enabled | false                            | speed up environment instantiations                                                                                    |
      | false        | config     | schedule.databasePurge.cron | 0 0 */08 * * ?                   | purge schedule : default is repeating every 8 hours                                                                    |

  Scenario: qa-env
    When instantiating environment "qa-env", I request the following override:
      | configSet  | key                         | value                            | comment                                             |
      | config     | splunk.portal.ip            | log.elpaaso.org |                                                     |
      | config     | schedule.databasePurge.cron | 0 0 * */01 * ?                   | testing db purge for release 1.12: cron every 1 min |
    Then the following environment config is available for review, and bound to the application:
      | is_overriden | config_set | key                         | value                            | comment                                             |
      | true         | config     | splunk.portal.ip            | log.elpaaso.org |                                                     |
      | true         | config     | schedule.databasePurge.cron | 0 0 * */01 * ?                   | purge schedule : default is repeating every 8 hours |
      | false        | config     | monitoring.features.enabled | true                             | (De)Activate risky features from monitoring         |

  Scenario: prod-env
    When instantiating environment "prod-env", I request the following override:
      | configSet  | key              | value                            | comment |
      | config     | splunk.portal.ip | log.elpaaso.org |         |
    Then the following environment config is available for review, and bound to the application:
      | is_overriden | config_set | key                         | value                            | comment                                             |
      | true         | config     | splunk.portal.ip            | log.elpaaso.org |                                                     |
      | false        | config     | monitoring.features.enabled | true                             | (De)Activate risky features from monitoring         |
      | false        | config     | schedule.databasePurge.cron | 0 0 */08 * * ?                   | purge schedule : default is repeating every 8 hours |
