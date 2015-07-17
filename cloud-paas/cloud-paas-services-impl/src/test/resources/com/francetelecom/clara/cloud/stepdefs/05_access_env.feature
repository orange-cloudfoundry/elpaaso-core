@min-permissions
Feature: 5- membership is requested to read/list/act on environments

  In order to protect confidential data in the app releases
  In order to avoid that other users act on my environments by mistake
  As a user
  I want to restrict access to my environments to invited members to my application.

  In order to assist paas-users with troubles on their private app environments
  As a paas-admin
  I need to be able to list and act on environments of private apps I'm not a member of

  Background:
    Given the following users are registered:
      | cuid     | first_name | last_name | role       |
      | wooj1234 | Olivier    | Orand     | role_admin |
      | ggqz1668 | Fabrice    | Bourel    | role_admin |
      | jlt0000  | Jean-Luc   | Touchaud  | role_user  |
      | cpei0000 | Christophe | PEIX      | role_user  |
    And the following applications were created:
      | app_name            | member_list       | visibility |
      | click-n-dev         | jlt0000           | private    |
      | elpaaso             | ggqz1668 wooj1234 | private    |
      | simulation retraite | jlt0000 cpei0000  | private    |
      | jhipster            | wooj1234          | public     |
    And the following releases were created:
      | app_name            | app_release |
      | click-n-dev         | 1.2         |
      | elpaaso             | 1.10.4      |
      | simulation retraite | 1.0         |
      | jhipster            | 1.0         |
    And the following environments were created:
      | env_name | app_name            | app_release |
      | jluc_dev | click-n-dev         | 1.2         |
      | live     | simulation retraite | 1.0         |
      | uat      | elpaaso             | 1.10.4      |
      | demo     | jhipster            | 1.0         |

  Scenario: by default non-admin users list environments of applications they're member of,
    Given I'm logged in as jlt0000
    When I list environments using the "default" filter
    Then the listed environments are:
      | env_name | app_name            | app_release |
      | jluc_dev | click-n-dev         | 1.2         |
      | live     | simulation retraite | 1.0         |

  Scenario: non-admin users can choose to list environments of public applications they're not member of
  in order to be inspired by public applications and enjoy these environments
    Given I'm logged in as jlt0000
    When I list environments using the "all_environments" filter
    Then the listed environments are:
      | env_name | app_name            | app_release |
      | jluc_dev | click-n-dev         | 1.2         |
      | live     | simulation retraite | 1.0         |
      | demo     | jhipster            | 1.0         |

  Scenario Outline: non-admin users can not read, or act on environments of private applications they're not member of
    Given I'm logged in as jlt0000
    When I request to see details on environment <env_name> associated to application <app_name> and release <app_release>
    Then I <can_access> access the environment
    And I <can_act> start the environment
    And I <can_act> stop the environment
    And I <can_act> delete the environment

  Examples:
    | env_name | app_name            | app_release | can_access | can_act |
    | jluc_dev | click-n-dev         | 1.2         | can        | can     |
    | live     | simulation retraite | 1.0         | can        | can     |
    | uat      | elpaaso             | 1.10.4      | cannot     | cannot  |
    | demo     | jhipster            | 1.0         | can        | cannot  |

  Scenario: by default admin users only list environments of applications they're member of
    Given I'm logged in as ggqz1668
    When I list environments using the "default" filter
    Then the listed environments are:
      | env_name | app_name | app_release |
      | uat      | elpaaso  | 1.10.4      |

  Scenario: admin users can choose to list environments of applications they're not member of
  in order to support paas-user with trouble they might have on their environment

    Given I'm logged in as ggqz1668
    When I list environments using the "all_environments" filter
    Then the listed environments are:
      | env_name | app_name            | app_release |
      | jluc_dev | click-n-dev         | 1.2         |
      | live     | simulation retraite | 1.0         |
      | uat      | elpaaso             | 1.10.4      |
      | demo     | jhipster            | 1.0         |

  Scenario Outline: admin users can read, or act on environments of private applications they're not member of
    Given I'm logged in as ggqz1668
    When I request to see details on environment <env_name> associated to application <app_name> and release <app_release>
    Then I <can_access> access the environment
    And I <can_act> start the environment
    And I <can_act> stop the environment
    And I <can_act> delete the environment

  Examples:
    | env_name | app_name            | app_release | can_access | can_act |
    | jluc_dev | click-n-dev         | 1.2         | can        | can     |
    | live     | simulation retraite | 1.0         | can        | can     |
    | uat      | elpaaso             | 1.10.4      | can        | can     |
    | demo     | jhipster            | 1.0         | can        | can     |

  Scenario Outline: only accessible releases can be instantiated as new environnements. Paas-users willing to instantiate
  release of public applications should clone them into their own app instead. This allows them to control who
  can act on their environment.

    Given I'm logged in as jlt0000
    When I request to instantiate app <app_name> and release <app_release> into a new environment then
    Then my environment instantiation request is <can_instantiate_env>

  Examples:
    | logged_in_user | app_name | app_release | can_instantiate_env |
    | jlt0000        | jhipster | 1.0         | rejected            |
    | wooj1234       | jhipster | 1.0         | accepted            |


