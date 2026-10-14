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
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.equalTo;
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