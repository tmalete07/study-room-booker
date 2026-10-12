package za.co.tapiso.studyroom.domain;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Checks the rules a time slot must follow before it can be booked.
 */
public class BookingPolicy {

    public static final LocalTime OPENS_AT = LocalTime.of(8, 0);
    public static final LocalTime CLOSES_AT = LocalTime.of(20, 0);

    private final Clock clock;

    /**
     * The clock tells the policy what "now" is.
     * The real app passes the system clock; tests pass a fixed clock so "now" never changes.
     */
    public BookingPolicy(Clock clock) {
        if (clock == null) {
            throw new IllegalArgumentException("Clock is required");
        }
        this.clock = clock;
    }

    public void validate(TimeSlot slot) {
        if (slot == null) {
            throw new IllegalArgumentException("Time slot is required");
        }
        checkNotInPast(slot);
        checkOpeningHours(slot);
    }

    private void checkNotInPast(TimeSlot slot) {
        LocalDateTime now = LocalDateTime.now(clock);

        if (slot.start().isBefore(now)) {
            throw new IllegalArgumentException("Bookings cannot start in the past");
        }
    }

    private void checkOpeningHours(TimeSlot slot) {
        LocalDate day = slot.start().toLocalDate();
        LocalDateTime opens = day.atTime(OPENS_AT);
        LocalDateTime closes = day.atTime(CLOSES_AT);

        if (slot.start().isBefore(opens) || slot.end().isAfter(closes)) {
            throw new IllegalArgumentException("Bookings must be between 08:00 and 20:00");
        }
    }
}