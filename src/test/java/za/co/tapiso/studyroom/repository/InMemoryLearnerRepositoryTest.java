package za.co.tapiso.studyroom.repository;

import org.junit.jupiter.api.Test;
import za.co.tapiso.studyroom.domain.Learner;
import za.co.tapiso.studyroom.domain.StudentNumber;

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
}