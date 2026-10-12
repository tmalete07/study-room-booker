package za.co.tapiso.studyroom.repository;

import za.co.tapiso.studyroom.domain.Booking;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Stores and finds bookings. The rest of the app only talks to this interface,
 * so the storage can change (memory now, a database later) without changing the rules.
 */
public interface BookingRepository {

    Booking save(Booking booking);

    Optional<Booking> findById(String id);

    List<Booking> findAll();

    List<Booking> findByRoomId(String roomId);

    List<Booking> findByStudentIdOn(String studentId, LocalDate date);

    void deleteById(String id);
}