Feature: Quiet rooms are for their own grade
  Each grade has its own quiet room. Shared rooms are open to every grade.

  Scenario Outline: A Grade <grade> learner only sees their own grade's quiet room
    Given I am logged in as "<name>" with student number "<student number>"
    When I open the booking page
    Then I can choose room "<own room>"
    And I can choose room "LIBRARY"
    But I cannot choose room "<other room>"

    Examples:
      | grade | name           | student number | own room | other room |
      | 8     | Thabo Mokoena  | TSS1001-8      | Q8       | Q9         |
      | 9     | Sipho Nkosi    | TSS2001-9      | Q9       | Q8         |
      | 10    | Lerato Molefe  | TSS3001-10     | Q10      | Q12        |
      | 11    | Karabo Mabena  | TSS1234-11     | Q11      | Q12        |
      | 12    | Zanele Mthembu | TSS5001-12     | Q12      | Q11        |

  Scenario: A link to another grade's quiet room does not choose it
    Given I am logged in as "Karabo Mabena" with student number "TSS1234-11"
    When I open the booking page for room "Q12"
    Then no room is chosen