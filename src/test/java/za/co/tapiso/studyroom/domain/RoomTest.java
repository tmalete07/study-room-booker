package za.co.tapiso.studyroom.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RoomTest {

    @Test
    void createsValidRoom() {
        Room room = new Room("R1", "Quiet Room", 4);

        assertThat(room.id()).isEqualTo("R1");
        assertThat(room.name()).isEqualTo("Quiet Room");
        assertThat(room.capacity()).isEqualTo(4);
    }

    @ParameterizedTest(name = "id = \"{0}\"")
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void rejectsMissingId(String id) {
        assertThatThrownBy(() -> new Room(id, "Quiet Room", 4))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Room id is required");
    }

    @ParameterizedTest(name = "name = \"{0}\"")
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void rejectsMissingName(String name) {
        assertThatThrownBy(() -> new Room("R1", name, 4))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Room name is required");
    }

    @ParameterizedTest(name = "capacity = {0}")
    @ValueSource(ints = {0, -1, -10})
    void rejectsCapacityBelowOne(int capacity) {
        assertThatThrownBy(() -> new Room("R1", "Quiet Room", capacity))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Capacity must be at least 1");
    }

    @Test
    void acceptsCapacityOfExactlyOne() {
        Room room = new Room("R1", "Solo Pod", 1);

        assertThat(room.capacity()).isEqualTo(1);
    }
}