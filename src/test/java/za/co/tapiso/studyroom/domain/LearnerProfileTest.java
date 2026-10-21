package za.co.tapiso.studyroom.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LearnerProfileTest {

    // ---------- Clean-up of values ----------

    @Test
    void nullsBecomeEmptyValues() {
        LearnerProfile profile = new LearnerProfile(null, null, null, null, null);

        assertThat(profile.className()).isEmpty();
        assertThat(profile.email()).isEmpty();
        assertThat(profile.subjects()).isEmpty();
        assertThat(profile.about()).isEmpty();
        assertThat(profile.goals()).isEmpty();
    }

    @Test
    void emptyProfileHasNothingFilledIn() {
        LearnerProfile profile = LearnerProfile.empty();

        assertThat(profile.subjects()).isEmpty();
        assertThat(profile.goals()).isEmpty();
        assertThat(profile.about()).isEmpty();
    }

    @Test
    void trimsSpacesFromText() {
        LearnerProfile profile = new LearnerProfile(" 11B ", " palesa@school.example ",
                List.of("  Mathematics "), "  I like science.  ", List.of());

        assertThat(profile.className()).isEqualTo("11B");
        assertThat(profile.email()).isEqualTo("palesa@school.example");
        assertThat(profile.subjects()).containsExactly("Mathematics");
        assertThat(profile.about()).isEqualTo("I like science.");
    }

    // ---------- About me: up to 500 characters ----------

    @Test
    void acceptsAboutOfExactly500Characters() {
        assertThat(profileWithAbout("x".repeat(500)).about()).hasSize(500);
    }

    @Test
    void rejectsAboutOf501Characters() {
        assertThatThrownBy(() -> profileWithAbout("x".repeat(501)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("About me must be 500 characters or fewer");
    }

    // ---------- Subjects ----------

    @Test
    void acceptsExactly10Subjects() {
        assertThat(profileWithSubjects(subjects(10)).subjects()).hasSize(10);
    }

    @Test
    void rejects11Subjects() {
        assertThatThrownBy(() -> profileWithSubjects(subjects(11)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A learner can list at most 10 subjects");
    }

    @Test
    void acceptsSubjectNameOfExactly40Characters() {
        assertThat(profileWithSubjects(List.of("x".repeat(40))).subjects().get(0)).hasSize(40);
    }

    @Test
    void rejectsSubjectNameOf41Characters() {
        assertThatThrownBy(() -> profileWithSubjects(List.of("x".repeat(41))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Subject names must be 40 characters or fewer");
    }

    @ParameterizedTest(name = "subject = \"{0}\"")
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void rejectsBlankSubjectName(String subject) {
        // Arrays.asList allows a null entry, which List.of would not
        assertThatThrownBy(() -> profileWithSubjects(Arrays.asList("Mathematics", subject)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Subject names cannot be blank");
    }

    @Test
    void rejectsTheSameSubjectTwiceIgnoringCapitalsAndSpaces() {
        assertThatThrownBy(() -> profileWithSubjects(List.of("Mathematics", " mathematics ")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Each subject can only be listed once: mathematics");
    }

    // ---------- Study goals ----------

    @Test
    void acceptsExactly5Goals() {
        assertThat(profileWithGoals(goals(5)).goals()).hasSize(5);
    }

    @Test
    void rejects6Goals() {
        assertThatThrownBy(() -> profileWithGoals(goals(6)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A learner can have at most 5 study goals");
    }

    @Test
    void rejectsEmptyGoalEntry() {
        assertThatThrownBy(() -> profileWithGoals(Arrays.asList(new StudyGoal("Pass Maths", ""), null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Study goals cannot contain empty entries");
    }

    // ---------- Lists are protected ----------

    @Test
    void changingTheOriginalListDoesNotChangeTheProfile() {
        List<String> original = new ArrayList<>(List.of("Mathematics"));
        LearnerProfile profile = profileWithSubjects(original);

        original.add("Art");

        assertThat(profile.subjects()).containsExactly("Mathematics");
    }

    @Test
    void profileListsCannotBeChangedFromOutside() {
        LearnerProfile profile = profileWithSubjects(List.of("Mathematics"));

        assertThatThrownBy(() -> profile.subjects().add("Art"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    // ---------- Learner edits ----------

    @Test
    void learnerEditsKeepClassAndEmailFromTheAdminOffice() {
        LearnerProfile original = new LearnerProfile("11B", "palesa@school.example",
                List.of("Mathematics"), "Old text", List.of());

        LearnerProfile edited = original.withLearnerEdits(
                List.of("Art"), "New text", List.of(new StudyGoal("Pass Art", "")));

        assertThat(edited.className()).isEqualTo("11B");
        assertThat(edited.email()).isEqualTo("palesa@school.example");
        assertThat(edited.subjects()).containsExactly("Art");
        assertThat(edited.about()).isEqualTo("New text");
        assertThat(edited.goals()).extracting(StudyGoal::title).containsExactly("Pass Art");
    }

    // ---------- Helpers ----------

    private static LearnerProfile profileWithAbout(String about) {
        return new LearnerProfile("11B", "", List.of(), about, List.of());
    }

    private static LearnerProfile profileWithSubjects(List<String> subjects) {
        return new LearnerProfile("11B", "", subjects, "", List.of());
    }

    private static LearnerProfile profileWithGoals(List<StudyGoal> goals) {
        return new LearnerProfile("11B", "", List.of(), "", goals);
    }

    private static List<String> subjects(int count) {
        List<String> subjects = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            subjects.add("Subject " + i);
        }
        return subjects;
    }

    private static List<StudyGoal> goals(int count) {
        List<StudyGoal> goals = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            goals.add(new StudyGoal("Goal " + i, ""));
        }
        return goals;
    }
}