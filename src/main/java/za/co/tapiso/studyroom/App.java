package za.co.tapiso.studyroom;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import za.co.tapiso.studyroom.repository.InMemoryRoomRepository;
import za.co.tapiso.studyroom.repository.RoomRepository;

import java.util.Map;

public class App {

    public static final int DEFAULT_PORT = 7070;

    /**
     * Builds the app without starting it, so tests can start it on any port.
     */
    public static Javalin create() {
        return create(InMemoryRoomRepository.withDefaultRooms());
    }

    /**
     * Builds the app with the given rooms. Tests use this to control exactly which rooms exist.
     */
    public static Javalin create(RoomRepository rooms) {
        return Javalin.create(config ->
                        config.staticFiles.add("/public", Location.CLASSPATH))
                .get("/api/health", ctx -> ctx.json(Map.of("status", "UP")))
                .get("/api/rooms", ctx -> ctx.json(rooms.findAll()));
    }

    public static void main(String[] args) {
        create().start(DEFAULT_PORT);
    }
}