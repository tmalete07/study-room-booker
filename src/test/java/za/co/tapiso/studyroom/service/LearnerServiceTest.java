package za.co.tapiso.studyroom.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import za.co.tapiso.studyroom.domain.Learner;
import za.co.tapiso.studyroom.domain.LearnerProfile;
import za.co.tapiso.studyroom.domain.StudentNumber;
import za.co.tapiso.studyroom.domain.StudyGoal;
import za.co.tapiso.studyroom.repository.InMemoryLearnerRepository;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LearnerServiceTest {

    private static final StudentNumber PALESA = StudentNumber.parse("TSS4001-11");

    private InMemoryLearnerRepository register;
    private LearnerService service;

    @BeforeEach
    void setUp() {
        register = new InMemoryLearnerRepository(List.of(
                new Learner(PALESA, "Palesa Ndlovu"),
                new Learner(StudentNumber.parse("TSS5001-12"), "Zanele Mthembu")));
        register.saveProfile(PALESA, new LearnerProfile("11B", "palesa.ndlovu@learners.tswelopele.example",
                List.of("Mathematics", "Physical Sciences"), "I want to study medicine.",
                List.of(new StudyGoal("Improve Physical Sciences", "Study twice a week."))));
        service = new LearnerService(register);
    }

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

    // ---------- Viewing a profile ----------

    @Test
    void returnsLearnerAndProfile() {
        ProfileDetails details = service.profileOf("tss4001-11");

        assertThat(details.learner().name()).isEqualTo("Palesa Ndlovu");
        assertThat(details.profile().className()).isEqualTo("11B");
        assertThat(details.profile().subjects()).containsExactly("Mathematics", "Physical Sciences");
    }

    @Test
    void viewingUnknownLearnerIsReported() {
        assertThatThrownBy(() -> service.profileOf("TSS9999-11"))
                .isInstanceOf(LearnerNotFoundException.class)
                .hasMessage("Learner not found: TSS9999-11");
    }

    // ---------- Editing a profile ----------

    @Test
    void learnerCanEditTheirOwnProfile() {
        ProfileDetails updated = service.updateProfile("TSS4001-11", "tss4001-11",
                List.of("Mathematics", "Life Sciences"), "I want to be a doctor.",
                List.of(new StudyGoal("Pass Life Sciences", "")));

        assertThat(updated.profile().subjects()).containsExactly("Mathematics", "Life Sciences");
        assertThat(register.findProfile(PALESA).about()).isEqualTo("I want to be a doctor.");
    }

    @Test
    void editingKeepsClassAndEmail() {
        service.updateProfile("TSS4001-11", "TSS4001-11", List.of("Art"), "", List.of());

        assertThat(register.findProfile(PALESA).className()).isEqualTo("11B");
        assertThat(register.findProfile(PALESA).email()).isEqualTo("palesa.ndlovu@learners.tswelopele.example");
    }

    @Test
    void learnerCannotEditSomeoneElsesProfile() {
        assertThatThrownBy(() -> service.updateProfile("TSS4001-11", "TSS5001-12",
                List.of("Art"), "Changed by someone else", List.of()))
                .isInstanceOf(NotProfileOwnerException.class)
                .hasMessage("You can only edit your own profile");

        assertThat(register.findProfile(PALESA).about()).isEqualTo("I want to study medicine.");
    }

    @Test
    void editingWithoutSayingWhoIsAskingIsRejected() {
        assertThatThrownBy(() -> service.updateProfile("TSS4001-11", null, List.of("Art"), "", List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Student number is required");
    }

    @Test
    void invalidEditIsRejectedAndOldProfileIsKept() {
        List<String> elevenSubjects = new ArrayList<>();
        for (int i = 1; i <= 11; i++) {
            elevenSubjects.add("Subject " + i);
        }

        assertThatThrownBy(() -> service.updateProfile("TSS4001-11", "TSS4001-11",
                elevenSubjects, "", List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A learner can list at most 10 subjects");

        assertThat(register.findProfile(PALESA).subjects()).containsExactly("Mathematics", "Physical Sciences");
    }

    @Test
    void editingUnknownLearnerIsReported() {
        assertThatThrownBy(() -> service.updateProfile("TSS9999-11", "TSS9999-11", List.of(), "", List.of()))
                .isInstanceOf(LearnerNotFoundException.class)
                .hasMessage("Learner not found: TSS9999-11");
    }
}