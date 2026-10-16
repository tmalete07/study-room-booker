package za.co.tapiso.studyroom.integration;

import io.javalin.Javalin;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import za.co.tapiso.studyroom.App;
import za.co.tapiso.studyroom.db.Database;
import za.co.tapiso.studyroom.domain.BookingPolicy;
import za.co.tapiso.studyroom.repository.InMemoryRoomRepository;
import za.co.tapiso.studyroom.repository.JdbcBookingRepository;
import za.co.tapiso.studyroom.service.BookingService;

import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;

/**
 * Full-stack integration test: real HTTP server, real service, real H2 database file on disk.
 * Proves bookings survive the server being stopped and started again.
 */
class BookingPersistenceTest {

    private static final ZoneId ZONE = ZoneId.of("Africa/Johannesburg");
    private static final Clock DAY_BEFORE = Clock.fixed(
            LocalDateTime.of(2026, 10, 11, 12, 0).atZone(ZONE).toInstant(), ZONE);

    // JUnit creates an empty temporary folder for each test and deletes it afterwards
    @TempDir
    Path tempDir;

    private Path databaseFile;
    private Javalin app;

    @BeforeEach
    void setUp() {
        databaseFile = tempDir.resolve("study-room-booker");
    }

    @AfterEach
    void stopServer() {
        if (app != null) {
            app.stop();
        }
    }

    @Test
    void bookingSurvivesAServerRestart() {
        app = startApp();
        String id = createBooking("R1", "student-1", "2026-10-12T09:00", "2026-10-12T10:00");

        restartApp();

        given()
                .port(app.port())
                .queryParam("studentId", "student-1")
                .when()
                .get("/api/bookings")
                .then()
                .statusCode(200)
                .body("id", hasItem(id));
    }

    @Test
    void rulesStillUseSavedBookingsAfterARestart() {
        app = startApp();
        createBooking("R1", "student-1", "2026-10-12T09:00", "2026-10-12T10:00");

        restartApp();

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
    void cancelledBookingStaysCancelledAfterARestart() {
        app = startApp();
        String id = createBooking("R1", "student-1", "2026-10-12T09:00", "2026-10-12T10:00");

        given()
                .port(app.port())
                .header(App.STUDENT_HEADER, "student-1")
                .when()
                .delete("/api/bookings/{id}", id)
                .then()
                .statusCode(204);

        restartApp();

        given()
                .port(app.port())
                .queryParam("studentId", "student-1")
                .when()
                .get("/api/bookings")
                .then()
                .statusCode(200)
                .body("size()", equalTo(0));
    }

    // ---------- Helpers ----------

    private Javalin startApp() {
        // A new Database object each time, just like a real restart
        Database database = Database.inFile(databaseFile.toString());
        database.createSchema();

        BookingService bookings = new BookingService(
                new JdbcBookingRepository(database), new BookingPolicy(DAY_BEFORE));

        return App.create(InMemoryRoomRepository.withDefaultRooms(), bookings).start(0);
    }

    private void restartApp() {
        app.stop();
        app = startApp();
    }

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