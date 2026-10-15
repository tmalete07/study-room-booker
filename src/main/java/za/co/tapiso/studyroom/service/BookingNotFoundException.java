package za.co.tapiso.studyroom.service;

/**
 * Thrown when a booking id does not match any existing booking.
 */
public class BookingNotFoundException extends RuntimeException {

    public BookingNotFoundException(String message) {
        super(message);
    }
}