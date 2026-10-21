package za.co.tapiso.studyroom.service;

/**
 * Thrown when a student number is valid but is not in the admin office register.
 */
public class LearnerNotFoundException extends RuntimeException {

    public LearnerNotFoundException(String message) {
        super(message);
    }
}