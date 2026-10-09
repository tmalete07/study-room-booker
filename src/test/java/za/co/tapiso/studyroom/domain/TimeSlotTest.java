package za.co.tapiso.studyroom.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TimeSlotTest {

    private final LocalDateTime nineAm = LocalDateTime.of(2026, 10, 9, 9, 0);

    @Test
    void calculatesDurationInMinutes() {
        TimeSlot slot = new TimeSlot(nineAm, nineAm.plusMinutes(90));

        assertThat(slot.durationInMinutes()).isEqualTo(90);
    }

    @Test
    void rejectsEndBeforeStart() {
        assertThatThrownBy(() -> new TimeSlot(nineAm, nineAm.minusHours(1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("End must be after start");
    }

    @Test
    void rejectsEndEqualToStart() {
        assertThatThrownBy(() -> new TimeSlot(nineAm, nineAm))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsMissingStart() {
        assertThatThrownBy(() -> new TimeSlot(null, nineAm))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Start and end are required");
    }

    // ---------- overlaps() ----------

    @ParameterizedTest(name = "{0}")
    @CsvSource({
            // scenario,                      A start, A end,  B start, B end,  overlaps?
            "B starts when A ends,            09:00,   10:00,  10:00,   11:00,  false",
            "B ends when A starts,            10:00,   11:00,  09:00,   10:00,  false",
            "B is completely after A,         09:00,   10:00,  11:00,   12:00,  false",
            "Same slot,                       09:00,   10:00,  09:00,   10:00,  true",
            "B is inside A,                   09:00,   12:00,  10:00,   11:00,  true",
            "A is inside B,                   10:00,   11:00,  09:00,   12:00,  true",
            "B overlaps the end of A,         09:00,   11:00,  10:00,   12:00,  true",
            "B overlaps the start of A,       10:00,   12:00,  09:00,   11:00,  true",
            "Overlap of just one minute,      09:00,   10:01,  10:00,   11:00,  true"
    })
    void detectsOverlap(String scenario,
                        String aStart, String aEnd,
                        String bStart, String bEnd,
                        boolean expected) {
        TimeSlot a = slot(aStart, aEnd);
        TimeSlot b = slot(bStart, bEnd);

        assertThat(a.overlaps(b))
                .as("A overlaps B")
                .isEqualTo(expected);
        assertThat(b.overlaps(a))
                .as("B overlaps A (overlap must work both ways)")
                .isEqualTo(expected);
    }

    @Test
    void rejectsMissingOtherSlot() {
        TimeSlot slot = slot("09:00", "10:00");

        assertThatThrownBy(() -> slot.overlaps(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Other slot is required");
    }

    private TimeSlot slot(String start, String end) {
        LocalDate day = LocalDate.of(2026, 10, 9);
        return new TimeSlot(
                LocalDateTime.of(day, LocalTime.parse(start)),
                LocalDateTime.of(day, LocalTime.parse(end)));
    }
}