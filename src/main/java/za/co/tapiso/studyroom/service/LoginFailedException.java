package za.co.tapiso.studyroom.service;

/**
 * Thrown when a name and student number do not match a learner in the register.
 */
public class LoginFailedException extends RuntimeException {

    public LoginFailedException(String message) {
        super(message);
    }
}