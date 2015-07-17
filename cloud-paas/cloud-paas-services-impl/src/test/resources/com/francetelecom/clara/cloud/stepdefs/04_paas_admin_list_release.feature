@min-permissions
Feature: 4- paas-admins access/act on any private applications
  
  In order to assist paas-users who lost access to their private apps
  In order to assist paas-users with troubles on their private app environments
  As a paas-admin
  I need to be able to list and act on private apps I'm not a member of

  Background: 
    Given the following users are registered:
      | cuid      | first_name | last_name | role       |
      | wooj1234  | Olivier    | Orand     | role_admin |
      | ggqz1668  | Fabrice    | Bourel    | role_admin |
      | jlt0000   | Jean-Luc   | Touchaud  | role_user  |
      | cpei0000  | Christophe | PEIX      | role_user  |
      | other0000 | Another    | Person1   | role_user  |
    And the following applications were created:
      | app_name            | member_list       | nb_releases | visibility |
      | elpaaso             | ggqz1668 wooj1234 | 4           | private    |
      | click-n-dev         | jlt0000           | 3           | private    |
      | simulation retraite | jlt0000 cpei0000  | 2           | private    |
      | jhipster            | other0000         | 2           | public     |

  Scenario: by default, admin users list only private applications they're member of
    so that during demos they can explain the non admin users default behavior

    Given I'm logged in as ggqz1668
    When I list applications using the "default" filter
    Then the listed applications are:
      | app_name | member_list       | can_edit |
      | elpaaso  | ggqz1668 wooj1234 | true     |

  Scenario: admin users, like regular users, can expand the list applications to show applications they're not member of
    Given I'm logged in as ggqz1668
    When I list applications using the "all_applications" filter
    Then the listed applications are:
      | app_name            | member_list       | can_edit |
      | click-n-dev         | jlt0000           | true     |
      | elpaaso             | ggqz1668 wooj1234 | true     |
      | simulation retraite | cpei0000 jlt0000  | true     |
      | jhipster            | other0000         | true     |

  Scenario Outline: admin users can display a private application they're not member of and can act on it.
    This allow them to support users who have made mistakes such lost membership to their app.

    Given I'm logged in as ggqz1668
    When I request the application <app_name>
    Then I see member_list=<member_list>
    And I <can_edit> edit the application
    And I <can_list_releases> list the releases
    And I <can_create_new_releases> create a new release for this application

    Examples: 
      | app_name            | member_list       | can_edit | can_list_releases | can_create_new_releases |
      | elpaaso             | ggqz1668 wooj1234 | can      | can               | can                     |
      | simulation retraite | jlt0000 cpei0000  | can      | can               | can                     |
      | jhipster            | other0000         | can      | can               | can                     |
