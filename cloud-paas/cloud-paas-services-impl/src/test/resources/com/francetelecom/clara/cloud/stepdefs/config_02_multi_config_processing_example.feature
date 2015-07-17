@config
Feature: initial env creation with release config override.

  Background:
  The dbaas application illustrates multiple config sets and jee processings.

    Given the following contract in the architecture:
      | config_set      | key                        | value     | comment                                                                                                                           |
      | dbaas-ui-config | DBaaSCore.connectTimeoutMs | 1000      | Number of MS to wait for the TCP socket to connect to the core SOAP API. Note host/port are injected through internal WSC instead |
      | dbaas-ws-config | ldap.url                   | change_me | the LDAP url                                                                                                                      |
      | dbaas-ws-config | ldap.enable                | false     | ldap is disabled by default, users authenticate through the db by default                                                         |
      | dbaas-features  | replication.enable         | false     | by default this galera experimental feature is disabled on both ui and core (i.e. SOAP API)                                       |

    And the following architecture config bindings to processing services:
      | jee_processing | config_set      |
      | dbaas-ui       | dbaas-ui-config |
      | dbaas-ui       | dbaas-features  |
      | dbaas-ws       | dbaas-ws-config |
      | dbaas-ws       | dbaas-features  |

  Scenario: dev-env not overriding configs.
  Default config defined in the architecture are applied.

    When instantiating environment "test", I request the following override:
      | config_set | key | value | comment |

    Then the following environment config is available for review, and bound to the application:
      | is_overriden | config_set      | jee_processing | key                        | value     | comment                                                                                                                           |
      | false        | dbaas-ui-config | dbaas-ui       | DBaaSCore.connectTimeoutMs | 1000      | Number of MS to wait for the TCP socket to connect to the core SOAP API. Note host/port are injected through internal WSC instead |
      | false        | dbaas-ws-config | dbaas-ws       | ldap.url                   | change_me | the LDAP url                                                                                                                      |
      | false        | dbaas-ws-config | dbaas-ws       | ldap.enable                | false     | ldap is disabled by default, users authenticate through the db by default                                                         |
      | false        | dbaas-features  | dbaas-ws       | replication.enable         | false     | by default this galera experimental feature is disabled on both ui and core (i.e. SOAP API)                                       |
      | false        | dbaas-features  | dbaas-ui       | replication.enable         | false     | by default this galera experimental feature is disabled on both ui and core (i.e. SOAP API)                                       |

  Scenario: dev-env overriding a config-set bound to many jeeprocessings

    When instantiating environment "test", I request the following override:
      | configSet      | key                | value | comment             |
      | dbaas-features | replication.enable | true  | testing replication |

    Then the following environment config is available for review, and bound to the application:
      | is_overriden | config_set      | jee_processing | key                        | value     | comment                                                                                                                           |
      | false        | dbaas-ui-config | dbaas-ui       | DBaaSCore.connectTimeoutMs | 1000      | Number of MS to wait for the TCP socket to connect to the core SOAP API. Note host/port are injected through internal WSC instead |
      | false        | dbaas-ws-config | dbaas-ws       | ldap.url                   | change_me | the LDAP url                                                                                                                      |
      | false        | dbaas-ws-config | dbaas-ws       | ldap.enable                | false     | ldap is disabled by default, users authenticate through the db by default                                                         |
      | true         | dbaas-features  | dbaas-ws       | replication.enable         | true      | testing replication                                                                                                               |
      | true         | dbaas-features  | dbaas-ui       | replication.enable         | true      | testing replication                                                                                                               |


  Scenario: copy/paste config overriding among environment
  As an app-ops team
  In order to reuse config overrides among environments while upgrading releases
  I need to be able to copy/paste config overrides among environments

    Given an environment live12 was instantiated with the following config override:
      | config_set      | key         | value                | comment                                                                 |
      | dbaas-ws-config | ldap.url    | ldap://ad.domain.org | using corporate ActiveDirectory                                         |
      | dbaas-ws-config | ldap.enable | true                 | enabling ldap, previous db registered users have been notified by email |
    When I request to display the environment live12 config override exported as text
    Then the environment live12 config override exported as text is
    """
      #using corporate ActiveDirectory
      dbaas-ws-config.ldap.url= ldap://ad.domain.org
      #enabling ldap, previous db registered users have been notified by email
      dbaas-ws-config.ldap.enable= true
      """
    When instantiating a new environment live13, I request the following override as text:
    """
      #using corporate ActiveDirectory
      dbaas-ws-config.ldap.url= ldap://ad.domain.org
      #enabling ldap, previous db registered users have been notified by email
      dbaas-ws-config.ldap.enable= true
      """
    Then the environment live13 config override is:
      | config_set      | key         | value                | comment                                                                 |
      | dbaas-ws-config | ldap.url    | ldap://ad.domain.org | using corporate ActiveDirectory                                         |
      | dbaas-ws-config | ldap.enable | true                 | enabling ldap, previous db registered users have been notified by email |
    And the environment live13 config is:
      | is_overriden | config_set      | jee_processing | key                        | value                | comment                                                                                                                           |
      | true         | dbaas-ws-config | dbaas-ws       | ldap.url                   | ldap://ad.domain.org | the LDAP url                                                                                                                      |
      | true         | dbaas-ws-config | dbaas-ws       | ldap.enable                | true                 | ldap is disabled by default, users authenticate through the db by default                                                         |
      | false        | dbaas-ui-config | dbaas-ui       | DBaaSCore.connectTimeoutMs | 1000                 | Number of MS to wait for the TCP socket to connect to the core SOAP API. Note host/port are injected through internal WSC instead |
      | false        | dbaas-features  | dbaas-ws       | replication.enable         | false                | by default this galera experimental feature is disabled on both ui and core (i.e. SOAP API)                                       |
      | false        | dbaas-features  | dbaas-ui       | replication.enable         | false                | by default this galera experimental feature is disabled on both ui and core (i.e. SOAP API)                                       |

  Scenario: editing an environment config override
  The modified config override will be applied once the environment is restarted.
  Each config override modification adds up an entry in the environment comment.

  As an app team
  In order to activate turn features on an existing environment with associated state and avoid recreate a new environment to change its config
  I need to edit config override on an existing environment

    Given an environment qa14 was instantiated with the following config override:
      | config_set      | key         | value                | comment                                                                 |
      | dbaas-ws-config | ldap.url    | ldap://ad.domain.org | using corporate ActiveDirectory                                         |
      | dbaas-ws-config | ldap.enable | true                 | enabling ldap, previous db registered users have been notified by email |

    When I request to edit the environment qa14 config override to:
      | config_set      | key                | value                | comment                                                                 |
      | dbaas-ws-config | ldap.url           | ldap://ad.domain.org | using corporate ActiveDirectory                                         |
      | dbaas-ws-config | ldap.enable        | true                 | enabling ldap, previous db registered users have been notified by email |
      | dbaas-features  | replication.enable | true                 | testing db replication for release 14                                   |

    Then the environment qa14 config is:
      | is_overriden | config_set      | jee_processing | key                        | value                | comment                                                                                                                           |
      | true         | dbaas-ws-config | dbaas-ws       | ldap.url                   | ldap://ad.domain.org | the LDAP url                                                                                                                      |
      | true         | dbaas-ws-config | dbaas-ws       | ldap.enable                | true                 | ldap is disabled by default, users authenticate through the db by default                                                         |
      | true         | dbaas-features  | dbaas-ws       | replication.enable         | true                 | testing db replication for release 14                                                                                             |
      | true         | dbaas-features  | dbaas-ui       | replication.enable         | true                 | testing db replication for release 14                                                                                             |
      | false        | dbaas-ui-config | dbaas-ui       | DBaaSCore.connectTimeoutMs | 1000                 | Number of MS to wait for the TCP socket to connect to the core SOAP API. Note host/port are injected through internal WSC instead |

  Scenario: editing an environment config override adds up an entry in the environment comment log.

  As an app-ops team
  In order to collaborate with multiple team member on the same environment
  I need to know when an environment config override was edited by someone else and when.
  In order to know if a config overide was applied on an environment
  I need to know if an environment restart was applied following a config override



    Given an environment prod14 was instantiated with the following config override:
      | config_set     | key                | value | comment                               |
      | dbaas-features | replication.enable | true  | testing db replication for release 14 |
    And environment qa14 has the following comment
    """
    QA environment used for manual tests. Please point jenkins to UAT platform instead
    """
    And current logged in user is skwg9735
    And current date is 26/06/2014 12:01/00

    When I request to edit the environment prod14 config override to:
      | config_set     | key                | value | comment                                                        |
      | dbaas-features | replication.enable | false | disabling db replication for release 14 as major bugs detected |

    Then the environment qa14 config is:
      | is_overriden | config_set     | jee_processing | key                | value | comment                                                        |
      | true         | dbaas-features | dbaas-ws       | replication.enable | true  | disabling db replication for release 14 as major bugs detected |
      | true         | dbaas-features | dbaas-ui       | replication.enable | true  | disabling db replication for release 14 as major bugs detected |

    And environment qa14 has the following comment
    """
    [config override edited] 26/06/2014 12:01/00 by skwg9735
    QA environment used for manual tests. Please point jenkins to UAT platform instead
    """
    Given current logged in user is ysbu7453
    And current date is 26/06/2014 14:01/00
    When the environment qa14 is stopped
    Then environment qa14 has the following comment
    """
    [environment stopped] 26/06/2014 14:01/00 by ysbu7453
    [config override edited] 26/06/2014 12:01/00 by skwg9735
    QA environment used for manual tests. Please point jenkins to UAT platform instead
    """
    Given current date is 26/06/2014 14:10/00
    When the environment qa14 is started
    Then environment qa14 has the following comment
    """
    [environment started] 26/06/2014 14:10/00 by ysbu7453
    [config override edited] 26/06/2014 12:01/00 by skwg9735
    QA environment used for manual tests. Please point jenkins to UAT platform instead
    """
