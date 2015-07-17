@min-permissions
Feature: 3- membership is requested to read/list/act on private applications releases
  In order to protect confidential data in the app releases
  As a paas-user
  I want to restrict access to my releases to invited members
  
  In order to assist paas-users with troubles on their private app environments
  As a paas-admin
  I need to be able to list and act on releases of private apps I'm not a member of

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
      | jenkins             | ggqz1668          | public     |
      | jhipster            | jlt0000           | public     |
    And the following releases were created:
      | app_name            | app_release |
      | click-n-dev         | 1.2         |
      | elpaaso             | 1.9         |
      | elpaaso             | 1.10.4      |
      | simulation retraite | 1.5         |
      | simulation retraite | 1.6         |
      | jenkins             | 1.1         |
      | jhipster            | 1.0         |

  Scenario: non-admin users can only list releases of applications they're member of
    Given I'm logged in as jlt0000
    When I list releases using the "default" filter
    Then the listed releases are:
      | app_name            | app_release |
      | click-n-dev         | 1.2         |
      | simulation retraite | 1.5         |
      | simulation retraite | 1.6         |
      | jhipster            | 1.0         |

  Scenario: non-admin users can expand choose to list releases of public applications
    so that they can reuse sample release and architectures (e.g. jenkins or mysqlmyadmin)

    Given I'm logged in as jlt0000
    When I list releases using the "all_releases" filter
    Then the listed releases are:
      | app_name            | app_release |
      | click-n-dev         | 1.2         |
      | simulation retraite | 1.5         |
      | simulation retraite | 1.6         |
      | jenkins             | 1.1         |
      | jhipster            | 1.0         |

  Scenario Outline: non-admin users can not read, or act on releases of a given private applications they're not member of
    so that confidentially on these releases is preserved.
    Non-admin users can see releases of public applications so that they can reuse sample apps and possible
    contribute to them.

    Given I'm logged in as jlt0000
    When I request the release <app_release> of the application <app_name>
    Then I <can_access> access the release
    And I <can_edit> edit the release
    # And I <can_display_or_edit_architecture> edit/display the architecture

    Examples: 
      | app_name    | app_release | can_access | can_edit | can_display_or_edit_architecture |
      | click-n-dev | 1.2         | can        | can      | can                              |
      | elpaaso     | 1.9         | cannot     | cannot   | cannot                           |
      | jenkins     | 1.1         | can        | cannot   | can                              |
      | jhipster    | 1.0         | can        | can      | can                              |

  Scenario: admin users by default only list releases of applications they're member of
    so that during demos they can explain the non admin users default behavior

    Given I'm logged in as ggqz1668
    When I list releases using the "default" filter
    Then the listed releases are:
      | app_name | app_release |
      | elpaaso  | 1.9         |
      | elpaaso  | 1.10.4      |
      | jenkins  | 1.1         |

  Scenario: admin users can list releases of private applications they're not member of
    in order to support paas-user with mistakes they made into their architecture
    (as well as public applications)

    Given I'm logged in as ggqz1668
    When I list releases using the "all_releases" filter
    Then the listed releases are:
      | app_name            | app_release |
      | click-n-dev         | 1.2         |
      | elpaaso             | 1.9         |
      | elpaaso             | 1.10.4      |
      | simulation retraite | 1.5         |
      | simulation retraite | 1.6         |
      | jenkins             | 1.1         |
      | jhipster            | 1.0         |

  Scenario Outline: admin users can read, or act on releases of private applications they're not member of
    in order to support paas-user with mistakes they made into their architecture

    Given I'm logged in as ggqz1668
    When I request the release <app_release> of the application <app_name>
    Then I <can_access> access the release
    And I <can_edit> edit the release
    # And I <can_display_or_edit_architecture> edit/display the architecture

    Examples: 
      | app_name            | app_release | can_access | can_edit | can_display_or_edit_architecture |
      | click-n-dev         | 1.2         | can        | can      | can                              |
      | elpaaso             | 1.9         | can        | can      | can                              |
      | elpaaso             | 1.10.4      | can        | can      | can                              |
      | simulation retraite | 1.5         | can        | can      | can                              |
      | simulation retraite | 1.6         | can        | can      | can                              |
      | jenkins             | 1.1         | can        | can      | can                              |

  Scenario: only accessible releases can be cloned
