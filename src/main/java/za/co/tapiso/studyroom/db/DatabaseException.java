package za.co.tapiso.studyroom.db;

/**
 * Wraps database errors so the rest of the app does not have to handle SQLException everywhere.
 */
public class DatabaseException extends RuntimeException {

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}