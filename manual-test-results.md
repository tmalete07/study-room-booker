# Manual API Test Results

**Date:** 16 October 2026
**Tester:** Tapiso Malete
**Build:** commit <paste the output of: git rev-parse --short HEAD>
**Environment:** Pop!_OS, Java 21, fresh `data/` folder

| ID   | Scenario                         | Expected | Actual | Result |
|------|----------------------------------|----------|--------|--------|
| TC1  | Health check                     | 200      |        |        |
| TC2  | List rooms                       | 200      |        |        |
| TC3  | Create booking                   | 201      |        |        |
| TC4  | Overlapping booking              | 409      |        |        |
| TC5  | Back-to-back booking             | 201      |        |        |
| TC6  | Second booking same day          | 201      |        |        |
| TC7  | Third booking same day           | 409      |        |        |
| TC8  | Booking in the past              | 400      |        |        |
| TC9  | Booking longer than 2 hours      | 400      |        |        |
| TC10 | Outside opening hours            | 400      |        |        |
| TC11 | Unknown room                     | 404      |        |        |
| TC12 | Malformed JSON                   | 400      |        |        |
| TC13 | List a student's bookings        | 200 (2)  |        |        |
| TC14 | Non-owner cancels                | 403      |        |        |
| TC15 | Owner cancels                    | 204      |        |        |
| TC16 | Cancelled booking is gone        | 200 (1)  |        |        |
| TC17 | Bookings survive restart         | 200 (1)  |        |        |
| TC18 | Freed slot can be rebooked       | 201      |        |        |

## Notes / defects found
-