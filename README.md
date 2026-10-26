# Study Room Booker

[![CI](https://github.com/tmalete07/study-room-booker/actions/workflows/ci.yml/badge.svg)](https://github.com/tmalete07/study-room-booker/actions/workflows/ci.yml)

A small study-room booking website built to practise the full testing pyramid.
Not deployed: it runs locally and is tested in CI.

Learners of the (fictional) Tswelopele Secondary School log in with their name and student number,
see the rooms open to their grade, book a study room and manage their bookings.
Each grade has its own quiet room; shared rooms are open to everyone.

**Stack:** Java 17, Javalin, H2, plain HTML/CSS/JavaScript. **Build:** Maven.

## Run
mvn compile exec:java
Then open http://localhost:7070

| Page | Address |
|---|---|
| Learner log in | http://localhost:7070/login.html |
| My profile (after log in) | http://localhost:7070/ |
| Rooms | http://localhost:7070/rooms.html |
| Book a room | http://localhost:7070/book.html |
| My bookings | http://localhost:7070/bookings.html |
| Admin office (all bookings) | http://localhost:7070/admin.html |

### Sample learners
The name and student number must match exactly.

| Name | Student number | Grade |
|---|---|---|
| Thabo Mokoena | TSS1001-8 | 8 |
| Naledi Dlamini | TSS1002-8 | 8 |
| Sipho Nkosi | TSS2001-9 | 9 |
| Ayanda Khumalo | TSS2002-9 | 9 |
| Lerato Molefe | TSS3001-10 | 10 |
| Kagiso Mahlangu | TSS3002-10 | 10 |
| Karabo Mabena | TSS1234-11 | 11 |
| Palesa Ndlovu | TSS4001-11 | 11 |
| Tumelo Sithole | TSS4002-11 | 11 |
| Zanele Mthembu | TSS5001-12 | 12 |
| Bongani Zulu | TSS5002-12 | 12 |

## Test
mvn test

Runs the 275 fast tests (unit, repository, integration and API) in seconds.

mvn verify

Runs the fast tests, then the 12 browser acceptance scenarios, and checks coverage
(the build fails below 90% lines or 85% branches). Add `-Dheadless=false` to watch the browser.

Reports after `mvn verify`:
- `target/cucumber-report.html`: acceptance scenarios, with a screenshot of any failure
- `target/site/jacoco/index.html`: coverage from the fast tests
- `target/site/jacoco-it/index.html`: coverage from the acceptance tests

The full approach is in **[TEST-STRATEGY.md](TEST-STRATEGY.md)**.

## Business rules
1. No overlapping bookings for the same room
2. Bookings last 30 minutes to 2 hours
3. Only within opening hours (08:00–20:00)
4. No bookings in the past
5. Max 2 bookings per student per day
6. Only the owner can cancel a booking
7. A grade's quiet room can only be booked by learners in that grade
8. Student numbers look like `TSS1234-11`: TSS, 4 digits, a dash, then the grade (8 to 12)

## Test levels
| Level       | Tools                        | Status  |
|-------------|------------------------------|---------|
| Unit        | JUnit 5, AssertJ, Mockito    | Done: 187 tests (domain and services) |
| Integration | JUnit 5, H2, Javalin         | Done: 39 tests (repositories, database, restart persistence) |
| API         | REST-assured                 | Done: 49 tests |
| Acceptance  | Cucumber + Playwright (Java) | Done: 12 scenarios |

Coverage (fast tests): about 95% of lines and 93% of branches.
Every push runs `mvn verify` on GitHub Actions.

## Known issues
This is a testing project, not a production system:
- There is no real authentication. The logged-in learner is kept in the browser tab (`sessionStorage`), and the admin page has no login.
- The API allows cancelling a booking that has already started.
- Profile edits are kept in memory and reset when the server restarts.

See [TEST-STRATEGY.md](TEST-STRATEGY.md#9-known-gaps-and-risks) for the full list and suggested fixes.

## Images
The school is used as a theme only. All learners are fictional, and the learner and school
photos are AI-generated illustrations. The badge is an original design, not the school's crest.

## Verification

WTC-G2SNX332