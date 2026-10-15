package za.co.tapiso.studyroom.service;

import za.co.tapiso.studyroom.domain.Booking;
import za.co.tapiso.studyroom.domain.BookingPolicy;
import za.co.tapiso.studyroom.domain.TimeSlot;
import za.co.tapiso.studyroom.repository.BookingRepository;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Creates bookings by checking every rule before saving:
 * the booking's own rules, the policy (past and opening hours),
 * no overlap in the same room, and at most 2 bookings per student per day.
 */
public class BookingService {

    public static final int MAX_BOOKINGS_PER_DAY = 2;

    private final BookingRepository repository;
    private final BookingPolicy policy;

    public BookingService(BookingRepository repository, BookingPolicy policy) {
        if (repository == null || policy == null) {
            throw new IllegalArgumentException("Repository and policy are required");
        }
        this.repository = repository;
        this.policy = policy;
    }

    public Booking book(String roomId, String studentId, TimeSlot slot) {
        Booking booking = new Booking(UUID.randomUUID().toString(), roomId, studentId, slot);
        policy.validate(booking.slot());

        checkRoomIsFree(booking);
        checkDailyLimit(booking);

        return repository.save(booking);
    }

    /**
     * Returns one student's bookings, earliest first.
     */
    public List<Booking> bookingsFor(String studentId) {
        requireStudentId(studentId);

        return repository.findAll().stream()
                .filter(booking -> booking.studentId().equals(studentId))
                .sorted(Comparator.comparing((Booking booking) -> booking.slot().start()))
                .toList();
    }

    /**
     * Cancels a booking. Only the student who made the booking may cancel it.
     */
    public void cancel(String bookingId, String studentId) {
        requireStudentId(studentId);

        Booking booking = repository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found: " + bookingId));

        if (!booking.studentId().equals(studentId)) {
            throw new NotBookingOwnerException("Only the student who made a booking can cancel it");
        }
        repository.deleteById(bookingId);
    }

    private void checkRoomIsFree(Booking booking) {
        boolean clash = repository.findByRoomId(booking.roomId()).stream()
                .anyMatch(booking::clashesWith);

        if (clash) {
            throw new BookingConflictException("Room is already booked for that time");
        }
    }

    private void checkDailyLimit(Booking booking) {
        LocalDate day = booking.slot().start().toLocalDate();
        int bookingsThatDay = repository.findByStudentIdOn(booking.studentId(), day).size();

        if (bookingsThatDay >= MAX_BOOKINGS_PER_DAY) {
            throw new BookingConflictException(
                    "Students can make at most " + MAX_BOOKINGS_PER_DAY + " bookings per day");
        }
    }

    private static void requireStudentId(String studentId) {
        if (studentId == null || studentId.isBlank()) {
            throw new IllegalArgumentException("Student id is required");
        }
    }
}