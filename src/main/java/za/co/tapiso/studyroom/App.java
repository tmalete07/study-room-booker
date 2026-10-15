package za.co.tapiso.studyroom;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.http.staticfiles.Location;
import io.javalin.json.JavalinJackson;
import za.co.tapiso.studyroom.api.CreateBookingRequest;
import za.co.tapiso.studyroom.api.NotFoundException;
import za.co.tapiso.studyroom.domain.Booking;
import za.co.tapiso.studyroom.domain.BookingPolicy;
import za.co.tapiso.studyroom.domain.TimeSlot;
import za.co.tapiso.studyroom.repository.InMemoryBookingRepository;
import za.co.tapiso.studyroom.repository.InMemoryRoomRepository;
import za.co.tapiso.studyroom.repository.RoomRepository;
import za.co.tapiso.studyroom.service.BookingConflictException;
import za.co.tapiso.studyroom.service.BookingService;

import java.time.Clock;
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
        return create(rooms, defaultBookingService());
    }

    /**
     * Builds the app with the given rooms and booking service.
     * Tests use this to pass a booking service with a fixed clock.
     */
    public static Javalin create(RoomRepository rooms, BookingService bookings) {
        return Javalin.create(config -> {
                    config.staticFiles.add("/public", Location.CLASSPATH);
                    config.jsonMapper(new JavalinJackson(jsonMapper(), false));
                })
                .get("/api/health", ctx -> ctx.json(Map.of("status", "UP")))
                .get("/api/rooms", ctx -> ctx.json(rooms.findAll()))
                .post("/api/bookings", ctx -> {
                    CreateBookingRequest request = readBookingRequest(ctx);
                    requireRoomExists(rooms, request.roomId());

                    Booking booking = bookings.book(
                            request.roomId(),
                            request.studentId(),
                            new TimeSlot(request.start(), request.end()));
                    ctx.status(HttpStatus.CREATED).json(booking);
                })
                .exception(IllegalArgumentException.class,
                        (e, ctx) -> sendError(ctx, HttpStatus.BAD_REQUEST, e))
                .exception(NotFoundException.class,
                        (e, ctx) -> sendError(ctx, HttpStatus.NOT_FOUND, e))
                .exception(BookingConflictException.class,
                        (e, ctx) -> sendError(ctx, HttpStatus.CONFLICT, e));
    }

    public static void main(String[] args) {
        create().start(DEFAULT_PORT);
    }

    private static CreateBookingRequest readBookingRequest(Context ctx) {
        CreateBookingRequest request;
        try {
            request = ctx.bodyAsClass(CreateBookingRequest.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Request body must be valid booking JSON");
        }
        if (request == null) {
            throw new IllegalArgumentException("Request body must be valid booking JSON");
        }
        return request;
    }

    private static void requireRoomExists(RoomRepository rooms, String roomId) {
        // A missing room id is a bad request (400), handled by Booking's own validation
        if (roomId == null || roomId.isBlank()) {
            return;
        }
        if (rooms.findById(roomId).isEmpty()) {
            throw new NotFoundException("Room not found: " + roomId);
        }
    }

    private static void sendError(Context ctx, HttpStatus status, Exception e) {
        ctx.status(status).json(Map.of("error", e.getMessage()));
    }

    private static BookingService defaultBookingService() {
        return new BookingService(
                new InMemoryBookingRepository(),
                new BookingPolicy(Clock.systemDefaultZone()));
    }

    private static ObjectMapper jsonMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}