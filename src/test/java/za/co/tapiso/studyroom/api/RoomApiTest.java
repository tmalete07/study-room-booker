package za.co.tapiso.studyroom.api;

import io.javalin.Javalin;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import za.co.tapiso.studyroom.App;
import za.co.tapiso.studyroom.domain.Room;
import za.co.tapiso.studyroom.repository.InMemoryRoomRepository;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

class RoomApiTest {

    private Javalin app;

    @AfterEach
    void stopServer() {
        if (app != null) {
            app.stop();
        }
    }

    @Test
    void healthEndpointReportsUp() {
        startServerWith(List.of());

        given()
                .port(app.port())
                .when()
                .get("/api/health")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("status", equalTo("UP"));
    }

    @Test
    void listsAllRooms() {
        startServerWith(List.of(
                new Room("R1", "Quiet Room", 4),
                new Room("R2", "Group Room", 8)));

        given()
                .port(app.port())
                .when()
                .get("/api/rooms")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", equalTo(2))
                .body("[0].id", equalTo("R1"))
                .body("[0].name", equalTo("Quiet Room"))
                .body("[0].capacity", equalTo(4))
                .body("[1].id", equalTo("R2"))
                .body("[1].capacity", equalTo(8));
    }

    @Test
    void returnsEmptyListWhenThereAreNoRooms() {
        startServerWith(List.of());

        given()
                .port(app.port())
                .when()
                .get("/api/rooms")
                .then()
                .statusCode(200)
                .body("size()", equalTo(0));
    }

    private void startServerWith(List<Room> rooms) {
        // Port 0 means "pick any free port", so tests never clash with a running app
        app = App.create(new InMemoryRoomRepository(rooms)).start(0);
    }
}