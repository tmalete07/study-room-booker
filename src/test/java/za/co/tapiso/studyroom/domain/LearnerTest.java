package za.co.tapiso.studyroom.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LearnerTest {

    @Test
    void takesGradeFromStudentNumber() {
        Learner learner = new Learner(StudentNumber.parse("TSS4001-11"), "Palesa Ndlovu");

        assertThat(learner.grade()).isEqualTo(11);
        assertThat(learner.name()).isEqualTo("Palesa Ndlovu");
    }

    @Test
    void rejectsMissingStudentNumber() {
        assertThatThrownBy(() -> new Learner(null, "Palesa Ndlovu"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Student number is required");
    }

    @ParameterizedTest(name = "name = \"{0}\"")
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void rejectsMissingName(String name) {
        assertThatThrownBy(() -> new Learner(StudentNumber.parse("TSS4001-11"), name))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Name is required");
    }
}