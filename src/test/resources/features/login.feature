Feature: Logging in
  As a Tswelopele learner
  I want to log in with my name and student number
  So that I can book a study room for myself

  Scenario: A registered learner logs in
    Given I am on the login page
    When I log in as "Karabo Mabena" with student number "TSS1234-11"
    Then I see my profile page
    And my name "Karabo Mabena" is shown

  Scenario: A learner cannot log in with the wrong name
    Given I am on the login page
    When I log in as "Someone Else" with student number "TSS1234-11"
    Then I am still on the login page
    And I see a login error