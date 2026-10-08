package za.co.tapiso.studyroom;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;

import java.util.Map;

public class App {

    public static final int DEFAULT_PORT = 7070;

    /**
     * Builds the app without starting it, so tests can start it on any port.
     */
    public static Javalin create() {
        return Javalin.create(config ->
                        config.staticFiles.add("/public", Location.CLASSPATH))
                .get("/api/health", ctx -> ctx.json(Map.of("status", "UP")));
    }

    public static void main(String[] args) {
        create().start(DEFAULT_PORT);
    }
}