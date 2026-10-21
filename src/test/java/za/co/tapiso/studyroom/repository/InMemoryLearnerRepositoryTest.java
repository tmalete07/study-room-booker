package za.co.tapiso.studyroom.repository;

import org.junit.jupiter.api.Test;
import za.co.tapiso.studyroom.domain.Learner;
import za.co.tapiso.studyroom.domain.LearnerProfile;
import za.co.tapiso.studyroom.domain.StudentNumber;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InMemoryLearnerRepositoryTest {

    private final LearnerRepository register = InMemoryLearnerRepository.withSampleLearners();

    @Test
    void sampleRegisterHasLearnersInEveryGrade() {
        assertThat(register.findAll())
                .extracting(Learner::grade)
                .contains(8, 9, 10, 11, 12);
    }

    @Test
    void findsLearnerByStudentNumber() {
        assertThat(register.findByStudentNumber(StudentNumber.parse("TSS4001-11")))
                .get()
                .extracting(Learner::name)
                .isEqualTo("Palesa Ndlovu");
    }

    @Test
    void findsLearnerWhateverCaseTheNumberWasTypedIn() {
        assertThat(register.findByStudentNumber(StudentNumber.parse("tss4001-11"))).isPresent();
    }

    @Test
    void returnsEmptyForUnknownStudentNumber() {
        assertThat(register.findByStudentNumber(StudentNumber.parse("TSS9999-11"))).isEmpty();
    }

    @Test
    void rejectsMissingLearnerList() {
        assertThatThrownBy(() -> new InMemoryLearnerRepository(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Learners are required");
    }

    // ---------- Profiles ----------

    @Test
    void everySampleLearnerHasAProfileForTheirGrade() {
        for (Learner learner : register.findAll()) {
            LearnerProfile profile = register.findProfile(learner.studentNumber());

            assertThat(profile.className())
                    .as("class of %s", learner.name())
                    .startsWith(String.valueOf(learner.grade()));
            assertThat(profile.email()).as("email of %s", learner.name()).endsWith(".example");
            assertThat(profile.subjects()).as("subjects of %s", learner.name()).isNotEmpty();
            assertThat(profile.goals()).as("goals of %s", learner.name()).isNotEmpty();
        }
    }

    @Test
    void learnerWithoutASavedProfileGetsAnEmptyOne() {
        LearnerRepository emptyRegister = new InMemoryLearnerRepository(List.of());

        assertThat(emptyRegister.findProfile(StudentNumber.parse("TSS4001-11")))
                .isEqualTo(LearnerProfile.empty());
    }

    @Test
    void savingAProfileReplacesTheOldOne() {
        StudentNumber palesa = StudentNumber.parse("TSS4001-11");
        LearnerProfile updated = register.findProfile(palesa)
                .withLearnerEdits(List.of("Art"), "New about me", List.of());

        register.saveProfile(palesa, updated);

        assertThat(register.findProfile(palesa).subjects()).containsExactly("Art");
        assertThat(register.findProfile(palesa).about()).isEqualTo("New about me");
    }

    @Test
    void rejectsSavingWithoutAProfile() {
        assertThatThrownBy(() -> register.saveProfile(StudentNumber.parse("TSS4001-11"), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Student number and profile are required");
    }
}