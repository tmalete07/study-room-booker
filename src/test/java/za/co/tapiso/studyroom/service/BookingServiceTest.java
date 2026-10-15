package za.co.tapiso.studyroom.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.co.tapiso.studyroom.domain.Booking;
import za.co.tapiso.studyroom.domain.BookingPolicy;
import za.co.tapiso.studyroom.domain.TimeSlot;
import za.co.tapiso.studyroom.repository.BookingRepository;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    private static final ZoneId ZONE = ZoneId.of("Africa/Johannesburg");
    private static final LocalDateTime MONDAY_NINE_AM = LocalDateTime.of(2026, 10, 12, 9, 0);

    @Mock
    private BookingRepository repository;

    private BookingService service;

    @BeforeEach
    void setUp() {
        // "Now" is the day before MONDAY, so Monday bookings are in the future
        Clock dayBefore = Clock.fixed(
                LocalDateTime.of(2026, 10, 11, 12, 0).atZone(ZONE).toInstant(), ZONE);
        service = new BookingService(repository, new BookingPolicy(dayBefore));
    }

    // ---------- Happy path ----------

    @Test
    void savesValidBooking() {
        when(repository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Booking booking = service.book("R1", "student-1", slot(MONDAY_NINE_AM, 60));

        assertThat(booking.id()).isNotBlank();
        assertThat(booking.roomId()).isEqualTo("R1");
        assertThat(booking.studentId()).isEqualTo("student-1");
        verify(repository).save(booking);
    }

    @Test
    void givesEachBookingAUniqueId() {
        when(repository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Booking first = service.book("R1", "student-1", slot(MONDAY_NINE_AM, 60));
        Booking second = service.book("R2", "student-2", slot(MONDAY_NINE_AM, 60));

        assertThat(first.id()).isNotEqualTo(second.id());
    }

    // ---------- Rule: no overlap in the same room ----------

    @Test
    void rejectsOverlappingBookingInSameRoom() {
        when(repository.findByRoomId("R1"))
                .thenReturn(List.of(existing("B1", "R1", "student-2", MONDAY_NINE_AM, 60)));

        assertThatThrownBy(() -> service.book("R1", "student-1", slot(MONDAY_NINE_AM.plusMinutes(30), 60)))
                .isInstanceOf(BookingConflictException.class)
                .hasMessage("Room is already booked for that time");

        verify(repository, never()).save(any());
    }

    @Test
    void allowsBackToBackBookingInSameRoom() {
        when(repository.findByRoomId("R1"))
                .thenReturn(List.of(existing("B1", "R1", "student-2", MONDAY_NINE_AM, 60)));
        when(repository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Booking booking = service.book("R1", "student-1", slot(MONDAY_NINE_AM.plusHours(1), 60));

        verify(repository).save(booking);
    }

    // ---------- Rule: at most 2 bookings per student per day ----------

    @Test
    void rejectsThirdBookingOnSameDay() {
        when(repository.findByStudentIdOn("student-1", MONDAY_NINE_AM.toLocalDate()))
                .thenReturn(List.of(
                        existing("B1", "R1", "student-1", MONDAY_NINE_AM, 60),
                        existing("B2", "R2", "student-1", MONDAY_NINE_AM.plusHours(2), 60)));

        assertThatThrownBy(() -> service.book("R3", "student-1", slot(MONDAY_NINE_AM.plusHours(5), 60)))
                .isInstanceOf(BookingConflictException.class)
                .hasMessage("Students can make at most 2 bookings per day");

        verify(repository, never()).save(any());
    }

    @Test
    void allowsSecondBookingOnSameDay() {
        when(repository.findByStudentIdOn("student-1", MONDAY_NINE_AM.toLocalDate()))
                .thenReturn(List.of(existing("B1", "R1", "student-1", MONDAY_NINE_AM, 60)));
        when(repository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Booking booking = service.book("R2", "student-1", slot(MONDAY_NINE_AM.plusHours(3), 60));

        verify(repository).save(booking);
    }

    // ---------- Invalid requests never reach the repository ----------

    @Test
    void rejectsBookingInThePastWithoutTouchingRepository() {
        LocalDateTime lastSaturday = LocalDateTime.of(2026, 10, 10, 9, 0);

        assertThatThrownBy(() -> service.book("R1", "student-1", slot(lastSaturday, 60)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Bookings cannot start in the past");

        verifyNoInteractions(repository);
    }

    @Test
    void rejectsInvalidDurationWithoutTouchingRepository() {
        assertThatThrownBy(() -> service.book("R1", "student-1", slot(MONDAY_NINE_AM, 180)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Booking must be between 30 and 120 minutes");

        verifyNoInteractions(repository);
    }

    @Test
    void rejectsMissingDependencies() {
        assertThatThrownBy(() -> new BookingService(null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Repository and policy are required");
    }

    // ---------- Listing a student's bookings ----------

    @Test
    void listsOnlyThatStudentsBookingsEarliestFirst() {
        when(repository.findAll()).thenReturn(List.of(
                existing("LATE", "R1", "student-1", MONDAY_NINE_AM.plusHours(4), 60),
                existing("OTHER", "R2", "student-2", MONDAY_NINE_AM, 60),
                existing("EARLY", "R3", "student-1", MONDAY_NINE_AM, 60)));

        List<Booking> bookings = service.bookingsFor("student-1");

        assertThat(bookings)
                .extracting(Booking::id)
                .containsExactly("EARLY", "LATE");
    }

    @Test
    void rejectsListingWithoutStudentId() {
        assertThatThrownBy(() -> service.bookingsFor(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Student id is required");

        verifyNoInteractions(repository);
    }

    // ---------- Rule: only the owner can cancel ----------

    @Test
    void ownerCanCancelTheirBooking() {
        when(repository.findById("B1"))
                .thenReturn(Optional.of(existing("B1", "R1", "student-1", MONDAY_NINE_AM, 60)));

        service.cancel("B1", "student-1");

        verify(repository).deleteById("B1");
    }

    @Test
    void otherStudentCannotCancelTheBooking() {
        when(repository.findById("B1"))
                .thenReturn(Optional.of(existing("B1", "R1", "student-1", MONDAY_NINE_AM, 60)));

        assertThatThrownBy(() -> service.cancel("B1", "student-2"))
                .isInstanceOf(NotBookingOwnerException.class)
                .hasMessage("Only the student who made a booking can cancel it");

        verify(repository, never()).deleteById(any());
    }

    @Test
    void cancellingUnknownBookingIsReported() {
        // No stubbing needed: a Mockito mock returns Optional.empty() by default

        assertThatThrownBy(() -> service.cancel("B404", "student-1"))
                .isInstanceOf(BookingNotFoundException.class)
                .hasMessage("Booking not found: B404");

        verify(repository, never()).deleteById(any());
    }

    @Test
    void rejectsCancelWithoutStudentId() {
        assertThatThrownBy(() -> service.cancel("B1", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Student id is required");

        verifyNoInteractions(repository);
    }

    // ---------- Helpers ----------

    private static TimeSlot slot(LocalDateTime start, int minutes) {
        return new TimeSlot(start, start.plusMinutes(minutes));
    }

    private static Booking existing(String id, String roomId, String studentId,
                                    LocalDateTime start, int minutes) {
        return new Booking(id, roomId, studentId, slot(start, minutes));
    }
}