package za.co.tapiso.studyroom.api;

import java.time.LocalDateTime;

/**
 * The JSON a client sends to create a booking, for example:
 * {"roomId": "R1", "studentId": "student-1", "start": "2026-10-12T09:00", "end": "2026-10-12T10:00"}
 */
public record CreateBookingRequest(String roomId, String studentId, LocalDateTime start, LocalDateTime end) {
}