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

    /**
     * Tswelopele Secondary School's rooms: one quiet room for each grade, plus rooms every grade can share.
     */
    public static InMemoryRoomRepository withDefaultRooms() {
        return new InMemoryRoomRepository(List.of(
                new Room("Q8", "Grade 8 Quiet Room", 4, 8),
                new Room("Q9", "Grade 9 Quiet Room", 4, 9),
                new Room("Q10", "Grade 10 Quiet Room", 4, 10),
                new Room("Q11", "Grade 11 Quiet Room", 4, 11),
                new Room("Q12", "Grade 12 Quiet Room", 4, 12),
                new Room("GROUP", "Group Room", 8),
                new Room("LIBRARY", "Library Pod", 1),
                new Room("LAB", "Computer Lab", 20)));
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