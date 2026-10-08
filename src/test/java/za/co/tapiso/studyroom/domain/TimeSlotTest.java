package za.co.tapiso.studyroom.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

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
}