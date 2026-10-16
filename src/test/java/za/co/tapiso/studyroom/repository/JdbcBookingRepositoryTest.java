package za.co.tapiso.studyroom.repository;

import org.junit.jupiter.api.Test;
import za.co.tapiso.studyroom.db.Database;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Runs every BookingRepositoryContract test against a real H2 database,
 * plus tests that only make sense for a database.
 */
class JdbcBookingRepositoryTest extends BookingRepositoryContract {

    private Database database;

    @Override
    protected BookingRepository createRepository() {
        // A brand-new, empty in-memory database for every test
        database = Database.inMemory("bookings-" + UUID.randomUUID());
        database.createSchema();
        return new JdbcBookingRepository(database);
    }

    @Test
    void dataIsStillThereForANewRepositoryOnTheSameDatabase() {
        repository.save(booking("B1", "R1", "student-1", MONDAY_NINE_AM));

        BookingRepository anotherRepository = new JdbcBookingRepository(database);

        assertThat(anotherRepository.findById("B1")).isPresent();
    }

    @Test
    void rejectsMissingDatabase() {
        assertThatThrownBy(() -> new JdbcBookingRepository(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Database is required");
    }
}