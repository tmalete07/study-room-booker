package za.co.tapiso.studyroom.domain;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * A period of time with a start and an end. The end must come after the start.
 */
public record TimeSlot(LocalDateTime start, LocalDateTime end) {

    public TimeSlot {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Start and end are required");
        }
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("End must be after start");
        }
    }

    public long durationInMinutes() {
        return Duration.between(start, end).toMinutes();
    }

    /**
     * Two slots overlap when each one starts before the other one ends.
     * Slots that only touch (one ends exactly when the other starts) do not overlap.
     */
    public boolean overlaps(TimeSlot other) {
        if (other == null) {
            throw new IllegalArgumentException("Other slot is required");
        }
        return this.start.isBefore(other.end) && other.start.isBefore(this.end);
    }
}