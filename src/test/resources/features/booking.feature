Feature: Booking a study room
  As a Tswelopele learner
  I want to book a study room for a time slot
  So that I have a quiet place to study

  Background:
    Given I am logged in as "Karabo Mabena" with student number "TSS1234-11"

  Scenario: A learner books a shared room
    When I book room "LIBRARY" tomorrow from "09:00" to "10:00"
    Then I see the booking confirmed for "09:00 to 10:00"

  Scenario: A learner cannot book a room that someone else already has
    Given "TSS4001-11" has already booked room "LIBRARY" tomorrow from "09:00" to "10:00"
    When I book room "LIBRARY" tomorrow from "09:30" to "10:30"
    Then I see the booking rejected because "Room is already booked for that time"