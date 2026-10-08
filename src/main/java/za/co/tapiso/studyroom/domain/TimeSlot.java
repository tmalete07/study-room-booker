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
}