package za.co.tapiso.studyroom.repository;

import za.co.tapiso.studyroom.domain.Booking;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Keeps bookings in memory. Everything is lost when the app stops.
 */
public class InMemoryBookingRepository implements BookingRepository {

    private final Map<String, Booking> bookings = new LinkedHashMap<>();

    @Override
    public Booking save(Booking booking) {
        if (booking == null) {
            throw new IllegalArgumentException("Booking is required");
        }
        bookings.put(booking.id(), booking);
        return booking;
    }

    @Override
    public Optional<Booking> findById(String id) {
        return Optional.ofNullable(bookings.get(id));
    }

    @Override
    public List<Booking> findAll() {
        return List.copyOf(bookings.values());
    }

    @Override
    public List<Booking> findByRoomId(String roomId) {
        return bookings.values().stream()
                .filter(booking -> booking.roomId().equals(roomId))
                .toList();
    }

    @Override
    public List<Booking> findByStudentIdOn(String studentId, LocalDate date) {
        return bookings.values().stream()
                .filter(booking -> booking.studentId().equals(studentId))
                .filter(booking -> booking.slot().start().toLocalDate().equals(date))
                .toList();
    }

    @Override
    public void deleteById(String id) {
        bookings.remove(id);
    }
}