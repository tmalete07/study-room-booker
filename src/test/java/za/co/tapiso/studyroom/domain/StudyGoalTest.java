package za.co.tapiso.studyroom.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StudyGoalTest {

    @Test
    void trimsTitleAndDetail() {
        StudyGoal goal = new StudyGoal("  Pass Maths  ", "  Practise daily  ");

        assertThat(goal.title()).isEqualTo("Pass Maths");
        assertThat(goal.detail()).isEqualTo("Practise daily");
    }

    @Test
    void missingDetailBecomesEmpty() {
        StudyGoal goal = new StudyGoal("Pass Maths", null);

        assertThat(goal.detail()).isEmpty();
    }

    @ParameterizedTest(name = "title = \"{0}\"")
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void rejectsMissingTitle(String title) {
        assertThatThrownBy(() -> new StudyGoal(title, "detail"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Goal title is required");
    }

    @Test
    void acceptsTitleOfExactly80Characters() {
        assertThat(new StudyGoal("x".repeat(80), "").title()).hasSize(80);
    }

    @Test
    void rejectsTitleOf81Characters() {
        assertThatThrownBy(() -> new StudyGoal("x".repeat(81), ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Goal title must be 80 characters or fewer");
    }

    @Test
    void acceptsDetailOfExactly200Characters() {
        assertThat(new StudyGoal("Title", "x".repeat(200)).detail()).hasSize(200);
    }

    @Test
    void rejectsDetailOf201Characters() {
        assertThatThrownBy(() -> new StudyGoal("Title", "x".repeat(201)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Goal detail must be 200 characters or fewer");
    }
}