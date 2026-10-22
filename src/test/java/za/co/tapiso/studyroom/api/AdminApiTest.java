package za.co.tapiso.studyroom.api;

import io.javalin.Javalin;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.co.tapiso.studyroom.App;
import za.co.tapiso.studyroom.domain.BookingPolicy;
import za.co.tapiso.studyroom.repository.InMemoryBookingRepository;
import za.co.tapiso.studyroom.repository.InMemoryRoomRepository;
import za.co.tapiso.studyroom.service.BookingService;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;

class AdminApiTest {

    private static final ZoneId ZONE = ZoneId.of("Africa/Johannesburg");

    // Learners from the sample register
    private static final String THABO_GRADE_8 = "TSS1001-8";
    private static final String PALESA_GRADE_11 = "TSS4001-11";
    private static final String ZANELE_GRADE_12 = "TSS5001-12";

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

    @Test
    void returnsEmptyListWhenThereAreNoBookings() {
        given()
                .port(app.port())
                .when()
                .get("/api/admin/bookings")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", equalTo(0));
    }

    @Test
    void returnsEveryLearnersBookingsEarliestFirst() {
        // Created out of time order on purpose
        String afternoon = createBooking("GROUP", PALESA_GRADE_11, "2026-10-12T14:00", "2026-10-12T15:00");
        String morning = createBooking("LAB", ZANELE_GRADE_12, "2026-10-12T09:00", "2026-10-12T10:00");
        String midday = createBooking("Q8", THABO_GRADE_8, "2026-10-12T11:00", "2026-10-12T12:00");

        given()
                .port(app.port())
                .when()
                .get("/api/admin/bookings")
                .then()
                .statusCode(200)
                .body("size()", equalTo(3))
                .body("id", contains(morning, midday, afternoon))
                .body("studentNumber", contains(ZANELE_GRADE_12, THABO_GRADE_8, PALESA_GRADE_11));
    }

    @Test
    void includesRoomNameLearnerNameAndGrade() {
        createBooking("LAB", ZANELE_GRADE_12, "2026-10-12T09:00", "2026-10-12T10:00");

        given()
                .port(app.port())
                .when()
                .get("/api/admin/bookings")
                .then()
                .statusCode(200)
                .body("[0].roomId", equalTo("LAB"))
                .body("[0].roomName", equalTo("Computer Lab"))
                .body("[0].studentNumber", equalTo(ZANELE_GRADE_12))
                .body("[0].learnerName", equalTo("Zanele Mthembu"))
                .body("[0].grade", equalTo(12))
                .body("[0].start", startsWith("2026-10-12T09:00"))
                .body("[0].end", startsWith("2026-10-12T10:00"));
    }

    @Test
    void cancelledBookingsDoNotAppear() {
        String kept = createBooking("GROUP", PALESA_GRADE_11, "2026-10-12T09:00", "2026-10-12T10:00");
        String cancelled = createBooking("LAB", ZANELE_GRADE_12, "2026-10-12T09:00", "2026-10-12T10:00");

        given()
                .port(app.port())
                .header(App.STUDENT_HEADER, ZANELE_GRADE_12)
                .when()
                .delete("/api/bookings/{id}", cancelled)
                .then()
                .statusCode(204);

        given()
                .port(app.port())
                .when()
                .get("/api/admin/bookings")
                .then()
                .statusCode(200)
                .body("id", contains(kept));
    }

    private String createBooking(String roomId, String studentId, String start, String end) {
        return given()
                .port(app.port())
                .contentType(ContentType.JSON)
                .body("""
                        {"roomId": "%s", "studentId": "%s", "start": "%s", "end": "%s"}
                        """.formatted(roomId, studentId, start, end))
                .when()
                .post("/api/bookings")
                .then()
                .statusCode(201)
                .extract()
                .path("id");
    }
}