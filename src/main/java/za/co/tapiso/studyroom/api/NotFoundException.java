package za.co.tapiso.studyroom.api;

/**
 * Thrown when a request refers to something that does not exist, such as an unknown room.
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}