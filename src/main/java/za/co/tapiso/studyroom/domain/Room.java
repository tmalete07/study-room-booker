package za.co.tapiso.studyroom.domain;

/**
 * A study room that students can book. A room needs an id, a name and space for at least one person.
 */
public record Room(String id, String name, int capacity, Integer grade) {

    public Room {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Room id is required");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Room name is required");
        }
        if (capacity < 1) {
            throw new IllegalArgumentException("Capacity must be at least 1");
        }
        if (grade != null && (grade < StudentNumber.LOWEST_GRADE || grade > StudentNumber.HIGHEST_GRADE)) {
            throw new IllegalArgumentException("Room grade must be between 8 and 12");
        }
    }

    /**
     * A shared room that learners in every grade can book.
     */
    public Room(String id, String name, int capacity) {
        this(id, name, capacity, null);
    }

    /**
     * A grade room (like the Grade 11 Quiet Room) is only for learners in that grade.
     * A shared room (grade is null) is open to every grade.
     */
    public boolean isOpenTo(StudentNumber learner) {
        if (learner == null) {
            throw new IllegalArgumentException("Student number is required");
        }
        return grade == null || grade == learner.grade();
    }

    public void checkOpenTo(StudentNumber learner) {
        if (!isOpenTo(learner)) {
            throw new RoomNotForGradeException("The " + name + " is for Grade " + grade + " learners only");
        }
    }
}