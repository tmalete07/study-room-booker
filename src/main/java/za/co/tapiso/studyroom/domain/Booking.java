package za.co.tapiso.studyroom.domain;

/**
 * A student's booking of one room for one time slot.
 * A booking must last between 30 minutes and 2 hours (both limits included).
 */
public record Booking(String id, String roomId, String studentId, TimeSlot slot) {

    public static final int MIN_DURATION_MINUTES = 30;
    public static final int MAX_DURATION_MINUTES = 120;

    public Booking {
        requireText(id, "Booking id is required");
        requireText(roomId, "Room id is required");
        requireText(studentId, "Student id is required");
        if (slot == null) {
            throw new IllegalArgumentException("Time slot is required");
        }

        long minutes = slot.durationInMinutes();
        if (minutes < MIN_DURATION_MINUTES || minutes > MAX_DURATION_MINUTES) {
            throw new IllegalArgumentException(
                    "Booking must be between " + MIN_DURATION_MINUTES
                            + " and " + MAX_DURATION_MINUTES + " minutes");
        }
    }

    /**
     * Two bookings clash when they are for the same room and their time slots overlap.
     */
    public boolean clashesWith(Booking other) {
        if (other == null) {
            throw new IllegalArgumentException("Other booking is required");
        }
        return roomId.equals(other.roomId) && slot.overlaps(other.slot);
    }

    private static void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }
}