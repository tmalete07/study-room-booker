package za.co.tapiso.studyroom.repository;

import za.co.tapiso.studyroom.domain.Room;

import java.util.List;
import java.util.Optional;

/**
 * Finds the study rooms that can be booked.
 */
public interface RoomRepository {

    List<Room> findAll();

    Optional<Room> findById(String id);
}