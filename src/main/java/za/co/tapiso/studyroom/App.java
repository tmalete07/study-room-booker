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
import za.co.tapiso.studyroom.api.LoginRequest;
import za.co.tapiso.studyroom.api.LoginResponse;
import za.co.tapiso.studyroom.api.NotFoundException;
import za.co.tapiso.studyroom.db.Database;
import za.co.tapiso.studyroom.domain.Booking;
import za.co.tapiso.studyroom.domain.BookingPolicy;
import za.co.tapiso.studyroom.domain.Learner;
import za.co.tapiso.studyroom.domain.Room;
import za.co.tapiso.studyroom.domain.RoomNotForGradeException;
import za.co.tapiso.studyroom.domain.StudentNumber;
import za.co.tapiso.studyroom.domain.TimeSlot;
import za.co.tapiso.studyroom.repository.InMemoryBookingRepository;
import za.co.tapiso.studyroom.repository.InMemoryLearnerRepository;
import za.co.tapiso.studyroom.repository.InMemoryRoomRepository;
import za.co.tapiso.studyroom.repository.JdbcBookingRepository;
import za.co.tapiso.studyroom.repository.LearnerRepository;
import za.co.tapiso.studyroom.repository.RoomRepository;
import za.co.tapiso.studyroom.service.BookingConflictException;
import za.co.tapiso.studyroom.service.BookingNotFoundException;
import za.co.tapiso.studyroom.service.BookingService;
import za.co.tapiso.studyroom.service.LearnerService;
import za.co.tapiso.studyroom.service.LoginFailedException;
import za.co.tapiso.studyroom.service.NotBookingOwnerException;

import java.time.Clock;
import java.util.Map;
import java.util.Optional;

public class App {

    public static final int DEFAULT_PORT = 7070;
    public static final String STUDENT_HEADER = "X-Student-Id";
    public static final String DATABASE_FILE = "./data/study-room-booker";

    /**
     * Builds the app without starting it, so tests can start it on any port.
     */
    public static Javalin create() {
        // The real app keeps bookings in a database file, so they survive a restart
        Database database = Database.inFile(DATABASE_FILE);
        database.createSchema();

        BookingService bookings = new BookingService(
                new JdbcBookingRepository(database),
                new BookingPolicy(Clock.systemDefaultZone()));

        return create(InMemoryRoomRepository.withDefaultRooms(), bookings);
    }

    /**
     * Builds the app with the given rooms. Tests use this to control exactly which rooms exist.
     */
    public static Javalin create(RoomRepository rooms) {
        return create(rooms, inMemoryBookingService());
    }

    /**
     * Builds the app with the given rooms and booking service.
     * Tests use this to pass a booking service with a fixed clock.
     */
    public static Javalin create(RoomRepository rooms, BookingService bookings) {
        return create(rooms, bookings, InMemoryLearnerRepository.withSampleLearners());
    }

    /**
     * Builds the app with the given rooms, booking service and learner register.
     * Tests use this to control exactly which learners can log in.
     */
    public static Javalin create(RoomRepository rooms, BookingService bookings, LearnerRepository learners) {
        LearnerService learnerService = new LearnerService(learners);

        return Javalin.create(config -> {
                    config.staticFiles.add("/public", Location.CLASSPATH);
                    config.jsonMapper(new JavalinJackson(jsonMapper(), false));
                })
                .get("/api/health", ctx -> ctx.json(Map.of("status", "UP")))
                .get("/api/rooms", ctx -> ctx.json(rooms.findAll()))
                .post("/api/login", ctx -> {
                    LoginRequest request = readBody(ctx, LoginRequest.class,
                            "Request body must be valid login JSON");
                    Learner learner = learnerService.login(request.name(), request.studentNumber());
                    ctx.json(LoginResponse.from(learner));
                })
                .get("/api/bookings", ctx -> {
                    StudentNumber student = StudentNumber.parse(ctx.queryParam("studentId"));
                    ctx.json(bookings.bookingsFor(student.value()));
                })
                .post("/api/bookings", ctx -> {
                    CreateBookingRequest request = readBody(ctx, CreateBookingRequest.class,
                            "Request body must be valid booking JSON");

                    StudentNumber student = StudentNumber.parse(request.studentId());
                    requireRegistered(learners, student);
                    findRoom(rooms, request.roomId()).ifPresent(room -> room.checkOpenTo(student));

                    Booking booking = bookings.book(
                            request.roomId(),
                            student.value(),
                            new TimeSlot(request.start(), request.end()));
                    ctx.status(HttpStatus.CREATED).json(booking);
                })
                .delete("/api/bookings/{id}", ctx -> {
                    StudentNumber student = StudentNumber.parse(ctx.header(STUDENT_HEADER));
                    bookings.cancel(ctx.pathParam("id"), student.value());
                    ctx.status(HttpStatus.NO_CONTENT);
                })
                .exception(IllegalArgumentException.class,
                        (e, ctx) -> sendError(ctx, HttpStatus.BAD_REQUEST, e))
                .exception(LoginFailedException.class,
                        (e, ctx) -> sendError(ctx, HttpStatus.UNAUTHORIZED, e))
                .exception(RoomNotForGradeException.class,
                        (e, ctx) -> sendError(ctx, HttpStatus.FORBIDDEN, e))
                .exception(NotFoundException.class,
                        (e, ctx) -> sendError(ctx, HttpStatus.NOT_FOUND, e))
                .exception(BookingNotFoundException.class,
                        (e, ctx) -> sendError(ctx, HttpStatus.NOT_FOUND, e))
                .exception(NotBookingOwnerException.class,
                        (e, ctx) -> sendError(ctx, HttpStatus.FORBIDDEN, e))
                .exception(BookingConflictException.class,
                        (e, ctx) -> sendError(ctx, HttpStatus.CONFLICT, e));
    }

    public static void main(String[] args) {
        create().start(DEFAULT_PORT);
    }

    /**
     * Reads the JSON body as the given type, or fails with a 400 and the given message.
     */
    private static <T> T readBody(Context ctx, Class<T> type, String errorMessage) {
        T body;
        try {
            body = ctx.bodyAsClass(type);
        } catch (Exception e) {
            throw new IllegalArgumentException(errorMessage);
        }
        if (body == null) {
            throw new IllegalArgumentException(errorMessage);
        }
        return body;
    }

    /**
     * Only learners in the admin office register can book.
     */
    private static void requireRegistered(LearnerRepository learners, StudentNumber student) {
        if (learners.findByStudentNumber(student).isEmpty()) {
            throw new NotFoundException("Learner not found: " + student);
        }
    }

    /**
     * Finds the room, or returns empty when no room id was sent. An unknown room id is a 404.
     */
    private static Optional<Room> findRoom(RoomRepository rooms, String roomId) {
        // A missing room id is a bad request (400), handled by Booking's own validation
        if (roomId == null || roomId.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(rooms.findById(roomId)
                .orElseThrow(() -> new NotFoundException("Room not found: " + roomId)));
    }

    private static void sendError(Context ctx, HttpStatus status, Exception e) {
        ctx.status(status).json(Map.of("error", e.getMessage()));
    }

    private static BookingService inMemoryBookingService() {
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