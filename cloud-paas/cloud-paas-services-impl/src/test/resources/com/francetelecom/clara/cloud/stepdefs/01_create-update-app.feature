@min-permissions
Feature: 1- paas-users manage application membership
  In order to protect confidential data in my private app releases
  In order to control who can modify releases in my public apps
  In order to control who can act on my environments
  As a paas-user
  I want to manage members in applications

  Background: 
    Given the following users are registered:
      | cuid     | first_name | last_name | role       |
      | wooj1234 | Olivier    | Orand     | role_admin |
      | ggqz1668 | Fabrice    | Bourel    | role_admin |
      | jlt0000  | Jean-Luc   | Touchaud  | role_user  |
      | cpei0000 | Christophe | PEIX      | role_user  |

  Scenario Outline: a public or private application requires a valid set of members to be created or updated.
    This prevents basic mistakes in member list:
    - Member ids are case insensitive
    - Duplicate members are removed
    Membership is mandatory to create the application and its first release.
    It is valid to relinquish membership afterwards to other users (e.g. when I quit the team)

    Given I'm logged in as <logged_user>
    When I request to create a <visibility> application with the following list of members: <member_list>
    Then the application creation request is <creation_status> potentially with the following error message: <message>
    Given I successfully created a <visibility> application with jlt0000 as member list
    When I request to update the application with the following list of members: <member_list>
    Then the application update request is <update_status> potentially with the following error message: <message>

    Examples: 
      | logged_user | visibility | member_list               | creation_status | update_status | message                                                          |
      | jlt0000     | private    |                           | rejected        | rejected      | an application needs to have at least one member specified       |
      | jlt0000     | private    | nosuchuser nosuchuser2    | rejected        | rejected      | unable to set user "nosuchuser2" as member as it is unknown.      |
      | jlt0000     | private    | jlt0000 wooj1234 wooj1234 | accepted        | accepted      | [duplicate member wooj1234 silently removed]                      |
      | jlt0000     | private    | wooj1234                  | rejected        | accepted      | [should add warning: lost membership of this application]        |
      | jlt0000     | private    | cpei0000                  | rejected        | accepted      | [should add warning: lost membership of this application]        |
      | jlt0000     | private    | jlt0000 WOOJ1234 CpEi0000 | accepted        | accepted      |                                                                  |
      | jlt0000     | public     |                           | rejected        | rejected      | an application needs to have at least one member specified       |
      | jlt0000     | public     | jlt0000 wooj1234 cpei0000 | accepted        | accepted      |                                                                  |
      | ggqz1668    | private    | cpei0000                  | accepted        | accepted      | [no warning: admin can create an application on behalf of users] |
