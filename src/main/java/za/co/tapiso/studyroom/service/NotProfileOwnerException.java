package za.co.tapiso.studyroom.service;

/**
 * Thrown when a learner tries to edit someone else's profile.
 */
public class NotProfileOwnerException extends RuntimeException {

    public NotProfileOwnerException(String message) {
        super(message);
    }
}