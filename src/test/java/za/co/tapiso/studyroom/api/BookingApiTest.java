package za.co.tapiso.studyroom.api;

import io.javalin.Javalin;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import za.co.tapiso.studyroom.App;
import za.co.tapiso.studyroom.domain.BookingPolicy;
import za.co.tapiso.studyroom.repository.InMemoryBookingRepository;
import za.co.tapiso.studyroom.repository.InMemoryRoomRepository;
import za.co.tapiso.studyroom.service.BookingService;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;

class BookingApiTest {

    private static final ZoneId ZONE = ZoneId.of("Africa/Johannesburg");

    private Javalin app;

    @BeforeEach
    void startServer() {
        // "Now" is Sunday 11 October 2026 at 12:00, so Monday 12 October is in the future
        Clock dayBefore = Clock.fixed(
                LocalDateTime.of(2026, 10, 11, 12, 0).atZone(ZONE).toInstant(), ZONE);
        BookingService bookings = new BookingService(
                new InMemoryBookingRepository(), new BookingPolicy(dayBefore));

        app = App.create(InMemoryRoomRepository.withDefaultRooms(), bookings).start(0);
    }

    @AfterEach
    void stopServer() {
        app.stop();
    }

    // ---------- 201 Created ----------

    @Test
    void createsBookingAndReturns201() {
        given()
                .port(app.port())
                .contentType(ContentType.JSON)
                .body(bookingJson("R1", "student-1", "2026-10-12T09:00", "2026-10-12T10:00"))
                .when()
                .post("/api/bookings")
                .then()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .body("id", not(emptyOrNullString()))
                .body("roomId", equalTo("R1"))
                .body("studentId", equalTo("student-1"))
                .body("slot.start", startsWith("2026-10-12T09:00"))
                .body("slot.end", startsWith("2026-10-12T10:00"));
    }

    @Test
    void givesEachBookingADifferentId() {
        String firstId = createBooking("R1", "student-1", "2026-10-12T09:00", "2026-10-12T10:00");
        String secondId = createBooking("R2", "student-2", "2026-10-12T09:00", "2026-10-12T10:00");

        assertThat(firstId).isNotEqualTo(secondId);
    }

    // ---------- 400 Bad Request ----------

    @ParameterizedTest(name = "{0} to {1} gives 400: {2}")
    @CsvSource({
            "2026-10-10T09:00, 2026-10-10T10:00, Bookings cannot start in the past",
            "2026-10-12T09:00, 2026-10-12T12:00, Booking must be between 30 and 120 minutes",
            "2026-10-12T19:30, 2026-10-12T20:30, Bookings must be between 08:00 and 20:00"
    })
    void rejectsInvalidTimesWith400(String start, String end, String expectedError) {
        given()
                .port(app.port())
                .contentType(ContentType.JSON)
                .body(bookingJson("R1", "student-1", start, end))
                .when()
                .post("/api/bookings")
                .then()
                .statusCode(400)
                .contentType(ContentType.JSON)
                .body("error", equalTo(expectedError));
    }

    @Test
    void rejectsMissingTimesWith400() {
        given()
                .port(app.port())
                .contentType(ContentType.JSON)
                .body("""
                        {"roomId": "R1", "studentId": "student-1"}
                        """)
                .when()
                .post("/api/bookings")
                .then()
                .statusCode(400)
                .body("error", equalTo("Start and end are required"));
    }

    @Test
    void rejectsMalformedJsonWith400() {
        given()
                .port(app.port())
                .contentType(ContentType.JSON)
                .body("this is not json")
                .when()
                .post("/api/bookings")
                .then()
                .statusCode(400)
                .body("error", equalTo("Request body must be valid booking JSON"));
    }

    // ---------- 404 Not Found ----------

    @Test
    void rejectsUnknownRoomWith404() {
        given()
                .port(app.port())
                .contentType(ContentType.JSON)
                .body(bookingJson("R99", "student-1", "2026-10-12T09:00", "2026-10-12T10:00"))
                .when()
                .post("/api/bookings")
                .then()
                .statusCode(404)
                .body("error", equalTo("Room not found: R99"));
    }

    // ---------- 409 Conflict ----------

    @Test
    void rejectsOverlappingBookingWith409() {
        createBooking("R1", "student-1", "2026-10-12T09:00", "2026-10-12T10:00");

        given()
                .port(app.port())
                .contentType(ContentType.JSON)
                .body(bookingJson("R1", "student-2", "2026-10-12T09:30", "2026-10-12T10:30"))
                .when()
                .post("/api/bookings")
                .then()
                .statusCode(409)
                .body("error", equalTo("Room is already booked for that time"));
    }

    @Test
    void rejectsThirdBookingOnSameDayWith409() {
        createBooking("R1", "student-1", "2026-10-12T09:00", "2026-10-12T10:00");
        createBooking("R2", "student-1", "2026-10-12T11:00", "2026-10-12T12:00");

        given()
                .port(app.port())
                .contentType(ContentType.JSON)
                .body(bookingJson("R3", "student-1", "2026-10-12T14:00", "2026-10-12T15:00"))
                .when()
                .post("/api/bookings")
                .then()
                .statusCode(409)
                .body("error", equalTo("Students can make at most 2 bookings per day"));
    }

    // ---------- GET /api/bookings ----------

    @Test
    void listsOnlyTheStudentsOwnBookings() {
        createBooking("R1", "student-1", "2026-10-12T09:00", "2026-10-12T10:00");
        createBooking("R2", "student-1", "2026-10-12T11:00", "2026-10-12T12:00");
        createBooking("R3", "student-2", "2026-10-12T09:00", "2026-10-12T10:00");

        given()
                .port(app.port())
                .queryParam("studentId", "student-1")
                .when()
                .get("/api/bookings")
                .then()
                .statusCode(200)
                .body("size()", equalTo(2))
                .body("studentId", everyItem(equalTo("student-1")));
    }

    @Test
    void listingWithoutStudentIdGives400() {
        given()
                .port(app.port())
                .when()
                .get("/api/bookings")
                .then()
                .statusCode(400)
                .body("error", equalTo("Student id is required"));
    }

    // ---------- DELETE /api/bookings/{id} ----------

    @Test
    void ownerCanCancelWith204() {
        String id = createBooking("R1", "student-1", "2026-10-12T09:00", "2026-10-12T10:00");

        given()
                .port(app.port())
                .header(App.STUDENT_HEADER, "student-1")
                .when()
                .delete("/api/bookings/{id}", id)
                .then()
                .statusCode(204);

        assertThat(countBookingsFor("student-1")).isZero();
    }

    @Test
    void otherStudentCannotCancelWith403() {
        String id = createBooking("R1", "student-1", "2026-10-12T09:00", "2026-10-12T10:00");

        given()
                .port(app.port())
                .header(App.STUDENT_HEADER, "student-2")
                .when()
                .delete("/api/bookings/{id}", id)
                .then()
                .statusCode(403)
                .body("error", equalTo("Only the student who made a booking can cancel it"));

        assertThat(countBookingsFor("student-1")).isEqualTo(1);
    }

    @Test
    void cancellingUnknownBookingGives404() {
        given()
                .port(app.port())
                .header(App.STUDENT_HEADER, "student-1")
                .when()
                .delete("/api/bookings/{id}", "does-not-exist")
                .then()
                .statusCode(404)
                .body("error", equalTo("Booking not found: does-not-exist"));
    }

    @Test
    void cancellingWithoutStudentHeaderGives400() {
        String id = createBooking("R1", "student-1", "2026-10-12T09:00", "2026-10-12T10:00");

        given()
                .port(app.port())
                .when()
                .delete("/api/bookings/{id}", id)
                .then()
                .statusCode(400)
                .body("error", equalTo("Student id is required"));
    }

    @Test
    void cancelledSlotCanBeBookedAgain() {
        String id = createBooking("R1", "student-1", "2026-10-12T09:00", "2026-10-12T10:00");

        given()
                .port(app.port())
                .header(App.STUDENT_HEADER, "student-1")
                .when()
                .delete("/api/bookings/{id}", id)
                .then()
                .statusCode(204);

        createBooking("R1", "student-2", "2026-10-12T09:00", "2026-10-12T10:00");
    }

    // ---------- Helpers ----------

    private String createBooking(String roomId, String studentId, String start, String end) {
        return given()
                .port(app.port())
                .contentType(ContentType.JSON)
                .body(bookingJson(roomId, studentId, start, end))
                .when()
                .post("/api/bookings")
                .then()
                .statusCode(201)
                .extract()
                .path("id");
    }

    private int countBookingsFor(String studentId) {
        return given()
                .port(app.port())
                .queryParam("studentId", studentId)
                .when()
                .get("/api/bookings")
                .then()
                .statusCode(200)
                .extract()
                .path("size()");
    }

    private static String bookingJson(String roomId, String studentId, String start, String end) {
        return """
                {
                  "roomId": "%s",
                  "studentId": "%s",
                  "start": "%s",
                  "end": "%s"
                }
                """.formatted(roomId, studentId, start, end);
    }
}