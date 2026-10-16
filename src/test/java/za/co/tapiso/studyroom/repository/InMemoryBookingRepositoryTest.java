package za.co.tapiso.studyroom.repository;

/**
 * Runs every BookingRepositoryContract test against the in-memory repository.
 */
class InMemoryBookingRepositoryTest extends BookingRepositoryContract {

    @Override
    protected BookingRepository createRepository() {
        return new InMemoryBookingRepository();
    }
}