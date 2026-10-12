package za.co.tapiso.studyroom.service;

/**
 * Thrown when a booking is valid on its own but clashes with bookings that already exist.
 */
public class BookingConflictException extends RuntimeException {

    public BookingConflictException(String message) {
        super(message);
    }
}