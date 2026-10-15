package za.co.tapiso.studyroom.service;

/**
 * Thrown when a student tries to change a booking that belongs to someone else.
 */
public class NotBookingOwnerException extends RuntimeException {

    public NotBookingOwnerException(String message) {
        super(message);
    }
}