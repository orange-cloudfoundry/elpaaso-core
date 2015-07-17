@min-permissions
Feature: 2- membership is requested to act on applications
  In order to protect confidential data in the app releases
  I want to restrict access to my application and releases to invited members

  Background: 
    Given the following users are registered:
      | cuid      | first_name | last_name | role       |
      | wooj1234  | Olivier    | Orand     | role_admin |
      | ggqz1668  | Fabrice    | Bourel    | role_admin |
      | jlt0000   | Jean-Luc   | Touchaud  | role_user  |
      | cpei0000  | Christophe | PEIX      | role_user  |
      | other0000 | Another    | Person1   | role_user  |
      | other0001 | Miguel     | Person2   | role_user  |
      | other0002 | John       | Person3   | role_user  |
    And the following applications were created:
      | app_name            | member_list                            | nb_releases | visibility |
      | demo                | ggqz1668                               | 3           | private    |
      | click-n-dev         | jlt0000                                | 3           | private    |
      | crowded app         | cpei0000 other0000 other0001 other0002 | 2           | private    |
      | elpaaso             | ggqz1668 wooj1234                      | 4           | private    |
      | simulation retraite | jlt0000 cpei0000                       | 2           | private    |
      | jenkins             | jlt0000                                | 2           | public     |
      | jhipster            | other0000                              | 2           | public     |

  Scenario: by default, non-admin users only list applications they're member of
    Given I'm logged in as jlt0000
    When I list applications using the "default" filter
    Then the listed applications are:
      | app_name            | member_list      | can_edit |
      | click-n-dev         | jlt0000          | true     |
      | simulation retraite | jlt0000 cpei0000 | true     |
      | jenkins             | jlt0000          | true     |

  Scenario: non-admin users can list applications they're not member of, but cannot act on them
    Given I'm logged in as jlt0000
    When I list applications using the "all_applications" filter
    Then the listed applications are:
      | app_name            | member_list                            | can_edit |
      | demo                | ggqz1668                               | false    |
      | click-n-dev         | jlt0000                                | true     |
      | crowded app         | cpei0000 other0000 other0001 other0002 | false    |
      | elpaaso             | ggqz1668 wooj1234                      | false    |
      | simulation retraite | jlt0000 cpei0000                       | true     |
      | jenkins             | jlt0000                                | true     |
      | jhipster            | other0000                              | false    |

  Scenario Outline: non-admin users can display a given application they're not member of, but cannot act on it.
    This allow them to apply for membership to the application, or understand that the application name/code is already used any another application.

    Given I'm logged in as jlt0000
    When I request the application <app_name>
    Then I see member_list=<member_list>
    And I <can_edit_app> edit the application
    And I <can_list_releases> list the releases
    And I <can_create_new_releases> create a new release for this application

    Examples: 
      | app_name            | member_list       | can_edit_app | can_list_releases | can_create_new_releases |
      | elpaaso             | ggqz1668 wooj1234 | cannot       | cannot            | cannot                  |
      | simulation retraite | jlt0000 cpei0000  | can          | can               | can                     |
      | jenkins             | jlt0000           | can          | can               | can                     |
      | jhipster            | other0000         | cannot       | can               | cannot                  |
