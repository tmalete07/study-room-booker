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

    // Learners from the sample register
    private static final String PALESA_GRADE_11 = "TSS4001-11";
    private static final String ZANELE_GRADE_12 = "TSS5001-12";
    private static final String THABO_GRADE_8 = "TSS1001-8";

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
                .body(bookingJson("GROUP", PALESA_GRADE_11, "2026-10-12T09:00", "2026-10-12T10:00"))
                .when()
                .post("/api/bookings")
                .then()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .body("id", not(emptyOrNullString()))
                .body("roomId", equalTo("GROUP"))
                .body("studentId", equalTo(PALESA_GRADE_11))
                .body("slot.start", startsWith("2026-10-12T09:00"))
                .body("slot.end", startsWith("2026-10-12T10:00"));
    }

    @Test
    void givesEachBookingADifferentId() {
        String firstId = createBooking("GROUP", PALESA_GRADE_11, "2026-10-12T09:00", "2026-10-12T10:00");
        String secondId = createBooking("LIBRARY", ZANELE_GRADE_12, "2026-10-12T09:00", "2026-10-12T10:00");

        assertThat(firstId).isNotEqualTo(secondId);
    }

    @Test
    void lowerCaseStudentNumberIsStoredInCapitals() {
        given()
                .port(app.port())
                .contentType(ContentType.JSON)
                .body(bookingJson("GROUP", "tss4001-11", "2026-10-12T09:00", "2026-10-12T10:00"))
                .when()
                .post("/api/bookings")
                .then()
                .statusCode(201)
                .body("studentId", equalTo(PALESA_GRADE_11));
    }

    // ---------- Grade rooms ----------

    @Test
    void learnerCanBookTheirOwnGradesQuietRoom() {
        createBooking("Q11", PALESA_GRADE_11, "2026-10-12T09:00", "2026-10-12T10:00");
    }

    @Test
    void learnerCannotBookAnotherGradesQuietRoomWith403() {
        given()
                .port(app.port())
                .contentType(ContentType.JSON)
                .body(bookingJson("Q11", THABO_GRADE_8, "2026-10-12T09:00", "2026-10-12T10:00"))
                .when()
                .post("/api/bookings")
                .then()
                .statusCode(403)
                .body("error", equalTo("The Grade 11 Quiet Room is for Grade 11 learners only"));
    }

    @Test
    void differentGradesCanUseTheirOwnQuietRoomsAtTheSameTime() {
        createBooking("Q8", THABO_GRADE_8, "2026-10-12T09:00", "2026-10-12T10:00");
        createBooking("Q11", PALESA_GRADE_11, "2026-10-12T09:00", "2026-10-12T10:00");
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
                .body(bookingJson("GROUP", PALESA_GRADE_11, start, end))
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
                        {"roomId": "GROUP", "studentId": "TSS4001-11"}
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

    @Test
    void rejectsInvalidStudentNumberWith400() {
        given()
                .port(app.port())
                .contentType(ContentType.JSON)
                .body(bookingJson("GROUP", "student-1", "2026-10-12T09:00", "2026-10-12T10:00"))
                .when()
                .post("/api/bookings")
                .then()
                .statusCode(400)
                .body("error", equalTo("Invalid student number. Use the format TSS1234-11"));
    }

    // ---------- 404 Not Found ----------

    @Test
    void rejectsUnknownRoomWith404() {
        given()
                .port(app.port())
                .contentType(ContentType.JSON)
                .body(bookingJson("ROOM99", PALESA_GRADE_11, "2026-10-12T09:00", "2026-10-12T10:00"))
                .when()
                .post("/api/bookings")
                .then()
                .statusCode(404)
                .body("error", equalTo("Room not found: ROOM99"));
    }

    @Test
    void rejectsLearnerWhoIsNotInTheRegisterWith404() {
        given()
                .port(app.port())
                .contentType(ContentType.JSON)
                .body(bookingJson("GROUP", "TSS9999-11", "2026-10-12T09:00", "2026-10-12T10:00"))
                .when()
                .post("/api/bookings")
                .then()
                .statusCode(404)
                .body("error", equalTo("Learner not found: TSS9999-11"));
    }

    // ---------- 409 Conflict ----------

    @Test
    void rejectsOverlappingBookingWith409() {
        createBooking("GROUP", PALESA_GRADE_11, "2026-10-12T09:00", "2026-10-12T10:00");

        given()
                .port(app.port())
                .contentType(ContentType.JSON)
                .body(bookingJson("GROUP", ZANELE_GRADE_12, "2026-10-12T09:30", "2026-10-12T10:30"))
                .when()
                .post("/api/bookings")
                .then()
                .statusCode(409)
                .body("error", equalTo("Room is already booked for that time"));
    }

    @Test
    void rejectsThirdBookingOnSameDayWith409() {
        createBooking("GROUP", PALESA_GRADE_11, "2026-10-12T09:00", "2026-10-12T10:00");
        createBooking("LIBRARY", PALESA_GRADE_11, "2026-10-12T11:00", "2026-10-12T12:00");

        given()
                .port(app.port())
                .contentType(ContentType.JSON)
                .body(bookingJson("LAB", PALESA_GRADE_11, "2026-10-12T14:00", "2026-10-12T15:00"))
                .when()
                .post("/api/bookings")
                .then()
                .statusCode(409)
                .body("error", equalTo("Students can make at most 2 bookings per day"));
    }

    // ---------- GET /api/bookings ----------

    @Test
    void listsOnlyTheStudentsOwnBookings() {
        createBooking("GROUP", PALESA_GRADE_11, "2026-10-12T09:00", "2026-10-12T10:00");
        createBooking("LIBRARY", PALESA_GRADE_11, "2026-10-12T11:00", "2026-10-12T12:00");
        createBooking("LAB", ZANELE_GRADE_12, "2026-10-12T09:00", "2026-10-12T10:00");

        given()
                .port(app.port())
                .queryParam("studentId", PALESA_GRADE_11)
                .when()
                .get("/api/bookings")
                .then()
                .statusCode(200)
                .body("size()", equalTo(2))
                .body("studentId", everyItem(equalTo(PALESA_GRADE_11)));
    }

    @Test
    void listingWithoutStudentIdGives400() {
        given()
                .port(app.port())
                .when()
                .get("/api/bookings")
                .then()
                .statusCode(400)
                .body("error", equalTo("Student number is required"));
    }

    // ---------- DELETE /api/bookings/{id} ----------

    @Test
    void ownerCanCancelWith204() {
        String id = createBooking("GROUP", PALESA_GRADE_11, "2026-10-12T09:00", "2026-10-12T10:00");

        given()
                .port(app.port())
                .header(App.STUDENT_HEADER, PALESA_GRADE_11)
                .when()
                .delete("/api/bookings/{id}", id)
                .then()
                .statusCode(204);

        assertThat(countBookingsFor(PALESA_GRADE_11)).isZero();
    }

    @Test
    void otherStudentCannotCancelWith403() {
        String id = createBooking("GROUP", PALESA_GRADE_11, "2026-10-12T09:00", "2026-10-12T10:00");

        given()
                .port(app.port())
                .header(App.STUDENT_HEADER, ZANELE_GRADE_12)
                .when()
                .delete("/api/bookings/{id}", id)
                .then()
                .statusCode(403)
                .body("error", equalTo("Only the student who made a booking can cancel it"));

        assertThat(countBookingsFor(PALESA_GRADE_11)).isEqualTo(1);
    }

    @Test
    void cancellingUnknownBookingGives404() {
        given()
                .port(app.port())
                .header(App.STUDENT_HEADER, PALESA_GRADE_11)
                .when()
                .delete("/api/bookings/{id}", "does-not-exist")
                .then()
                .statusCode(404)
                .body("error", equalTo("Booking not found: does-not-exist"));
    }

    @Test
    void cancellingWithoutStudentHeaderGives400() {
        String id = createBooking("GROUP", PALESA_GRADE_11, "2026-10-12T09:00", "2026-10-12T10:00");

        given()
                .port(app.port())
                .when()
                .delete("/api/bookings/{id}", id)
                .then()
                .statusCode(400)
                .body("error", equalTo("Student number is required"));
    }

    @Test
    void cancelledSlotCanBeBookedAgain() {
        String id = createBooking("GROUP", PALESA_GRADE_11, "2026-10-12T09:00", "2026-10-12T10:00");

        given()
                .port(app.port())
                .header(App.STUDENT_HEADER, PALESA_GRADE_11)
                .when()
                .delete("/api/bookings/{id}", id)
                .then()
                .statusCode(204);

        createBooking("GROUP", ZANELE_GRADE_12, "2026-10-12T09:00", "2026-10-12T10:00");
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