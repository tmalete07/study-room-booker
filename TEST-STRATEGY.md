# Test Strategy — Study Room Booker

This document explains how the Study Room Booker is tested: what is tested at each level,
which tools and techniques are used, how quality is measured, and what is known not to be covered.

## 1. Scope

Study Room Booker lets learners of a (fictional) secondary school log in with their name and
student number, view their profile, see the rooms open to their grade, book a room for a time
slot, and cancel their own bookings. An admin page lists every booking.

**In scope:** business rules (booking times, clashes, daily limits, grade-only quiet rooms,
student number format), the REST API, persistence, and the main learner journeys in a real browser.

**Out of scope:** real authentication, deployment, load/performance, and cross-browser testing
(see [Known gaps](#9-known-gaps-and-risks)).

## 2. Test levels

The suite follows the test pyramid: many fast, focused tests at the bottom and a small number of
slow, end-to-end tests at the top.

| Level | What is real | Tests | Example classes |
|---|---|---:|---|
| Unit: domain | One class, no I/O | 154 | `StudentNumberTest`, `BookingTest`, `BookingPolicyTest`, `RoomTest` |
| Unit: services | Service logic, repositories mocked or in-memory | 33 | `BookingServiceTest`, `LearnerServiceTest` |
| Repository (contract) | Real repositories, incl. H2 database | 33 | `InMemoryBookingRepositoryTest`, `JdbcBookingRepositoryTest` |
| Database | Real H2 schema | 3 | `DatabaseTest` |
| Integration | Server + database file, across a restart | 3 | `BookingPersistenceTest` |
| API | Real HTTP server, no browser | 49 | `BookingApiTest`, `LoginApiTest`, `ProfileApiTest`, `AdminApiTest` |
| **Acceptance** | **Everything: browser, HTML, JS, CSS, server** | **12 scenarios** | `login.feature`, `booking.feature`, `room-access.feature`, `profile.feature` |

**Total: 275 fast tests + 12 browser scenarios.**

## 3. Tools

| Purpose | Tool |
|---|---|
| Test framework | JUnit 5 (incl. `@ParameterizedTest`) |
| Assertions | AssertJ |
| Mocking | Mockito |
| API testing | REST-assured |
| Acceptance tests (BDD) | Cucumber (Gherkin feature files) with PicoContainer |
| Browser automation | Playwright for Java (Chromium) |
| Coverage | JaCoCo |
| Build / test runners | Maven, Surefire (fast tests), Failsafe (acceptance tests) |
| Continuous integration | GitHub Actions |

## 4. Test design techniques

- **Equivalence partitioning and boundary value analysis**: for example, student numbers
  (valid grades 8–12, boundaries 7 and 13, 3 vs 4 vs 5 digits, spaces, lower case),
  booking length (30–120 minutes) and opening hours (08:00–20:00).
- **Parameterized tests** to run one rule against many inputs.
- **Negative testing**: invalid input, unknown rooms and learners, past bookings, clashes,
  cancelling someone else's booking, extra JSON fields (mass-assignment protection).
- **Contract tests**: one abstract `BookingRepositoryContract` runs the same tests against the
  in-memory and the H2 (JDBC) repository, proving they behave the same.
- **Fixed clock**: services take a `Clock`, so "now" is controlled and time-based tests are repeatable.
- **Security-minded checks**: the same login error for an unknown student number and a wrong
  name (no user enumeration); grade rules enforced by the API (403) and not only hidden in the UI
  (defence in depth); output written with `textContent`, not `innerHTML` (XSS).
- **Scenario Outline**: one acceptance scenario run for every grade (8–12).
- **Network stubbing**: Playwright intercepts the photo request and returns 404 to test the
  "no photo, show initials" fallback without changing the app.

## 5. Acceptance tests (BDD)

Feature files in `src/test/resources/features` describe behaviour in plain English. Each scenario:

1. gets a **fresh server** on a free port with in-memory data (never the real database file),
2. gets a **fresh browser context** (no leftover session), and
3. is driven through **Page Objects** (`LoginPage`, `BookPage`, `ProfilePage`), so HTML details
   live in one place and the steps read like the feature files.

Test data is set up through the API where the setup is not what is being tested
(for example, another learner's existing booking in the clash scenario).
Dates are always "tomorrow", worked out at run time, so scenarios never go out of date.
Playwright's auto-waiting assertions are used instead of sleeps; checks that something is
**absent** first wait for the page to finish loading, so they cannot pass too early.

When a scenario fails, a **full-page screenshot** is attached to the Cucumber HTML report and
saved to `target/screenshots/`.

## 6. Proving the tests work (mutation checks)

Tests were checked by deliberately breaking the code and confirming they fail:

| Change made on purpose | Result |
|---|---|
| Grade filter in `app.js` made to allow every room | 6 of 10 acceptance scenarios failed |
| Expected booking time changed in a feature file | Scenario failed, screenshot captured |
| Coverage minimum raised to 99% | Build failed on the coverage check |

Each change was reverted afterwards.

## 7. Coverage and quality gates

JaCoCo measures coverage for the fast tests and, separately, for the acceptance tests.

| Report | Lines | Branches |
|---|---:|---:|
| Fast tests (`target/site/jacoco`) | about 95% | 93% |
| Acceptance tests (`target/site/jacoco-it`) | 58% (instructions) | 43% |

Acceptance coverage is lower by design: those tests follow the main user journeys, while edge
cases are covered by the fast tests.

**Quality gate:** `mvn verify` fails if fast-test coverage drops below **90% lines** or
**85% branches**.

## 8. How to run the tests

| Command | Runs |
|---|---|
| `mvn test` | The 275 fast tests (seconds) and the coverage report |
| `mvn verify` | Fast tests, then the 12 browser scenarios, both coverage reports and the coverage check |
| `mvn verify -Dit.test=RunCucumberIT` | Fast tests plus the acceptance scenarios only via Failsafe |
| add `-Dheadless=false` | Watch the browser run the scenarios |

Reports after `mvn verify`:
`target/cucumber-report.html`, `target/site/jacoco/index.html`, `target/site/jacoco-it/index.html`.

**CI:** every push and pull request to `main` runs `mvn verify` on GitHub Actions with Java 17.
The reports and any failure screenshots are uploaded as a build artifact, even when the build fails.

## 9. Known gaps and risks

| Gap | Risk | Mitigation / future work |
|---|---|---|
| No real authentication; the logged-in learner is kept in `sessionStorage` | Anyone can act as another learner by changing the stored value | Add server-side sessions or tokens |
| Admin page and `/api/admin/bookings` have no login | Anyone can see all bookings | Add an admin role |
| API allows cancelling a booking that has already started | Learner frees a room mid-session | Add a rule and tests |
| Profile edits are held in memory | Lost on restart | Persist profiles in the database |
| JavaScript has no unit tests | Logic bugs found only by acceptance tests | Add a JS test runner (e.g. Jest) |
| Acceptance tests run in Chromium only | Firefox/Safari differences missed | Run Playwright with more browsers |
| No automated accessibility, performance or security scanning | Issues found late | Add axe-core checks, a load test, a dependency scan |

## 10. Defects found during development

| Defect | Found by | Fix |
|---|---|---|
| Booking in the past returned **500** instead of a clear error | Manual API check (curl) | Mapped domain exceptions to 400/403/404/409 |
| `List.of(...).contains(null)` threw `NullPointerException` when validating goals | Unit test | Checked for nulls with a loop |
| Admin page status pills took the header's `.status` style | Visual check | Renamed the class to `.status-pill` |
| Edit-profile "Remove" buttons pushed out of the dialog | Visual check | `minmax(0, …)` grid columns |
| Playwright regex using `Pattern.quote` never matched | Acceptance test | Exact match; Playwright regexes run as JavaScript |