package za.co.tapiso.studyroom.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.co.tapiso.studyroom.db.Database;
import za.co.tapiso.studyroom.domain.Booking;
import za.co.tapiso.studyroom.domain.TimeSlot;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JdbcBookingRepositoryTest {

    private static final LocalDateTime MONDAY_NINE_AM = LocalDateTime.of(2026, 10, 12, 9, 0);

    private Database database;
    private JdbcBookingRepository repository;

    @BeforeEach
    void setUp() {
        // A brand-new, empty in-memory database for every test
        database = Database.inMemory("bookings-" + UUID.randomUUID());
        database.createSchema();
        repository = new JdbcBookingRepository(database);
    }

    @Test
    void savesAndFindsBookingById() {
        Booking booking = booking("B1", "R1", "student-1", MONDAY_NINE_AM);

        repository.save(booking);

        assertThat(repository.findById("B1")).contains(booking);
    }

    @Test
    void returnsEmptyWhenIdIsNotFound() {
        assertThat(repository.findById("does-not-exist")).isEmpty();
    }

    @Test
    void findsOnlyBookingsForTheGivenRoom() {
        repository.save(booking("B1", "R1", "student-1", MONDAY_NINE_AM));
        repository.save(booking("B2", "R2", "student-1", MONDAY_NINE_AM));
        repository.save(booking("B3", "R1", "student-2", MONDAY_NINE_AM.plusHours(2)));

        assertThat(repository.findByRoomId("R1"))
                .extracting(Booking::id)
                .containsExactlyInAnyOrder("B1", "B3");
    }

    @Test
    void findsStudentBookingsOnTheGivenDayOnly() {
        repository.save(booking("B1", "R1", "student-1", MONDAY_NINE_AM));
        repository.save(booking("LATE", "R2", "student-1", LocalDateTime.of(2026, 10, 12, 23, 0)));
        repository.save(booking("NEXT-DAY", "R1", "student-1", LocalDateTime.of(2026, 10, 13, 0, 0)));
        repository.save(booking("OTHER", "R1", "student-2", MONDAY_NINE_AM.plusHours(2)));

        assertThat(repository.findByStudentIdOn("student-1", MONDAY_NINE_AM.toLocalDate()))
                .extracting(Booking::id)
                .containsExactlyInAnyOrder("B1", "LATE");
    }

    @Test
    void savingWithSameIdReplacesTheBooking() {
        repository.save(booking("B1", "R1", "student-1", MONDAY_NINE_AM));
        repository.save(booking("B1", "R3", "student-1", MONDAY_NINE_AM));

        assertThat(repository.findAll()).hasSize(1);
        assertThat(repository.findById("B1").orElseThrow().roomId()).isEqualTo("R3");
    }

    @Test
    void deletesBookingById() {
        repository.save(booking("B1", "R1", "student-1", MONDAY_NINE_AM));
        repository.save(booking("B2", "R2", "student-1", MONDAY_NINE_AM));

        repository.deleteById("B1");

        assertThat(repository.findAll())
                .extracting(Booking::id)
                .containsExactly("B2");
    }

    @Test
    void deletingUnknownIdChangesNothing() {
        repository.save(booking("B1", "R1", "student-1", MONDAY_NINE_AM));

        repository.deleteById("does-not-exist");

        assertThat(repository.findAll()).hasSize(1);
    }

    @Test
    void dataIsStillThereForANewRepositoryOnTheSameDatabase() {
        repository.save(booking("B1", "R1", "student-1", MONDAY_NINE_AM));

        JdbcBookingRepository anotherRepository = new JdbcBookingRepository(database);

        assertThat(anotherRepository.findById("B1")).isPresent();
    }

    @Test
    void rejectsMissingBooking() {
        assertThatThrownBy(() -> repository.save(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Booking is required");
    }

    @Test
    void rejectsMissingDatabase() {
        assertThatThrownBy(() -> new JdbcBookingRepository(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Database is required");
    }

    private static Booking booking(String id, String roomId, String studentId, LocalDateTime start) {
        return new Booking(id, roomId, studentId, new TimeSlot(start, start.plusHours(1)));
    }
}