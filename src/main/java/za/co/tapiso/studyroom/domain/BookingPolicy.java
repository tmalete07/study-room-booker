package za.co.tapiso.studyroom.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Checks the rules a time slot must follow before it can be booked.
 */
public class BookingPolicy {

    public static final LocalTime OPENS_AT = LocalTime.of(8, 0);
    public static final LocalTime CLOSES_AT = LocalTime.of(20, 0);

    public void validate(TimeSlot slot) {
        if (slot == null) {
            throw new IllegalArgumentException("Time slot is required");
        }
        checkOpeningHours(slot);
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