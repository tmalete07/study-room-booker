package za.co.tapiso.studyroom.api;

import io.javalin.Javalin;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.co.tapiso.studyroom.App;
import za.co.tapiso.studyroom.domain.BookingPolicy;
import za.co.tapiso.studyroom.domain.Learner;
import za.co.tapiso.studyroom.domain.LearnerProfile;
import za.co.tapiso.studyroom.domain.StudentNumber;
import za.co.tapiso.studyroom.domain.StudyGoal;
import za.co.tapiso.studyroom.repository.InMemoryBookingRepository;
import za.co.tapiso.studyroom.repository.InMemoryLearnerRepository;
import za.co.tapiso.studyroom.repository.InMemoryRoomRepository;
import za.co.tapiso.studyroom.service.BookingService;

import java.time.Clock;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

class ProfileApiTest {

    private static final String PALESA = "TSS4001-11";
    private static final String ZANELE = "TSS5001-12";

    private Javalin app;

    @BeforeEach
    void startServer() {
        InMemoryLearnerRepository register = new InMemoryLearnerRepository(List.of(
                new Learner(StudentNumber.parse(PALESA), "Palesa Ndlovu"),
                new Learner(StudentNumber.parse(ZANELE), "Zanele Mthembu")));
        register.saveProfile(StudentNumber.parse(PALESA), new LearnerProfile(
                "11B", "palesa.ndlovu@learners.tswelopele.example",
                List.of("Mathematics", "Physical Sciences"), "I want to study medicine.",
                List.of(new StudyGoal("Improve Physical Sciences", "Study twice a week."))));

        BookingService bookings = new BookingService(
                new InMemoryBookingRepository(), new BookingPolicy(Clock.systemDefaultZone()));

        app = App.create(InMemoryRoomRepository.withDefaultRooms(), bookings, register).start(0);
    }

    @AfterEach
    void stopServer() {
        app.stop();
    }

    // ---------- GET: view a profile ----------

    @Test
    void returnsTheLearnersProfile() {
        given()
                .port(app.port())
                .when()
                .get("/api/learners/{number}/profile", PALESA)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("studentNumber", equalTo(PALESA))
                .body("firstName", equalTo("Palesa"))
                .body("surname", equalTo("Ndlovu"))
                .body("grade", equalTo(11))
                .body("className", equalTo("11B"))
                .body("email", equalTo("palesa.ndlovu@learners.tswelopele.example"))
                .body("subjects", contains("Mathematics", "Physical Sciences"))
                .body("about", equalTo("I want to study medicine."))
                .body("goals[0].title", equalTo("Improve Physical Sciences"))
                .body("goals[0].detail", equalTo("Study twice a week."));
    }

    @Test
    void lowerCaseNumberInTheAddressWorks() {
        given()
                .port(app.port())
                .when()
                .get("/api/learners/{number}/profile", "tss4001-11")
                .then()
                .statusCode(200)
                .body("studentNumber", equalTo(PALESA));
    }

    @Test
    void unknownLearnerGives404() {
        given()
                .port(app.port())
                .when()
                .get("/api/learners/{number}/profile", "TSS9999-11")
                .then()
                .statusCode(404)
                .body("error", equalTo("Learner not found: TSS9999-11"));
    }

    @Test
    void invalidStudentNumberGives400() {
        given()
                .port(app.port())
                .when()
                .get("/api/learners/{number}/profile", "TSS4001-13")
                .then()
                .statusCode(400)
                .body("error", equalTo("Invalid student number. Grade must be between 8 and 12"));
    }

    // ---------- PUT: edit a profile ----------

    @Test
    void learnerCanEditTheirOwnProfile() {
        given()
                .port(app.port())
                .header(App.STUDENT_HEADER, PALESA)
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "subjects": ["Mathematics", "Life Sciences"],
                          "about": "I want to be a doctor.",
                          "goals": [{"title": "Pass Life Sciences", "detail": "Revise every Friday."}]
                        }
                        """)
                .when()
                .put("/api/learners/{number}/profile", PALESA)
                .then()
                .statusCode(200)
                .body("subjects", contains("Mathematics", "Life Sciences"))
                .body("about", equalTo("I want to be a doctor."))
                .body("goals[0].title", equalTo("Pass Life Sciences"))
                .body("className", equalTo("11B"));

        // The change was really saved, not just echoed back
        given()
                .port(app.port())
                .when()
                .get("/api/learners/{number}/profile", PALESA)
                .then()
                .body("about", equalTo("I want to be a doctor."));
    }

    @Test
    void learnerCannotEditSomeoneElsesProfileWith403() {
        given()
                .port(app.port())
                .header(App.STUDENT_HEADER, ZANELE)
                .contentType(ContentType.JSON)
                .body("""
                        {"subjects": ["Art"], "about": "Changed by someone else", "goals": []}
                        """)
                .when()
                .put("/api/learners/{number}/profile", PALESA)
                .then()
                .statusCode(403)
                .body("error", equalTo("You can only edit your own profile"));

        given()
                .port(app.port())
                .when()
                .get("/api/learners/{number}/profile", PALESA)
                .then()
                .body("about", equalTo("I want to study medicine."));
    }

    @Test
    void editingWithoutStudentHeaderGives400() {
        given()
                .port(app.port())
                .contentType(ContentType.JSON)
                .body("""
                        {"subjects": ["Art"], "about": "", "goals": []}
                        """)
                .when()
                .put("/api/learners/{number}/profile", PALESA)
                .then()
                .statusCode(400)
                .body("error", equalTo("Student number is required"));
    }

    @Test
    void aboutLongerThan500CharactersGives400() {
        given()
                .port(app.port())
                .header(App.STUDENT_HEADER, PALESA)
                .contentType(ContentType.JSON)
                .body("""
                        {"subjects": ["Mathematics"], "about": "%s", "goals": []}
                        """.formatted("x".repeat(501)))
                .when()
                .put("/api/learners/{number}/profile", PALESA)
                .then()
                .statusCode(400)
                .body("error", equalTo("About me must be 500 characters or fewer"));
    }

    @Test
    void goalWithoutTitleGivesTheRealReason() {
        given()
                .port(app.port())
                .header(App.STUDENT_HEADER, PALESA)
                .contentType(ContentType.JSON)
                .body("""
                        {"subjects": ["Mathematics"], "about": "", "goals": [{"title": "  ", "detail": "x"}]}
                        """)
                .when()
                .put("/api/learners/{number}/profile", PALESA)
                .then()
                .statusCode(400)
                .body("error", equalTo("Goal title is required"));
    }

    @Test
    void extraFieldsLikeClassAreRejectedWith400() {
        given()
                .port(app.port())
                .header(App.STUDENT_HEADER, PALESA)
                .contentType(ContentType.JSON)
                .body("""
                        {"subjects": ["Mathematics"], "about": "", "goals": [], "className": "12A"}
                        """)
                .when()
                .put("/api/learners/{number}/profile", PALESA)
                .then()
                .statusCode(400)
                .body("error", equalTo("Request body must be valid profile JSON"));

        given()
                .port(app.port())
                .when()
                .get("/api/learners/{number}/profile", PALESA)
                .then()
                .body("className", equalTo("11B"));
    }

    @Test
    void malformedJsonGives400() {
        given()
                .port(app.port())
                .header(App.STUDENT_HEADER, PALESA)
                .contentType(ContentType.JSON)
                .body("not json")
                .when()
                .put("/api/learners/{number}/profile", PALESA)
                .then()
                .statusCode(400)
                .body("error", equalTo("Request body must be valid profile JSON"));
    }
}