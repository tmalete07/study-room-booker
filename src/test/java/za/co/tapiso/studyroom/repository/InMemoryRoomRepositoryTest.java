package za.co.tapiso.studyroom.repository;

import org.junit.jupiter.api.Test;
import za.co.tapiso.studyroom.domain.Room;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InMemoryRoomRepositoryTest {

    @Test
    void defaultRoomsAreAvailable() {
        RoomRepository repository = InMemoryRoomRepository.withDefaultRooms();

        assertThat(repository.findAll())
                .extracting(Room::id)
                .containsExactly("R1", "R2", "R3");
    }

    @Test
    void findsRoomById() {
        Room quiet = new Room("R1", "Quiet Room", 4);
        RoomRepository repository = new InMemoryRoomRepository(List.of(quiet));

        assertThat(repository.findById("R1")).contains(quiet);
    }

    @Test
    void returnsEmptyWhenRoomIsNotFound() {
        RoomRepository repository = new InMemoryRoomRepository(List.of());

        assertThat(repository.findById("R99")).isEmpty();
    }

    @Test
    void rejectsMissingRoomList() {
        assertThatThrownBy(() -> new InMemoryRoomRepository(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Rooms are required");
    }
}