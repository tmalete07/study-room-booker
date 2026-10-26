Feature: Learner profile photo
  Each learner sees their own photo on their profile.
  If the photo cannot be loaded, their initials are shown instead.

  Scenario: A learner sees their own photo
    Given I am logged in as "Karabo Mabena" with student number "TSS1234-11"
    Then I see my photo for "TSS1234-11"

  Scenario: A learner whose photo is missing sees their initials
    Given my photo cannot be loaded
    And I am logged in as "Karabo Mabena" with student number "TSS1234-11"
    Then I see my initials "KM" instead of a photo