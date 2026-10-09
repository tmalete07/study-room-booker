# Study Room Booker

A small study-room booking website built to practise the full testing pyramid.
Not deployed: it runs locally and is tested in CI.

## Run
mvn compile exec:java
Then open http://localhost:7070

## Test
mvn test

## Business rules
1. No overlapping bookings for the same room
2. Bookings last 30 minutes to 2 hours
3. Only within opening hours (08:00–20:00)
4. No bookings in the past
5. Max 2 bookings per student per day
6. Only the owner can cancel a booking

## Test levels
| Level       | Tools                        | Status  |
|-------------|------------------------------|---------|
| Unit        | JUnit 5, AssertJ, Mockito    | Started |
| Integration | JUnit 5, H2, Javalin         | Planned |
| API         | REST-assured                 | Planned |
| Acceptance  | Cucumber + Playwright (Java) | Planned |


## Verification

WTC-G2SNX332