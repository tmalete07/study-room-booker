package za.co.tapiso.studyroom.domain;

/**
 * A learner in the admin office register: their student number and full name.
 * The learner's grade comes from the student number.
 */
public record Learner(StudentNumber studentNumber, String name) {

    public Learner {
        if (studentNumber == null) {
            throw new IllegalArgumentException("Student number is required");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }
    }

    public int grade() {
        return studentNumber.grade();
    }
}