package za.co.tapiso.studyroom.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
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

    // ---------- Shared rooms ----------

    @Test
    void sharedRoomHasNoGrade() {
        Room room = new Room("GROUP", "Group Room", 8);

        assertThat(room.grade()).isNull();
    }

    @ParameterizedTest(name = "a Grade {0} learner can book a shared room")
    @ValueSource(strings = {"TSS1001-8", "TSS2001-9", "TSS3001-10", "TSS4001-11", "TSS5001-12"})
    void sharedRoomIsOpenToEveryGrade(String studentNumber) {
        Room room = new Room("GROUP", "Group Room", 8);

        assertThat(room.isOpenTo(StudentNumber.parse(studentNumber))).isTrue();
    }

    // ---------- Grade rooms ----------

    @ParameterizedTest(name = "a room can belong to Grade {0}")
    @ValueSource(ints = {8, 12})
    void acceptsRoomGradesFromEightToTwelve(int grade) {
        Room room = new Room("Q" + grade, "Grade " + grade + " Quiet Room", 4, grade);

        assertThat(room.grade()).isEqualTo(grade);
    }

    @ParameterizedTest(name = "a room cannot belong to Grade {0}")
    @ValueSource(ints = {7, 13})
    void rejectsRoomGradesOutsideEightToTwelve(int grade) {
        assertThatThrownBy(() -> new Room("Q" + grade, "Grade " + grade + " Quiet Room", 4, grade))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Room grade must be between 8 and 12");
    }

    @Test
    void gradeRoomIsOpenToItsOwnGrade() {
        Room room = new Room("Q11", "Grade 11 Quiet Room", 4, 11);

        assertThatCode(() -> room.checkOpenTo(StudentNumber.parse("TSS4001-11")))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest(name = "{0} cannot book the Grade 11 Quiet Room")
    @ValueSource(strings = {"TSS1001-8", "TSS2001-9", "TSS3001-10", "TSS5001-12"})
    void gradeRoomRejectsEveryOtherGrade(String studentNumber) {
        Room room = new Room("Q11", "Grade 11 Quiet Room", 4, 11);

        assertThatThrownBy(() -> room.checkOpenTo(StudentNumber.parse(studentNumber)))
                .isInstanceOf(RoomNotForGradeException.class)
                .hasMessage("The Grade 11 Quiet Room is for Grade 11 learners only");
    }

    @Test
    void rejectsMissingStudentNumber() {
        Room room = new Room("Q11", "Grade 11 Quiet Room", 4, 11);

        assertThatThrownBy(() -> room.isOpenTo(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Student number is required");
    }
}