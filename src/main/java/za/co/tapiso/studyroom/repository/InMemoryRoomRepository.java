package za.co.tapiso.studyroom.repository;

import za.co.tapiso.studyroom.domain.Room;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Keeps rooms in memory. The app starts with a fixed set of rooms; tests can pass their own.
 */
public class InMemoryRoomRepository implements RoomRepository {

    private final Map<String, Room> rooms = new LinkedHashMap<>();

    public InMemoryRoomRepository(List<Room> rooms) {
        if (rooms == null) {
            throw new IllegalArgumentException("Rooms are required");
        }
        rooms.forEach(room -> this.rooms.put(room.id(), room));
    }

    public static InMemoryRoomRepository withDefaultRooms() {
        return new InMemoryRoomRepository(List.of(
                new Room("R1", "Quiet Room", 4),
                new Room("R2", "Group Room", 8),
                new Room("R3", "Solo Pod", 1)));
    }

    @Override
    public List<Room> findAll() {
        return List.copyOf(rooms.values());
    }

    @Override
    public Optional<Room> findById(String id) {
        return Optional.ofNullable(rooms.get(id));
    }
}