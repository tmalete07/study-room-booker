package za.co.tapiso.studyroom.domain;

/**
 * Something a learner is working towards, for example
 * "Build my confidence in Mathematics" with a short plan underneath.
 */
public record StudyGoal(String title, String detail) {

    public static final int MAX_TITLE_LENGTH = 80;
    public static final int MAX_DETAIL_LENGTH = 200;

    public StudyGoal {
        title = title == null ? "" : title.trim();
        detail = detail == null ? "" : detail.trim();

        if (title.isEmpty()) {
            throw new IllegalArgumentException("Goal title is required");
        }
        if (title.length() > MAX_TITLE_LENGTH) {
            throw new IllegalArgumentException(
                    "Goal title must be " + MAX_TITLE_LENGTH + " characters or fewer");
        }
        if (detail.length() > MAX_DETAIL_LENGTH) {
            throw new IllegalArgumentException(
                    "Goal detail must be " + MAX_DETAIL_LENGTH + " characters or fewer");
        }
    }
}