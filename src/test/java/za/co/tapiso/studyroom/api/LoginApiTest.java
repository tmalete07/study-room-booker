package za.co.tapiso.studyroom.api;

import io.javalin.Javalin;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.co.tapiso.studyroom.App;
import za.co.tapiso.studyroom.domain.BookingPolicy;
import za.co.tapiso.studyroom.domain.Learner;
import za.co.tapiso.studyroom.domain.StudentNumber;
import za.co.tapiso.studyroom.repository.InMemoryBookingRepository;
import za.co.tapiso.studyroom.repository.InMemoryLearnerRepository;
import za.co.tapiso.studyroom.repository.InMemoryRoomRepository;
import za.co.tapiso.studyroom.service.BookingService;

import java.time.Clock;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

class LoginApiTest {

    private Javalin app;

    @BeforeEach
    void startServer() {
        InMemoryLearnerRepository register = new InMemoryLearnerRepository(List.of(
                new Learner(StudentNumber.parse("TSS4001-11"), "Palesa Ndlovu"),
                new Learner(StudentNumber.parse("TSS5001-12"), "Zanele Mthembu")));

        BookingService bookings = new BookingService(
                new InMemoryBookingRepository(), new BookingPolicy(Clock.systemDefaultZone()));

        app = App.create(InMemoryRoomRepository.withDefaultRooms(), bookings, register).start(0);
    }

    @AfterEach
    void stopServer() {
        app.stop();
    }

    // ---------- 200 OK ----------

    @Test
    void logsInAndReturnsTheLearnersDetails() {
        given()
                .port(app.port())
                .contentType(ContentType.JSON)
                .body(loginJson("Palesa Ndlovu", "TSS4001-11"))
                .when()
                .post("/api/login")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("studentNumber", equalTo("TSS4001-11"))
                .body("name", equalTo("Palesa Ndlovu"))
                .body("grade", equalTo(11));
    }

    @Test
    void lowerCaseNumberComesBackInCapitals() {
        given()
                .port(app.port())
                .contentType(ContentType.JSON)
                .body(loginJson("palesa ndlovu", "tss4001-11"))
                .when()
                .post("/api/login")
                .then()
                .statusCode(200)
                .body("studentNumber", equalTo("TSS4001-11"))
                .body("name", equalTo("Palesa Ndlovu"));
    }

    // ---------- 401 Unauthorized ----------

    @Test
    void wrongNameGives401() {
        given()
                .port(app.port())
                .contentType(ContentType.JSON)
                .body(loginJson("Zanele Mthembu", "TSS4001-11"))
                .when()
                .post("/api/login")
                .then()
                .statusCode(401)
                .body("error", equalTo("Student number or name is incorrect"));
    }

    @Test
    void unknownStudentNumberGivesTheSame401() {
        given()
                .port(app.port())
                .contentType(ContentType.JSON)
                .body(loginJson("Palesa Ndlovu", "TSS9999-11"))
                .when()
                .post("/api/login")
                .then()
                .statusCode(401)
                .body("error", equalTo("Student number or name is incorrect"));
    }

    // ---------- 400 Bad Request ----------

    @Test
    void gradeOutsideEightToTwelveGives400() {
        given()
                .port(app.port())
                .contentType(ContentType.JSON)
                .body(loginJson("Palesa Ndlovu", "TSS4001-13"))
                .when()
                .post("/api/login")
                .then()
                .statusCode(400)
                .body("error", equalTo("Invalid student number. Grade must be between 8 and 12"));
    }

    @Test
    void spacesInStudentNumberGive400() {
        given()
                .port(app.port())
                .contentType(ContentType.JSON)
                .body(loginJson("Palesa Ndlovu", "TSS4001 -11"))
                .when()
                .post("/api/login")
                .then()
                .statusCode(400)
                .body("error", equalTo("Student number cannot contain spaces"));
    }

    @Test
    void missingNameGives400() {
        given()
                .port(app.port())
                .contentType(ContentType.JSON)
                .body("""
                        {"studentNumber": "TSS4001-11"}
                        """)
                .when()
                .post("/api/login")
                .then()
                .statusCode(400)
                .body("error", equalTo("Name is required"));
    }

    @Test
    void malformedJsonGives400() {
        given()
                .port(app.port())
                .contentType(ContentType.JSON)
                .body("not json at all")
                .when()
                .post("/api/login")
                .then()
                .statusCode(400)
                .body("error", equalTo("Request body must be valid login JSON"));
    }

    private static String loginJson(String name, String studentNumber) {
        return """
                {
                  "name": "%s",
                  "studentNumber": "%s"
                }
                """.formatted(name, studentNumber);
    }
}