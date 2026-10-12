package za.co.tapiso.studyroom.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BookingPolicyTest {

    private static final ZoneId ZONE = ZoneId.of("Africa/Johannesburg");
    private static final LocalDate MONDAY = LocalDate.of(2026, 10, 12);

    // The day before MONDAY, so the opening-hours tests are never "in the past"
    private final BookingPolicy policy = new BookingPolicy(clockAt(LocalDateTime.of(2026, 10, 11, 12, 0)));

    // ---------- Opening hours: 08:00 to 20:00 ----------

    @ParameterizedTest(name = "{0} to {1} is allowed")
    @CsvSource({
            "08:00, 09:00",
            "08:00, 08:30",
            "12:00, 14:00",
            "19:00, 20:00",
            "19:30, 20:00"
    })
    void acceptsSlotsInsideOpeningHours(String start, String end) {
        TimeSlot slot = slot(start, end);

        assertThatCode(() -> policy.validate(slot)).doesNotThrowAnyException();
    }

    @ParameterizedTest(name = "{0} to {1} is rejected")
    @CsvSource({
            "06:00, 07:00",
            "07:00, 08:30",
            "07:59, 09:00",
            "19:00, 21:00",
            "19:30, 20:01",
            "20:00, 21:00"
    })
    void rejectsSlotsOutsideOpeningHours(String start, String end) {
        TimeSlot slot = slot(start, end);

        assertThatThrownBy(() -> policy.validate(slot))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Bookings must be between 08:00 and 20:00");
    }

    @Test
    void rejectsSlotThatRunsPastMidnight() {
        TimeSlot overnight = new TimeSlot(
                MONDAY.atTime(23, 0),
                MONDAY.plusDays(1).atTime(0, 30));

        assertThatThrownBy(() -> policy.validate(overnight))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Bookings must be between 08:00 and 20:00");
    }

    @Test
    void rejectsMissingSlot() {
        assertThatThrownBy(() -> policy.validate(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Time slot is required");
    }

    // ---------- No bookings in the past ----------
    // "Now" is frozen at Monday 12 October 2026, 10:00

    private final BookingPolicy policyAtTenAm = new BookingPolicy(clockAt(MONDAY.atTime(10, 0)));

    @ParameterizedTest(name = "starting at {0} is rejected")
    @ValueSource(strings = {
            "2026-10-11T12:00",   // yesterday
            "2026-10-12T09:00",   // one hour ago
            "2026-10-12T09:59"    // one minute ago
    })
    void rejectsSlotsStartingInThePast(String start) {
        TimeSlot slot = oneHourFrom(start);

        assertThatThrownBy(() -> policyAtTenAm.validate(slot))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Bookings cannot start in the past");
    }

    @ParameterizedTest(name = "starting at {0} is allowed")
    @ValueSource(strings = {
            "2026-10-12T10:00",   // exactly now
            "2026-10-12T10:01",   // one minute from now
            "2026-10-13T08:00"    // tomorrow
    })
    void acceptsSlotsStartingNowOrLater(String start) {
        TimeSlot slot = oneHourFrom(start);

        assertThatCode(() -> policyAtTenAm.validate(slot)).doesNotThrowAnyException();
    }

    @Test
    void rejectsSlotThatAlreadyStarted() {
        TimeSlot alreadyStarted = new TimeSlot(MONDAY.atTime(9, 30), MONDAY.atTime(10, 30));

        assertThatThrownBy(() -> policyAtTenAm.validate(alreadyStarted))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Bookings cannot start in the past");
    }

    @Test
    void rejectsMissingClock() {
        assertThatThrownBy(() -> new BookingPolicy(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Clock is required");
    }

    // ---------- Helpers ----------

    private static Clock clockAt(LocalDateTime time) {
        return Clock.fixed(time.atZone(ZONE).toInstant(), ZONE);
    }

    private static TimeSlot slot(String start, String end) {
        return new TimeSlot(
                MONDAY.atTime(LocalTime.parse(start)),
                MONDAY.atTime(LocalTime.parse(end)));
    }

    private static TimeSlot oneHourFrom(String start) {
        LocalDateTime begin = LocalDateTime.parse(start);
        return new TimeSlot(begin, begin.plusHours(1));
    }
}