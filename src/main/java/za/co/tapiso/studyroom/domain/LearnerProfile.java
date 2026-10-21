package za.co.tapiso.studyroom.domain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * The extra details on a learner's profile page.
 * Class and school email are set by the admin office. Subjects, "about me" and study goals
 * are the learner's own, and the learner can edit them.
 */
public record LearnerProfile(String className, String email, List<String> subjects,
                             String about, List<StudyGoal> goals) {

    public static final int MAX_SUBJECTS = 10;
    public static final int MAX_SUBJECT_LENGTH = 40;
    public static final int MAX_ABOUT_LENGTH = 500;
    public static final int MAX_GOALS = 5;

    public LearnerProfile {
        className = className == null ? "" : className.trim();
        email = email == null ? "" : email.trim();
        about = about == null ? "" : about.trim();
        subjects = cleanSubjects(subjects);
        goals = cleanGoals(goals);

        if (about.length() > MAX_ABOUT_LENGTH) {
            throw new IllegalArgumentException(
                    "About me must be " + MAX_ABOUT_LENGTH + " characters or fewer");
        }
    }

    /**
     * A profile with nothing filled in yet.
     */
    public static LearnerProfile empty() {
        return new LearnerProfile("", "", List.of(), "", List.of());
    }

    /**
     * Returns a copy with the learner's own parts changed. Class and email stay as the admin office set them.
     */
    public LearnerProfile withLearnerEdits(List<String> newSubjects, String newAbout, List<StudyGoal> newGoals) {
        return new LearnerProfile(className, email, newSubjects, newAbout, newGoals);
    }

    private static List<String> cleanSubjects(List<String> subjects) {
        if (subjects == null) {
            return List.of();
        }
        if (subjects.size() > MAX_SUBJECTS) {
            throw new IllegalArgumentException("A learner can list at most " + MAX_SUBJECTS + " subjects");
        }

        List<String> cleaned = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (String subject : subjects) {
            String name = subject == null ? "" : subject.trim();
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Subject names cannot be blank");
            }
            if (name.length() > MAX_SUBJECT_LENGTH) {
                throw new IllegalArgumentException(
                        "Subject names must be " + MAX_SUBJECT_LENGTH + " characters or fewer");
            }
            if (!seen.add(name.toLowerCase(Locale.ROOT))) {
                throw new IllegalArgumentException("Each subject can only be listed once: " + name);
            }
            cleaned.add(name);
        }
        // An unchangeable copy, so changing the original list later cannot change this profile
        return List.copyOf(cleaned);
    }

    private static List<StudyGoal> cleanGoals(List<StudyGoal> goals) {
        if (goals == null) {
            return List.of();
        }
        if (goals.size() > MAX_GOALS) {
            throw new IllegalArgumentException("A learner can have at most " + MAX_GOALS + " study goals");
        }
        // A loop, not goals.contains(null): unchangeable lists like List.of() throw an error if asked about null
        for (StudyGoal goal : goals) {
            if (goal == null) {
                throw new IllegalArgumentException("Study goals cannot contain empty entries");
            }
        }
        return List.copyOf(goals);
    }
}