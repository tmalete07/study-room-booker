package za.co.tapiso.studyroom.domain;

/**
 * A study room that students can book. A room needs an id, a name and space for at least one person.
 */
public record Room(String id, String name, int capacity) {

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
    }
}