package za.co.tapiso.studyroom.domain;

/**
 * Thrown when a learner tries to book a grade room that belongs to another grade.
 */
public class RoomNotForGradeException extends RuntimeException {

    public RoomNotForGradeException(String message) {
        super(message);
    }
}