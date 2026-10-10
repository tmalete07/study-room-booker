package za.co.tapiso.studyroom.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BookingTest {

    private static final LocalDateTime NINE_AM = LocalDateTime.of(2026, 10, 12, 9, 0);

    // ---------- Duration rule: 30 to 120 minutes ----------

    @ParameterizedTest(name = "{0} minutes is allowed")
    @ValueSource(ints = {30, 31, 60, 90, 119, 120})
    void acceptsDurationsWithinLimits(int minutes) {
        Booking booking = bookingFor("R1", NINE_AM, minutes);

        assertThat(booking.slot().durationInMinutes()).isEqualTo(minutes);
    }

    @ParameterizedTest(name = "{0} minutes is rejected")
    @ValueSource(ints = {1, 15, 29, 121, 180, 240})
    void rejectsDurationsOutsideLimits(int minutes) {
        assertThatThrownBy(() -> bookingFor("R1", NINE_AM, minutes))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Booking must be between 30 and 120 minutes");
    }

    // ---------- Required fields ----------

    @ParameterizedTest(name = "booking id = \"{0}\"")
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void rejectsMissingBookingId(String id) {
        assertThatThrownBy(() -> new Booking(id, "R1", "student-1", slot(NINE_AM, 60)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Booking id is required");
    }

    @ParameterizedTest(name = "room id = \"{0}\"")
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void rejectsMissingRoomId(String roomId) {
        assertThatThrownBy(() -> new Booking("B1", roomId, "student-1", slot(NINE_AM, 60)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Room id is required");
    }

    @ParameterizedTest(name = "student id = \"{0}\"")
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void rejectsMissingStudentId(String studentId) {
        assertThatThrownBy(() -> new Booking("B1", "R1", studentId, slot(NINE_AM, 60)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Student id is required");
    }

    @Test
    void rejectsMissingSlot() {
        assertThatThrownBy(() -> new Booking("B1", "R1", "student-1", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Time slot is required");
    }

    // ---------- Clashes ----------

    @Test
    void clashesWhenSameRoomAndOverlappingTime() {
        Booking first = bookingFor("R1", NINE_AM, 60);
        Booking second = bookingFor("R1", NINE_AM.plusMinutes(30), 60);

        assertThat(first.clashesWith(second)).isTrue();
        assertThat(second.clashesWith(first)).isTrue();
    }

    @Test
    void doesNotClashWhenDifferentRoomsAtSameTime() {
        Booking first = bookingFor("R1", NINE_AM, 60);
        Booking second = bookingFor("R2", NINE_AM, 60);

        assertThat(first.clashesWith(second)).isFalse();
    }

    @Test
    void doesNotClashWhenSameRoomBackToBack() {
        Booking first = bookingFor("R1", NINE_AM, 60);
        Booking second = bookingFor("R1", NINE_AM.plusMinutes(60), 60);

        assertThat(first.clashesWith(second)).isFalse();
    }

    @Test
    void rejectsMissingOtherBooking() {
        Booking booking = bookingFor("R1", NINE_AM, 60);

        assertThatThrownBy(() -> booking.clashesWith(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Other booking is required");
    }

    // ---------- Helpers ----------

    private static Booking bookingFor(String roomId, LocalDateTime start, int minutes) {
        return new Booking("B-" + roomId + "-" + start, roomId, "student-1", slot(start, minutes));
    }

    private static TimeSlot slot(LocalDateTime start, int minutes) {
        return new TimeSlot(start, start.plusMinutes(minutes));
    }
}