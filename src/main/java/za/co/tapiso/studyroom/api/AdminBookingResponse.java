package za.co.tapiso.studyroom.api;

import za.co.tapiso.studyroom.domain.Booking;
import za.co.tapiso.studyroom.domain.Learner;
import za.co.tapiso.studyroom.domain.Room;
import za.co.tapiso.studyroom.domain.StudentNumber;
import za.co.tapiso.studyroom.repository.LearnerRepository;
import za.co.tapiso.studyroom.repository.RoomRepository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * One booking as the admin office sees it: who booked which room, and when.
 * A learner or room that can no longer be found is shown as "Unknown" instead of failing the whole list.
 */
public record AdminBookingResponse(String id, String roomId, String roomName,
                                   String studentNumber, String learnerName, Integer grade,
                                   LocalDateTime start, LocalDateTime end) {

    public static AdminBookingResponse from(Booking booking, RoomRepository rooms, LearnerRepository learners) {
        String roomName = rooms.findById(booking.roomId())
                .map(Room::name)
                .orElse("Unknown room (" + booking.roomId() + ")");

        Optional<Learner> learner = findLearner(learners, booking.studentId());

        return new AdminBookingResponse(
                booking.id(),
                booking.roomId(),
                roomName,
                booking.studentId(),
                learner.map(Learner::name).orElse("Unknown learner"),
                learner.map(Learner::grade).orElse(null),
                booking.slot().start(),
                booking.slot().end());
    }

    private static Optional<Learner> findLearner(LearnerRepository learners, String studentId) {
        try {
            return learners.findByStudentNumber(StudentNumber.parse(studentId));
        } catch (IllegalArgumentException e) {
            // An old booking saved before student numbers were checked
            return Optional.empty();
        }
    }
}