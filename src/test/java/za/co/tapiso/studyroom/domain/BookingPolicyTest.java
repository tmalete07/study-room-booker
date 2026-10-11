package za.co.tapiso.studyroom.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BookingPolicyTest {

    private static final LocalDate MONDAY = LocalDate.of(2026, 10, 12);

    private final BookingPolicy policy = new BookingPolicy();

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

    // ---------- Helpers ----------

    private static TimeSlot slot(String start, String end) {
        return new TimeSlot(
                MONDAY.atTime(LocalTime.parse(start)),
                MONDAY.atTime(LocalTime.parse(end)));
    }
}