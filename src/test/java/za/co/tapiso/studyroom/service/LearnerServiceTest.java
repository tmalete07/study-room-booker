package za.co.tapiso.studyroom.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import za.co.tapiso.studyroom.domain.Learner;
import za.co.tapiso.studyroom.domain.StudentNumber;
import za.co.tapiso.studyroom.repository.InMemoryLearnerRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LearnerServiceTest {

    private final LearnerService service = new LearnerService(new InMemoryLearnerRepository(List.of(
            new Learner(StudentNumber.parse("TSS4001-11"), "Palesa Ndlovu"),
            new Learner(StudentNumber.parse("TSS5001-12"), "Zanele Mthembu"))));

    // ---------- Successful login ----------

    @Test
    void logsInWithCorrectNameAndNumber() {
        Learner learner = service.login("Palesa Ndlovu", "TSS4001-11");

        assertThat(learner.studentNumber().value()).isEqualTo("TSS4001-11");
        assertThat(learner.grade()).isEqualTo(11);
    }

    @Test
    void ignoresCapitalsAndExtraSpacesInTheName() {
        Learner learner = service.login("  palesa   NDLOVU ", "TSS4001-11");

        assertThat(learner.name()).isEqualTo("Palesa Ndlovu");
    }

    @Test
    void acceptsLowerCaseStudentNumber() {
        Learner learner = service.login("Palesa Ndlovu", "tss4001-11");

        assertThat(learner.studentNumber().value()).isEqualTo("TSS4001-11");
    }

    // ---------- Failed login: same message either way ----------

    @Test
    void rejectsUnknownStudentNumber() {
        assertThatThrownBy(() -> service.login("Palesa Ndlovu", "TSS9999-11"))
                .isInstanceOf(LoginFailedException.class)
                .hasMessage("Student number or name is incorrect");
    }

    @Test
    void rejectsNameThatDoesNotMatchTheNumber() {
        assertThatThrownBy(() -> service.login("Zanele Mthembu", "TSS4001-11"))
                .isInstanceOf(LoginFailedException.class)
                .hasMessage("Student number or name is incorrect");
    }

    // ---------- Bad input ----------

    @Test
    void rejectsInvalidStudentNumberBeforeLookingItUp() {
        assertThatThrownBy(() -> service.login("Palesa Ndlovu", "TSS4001-13"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid student number. Grade must be between 8 and 12");
    }

    @ParameterizedTest(name = "name = \"{0}\"")
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void rejectsMissingName(String name) {
        assertThatThrownBy(() -> service.login(name, "TSS4001-11"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Name is required");
    }

    @Test
    void rejectsMissingRegister() {
        assertThatThrownBy(() -> new LearnerService(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Learner repository is required");
    }
}