package za.co.tapiso.studyroom.service;

import za.co.tapiso.studyroom.domain.Learner;
import za.co.tapiso.studyroom.domain.LearnerProfile;
import za.co.tapiso.studyroom.domain.StudentNumber;
import za.co.tapiso.studyroom.domain.StudyGoal;
import za.co.tapiso.studyroom.repository.LearnerRepository;

import java.util.List;
import java.util.Locale;

/**
 * Logs learners in by checking their name and student number against the admin office register.
 */
public class LearnerService {

    // The same message for an unknown number and a wrong name,
    // so nobody can use the login form to find out which student numbers exist
    public static final String LOGIN_FAILED = "Student number or name is incorrect";

    private final LearnerRepository learners;

    public LearnerService(LearnerRepository learners) {
        if (learners == null) {
            throw new IllegalArgumentException("Learner repository is required");
        }
        this.learners = learners;
    }

    public Learner login(String name, String studentNumberText) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }
        StudentNumber studentNumber = StudentNumber.parse(studentNumberText);

        Learner learner = learners.findByStudentNumber(studentNumber)
                .orElseThrow(() -> new LoginFailedException(LOGIN_FAILED));

        if (!normalise(learner.name()).equals(normalise(name))) {
            throw new LoginFailedException(LOGIN_FAILED);
        }
        return learner;
    }

    /**
     * Returns the learner and their profile.
     */
    public ProfileDetails profileOf(String studentNumberText) {
        Learner learner = registeredLearner(StudentNumber.parse(studentNumberText));
        return new ProfileDetails(learner, learners.findProfile(learner.studentNumber()));
    }

    /**
     * Changes a learner's subjects, "about me" and study goals.
     * Only the learner themselves may do this; class and email stay as the admin office set them.
     */
    public ProfileDetails updateProfile(String studentNumberText, String requestedByText,
                                        List<String> subjects, String about, List<StudyGoal> goals) {
        StudentNumber owner = StudentNumber.parse(studentNumberText);
        StudentNumber requestedBy = StudentNumber.parse(requestedByText);
        Learner learner = registeredLearner(owner);

        if (!owner.equals(requestedBy)) {
            throw new NotProfileOwnerException("You can only edit your own profile");
        }

        LearnerProfile updated = learners.findProfile(owner).withLearnerEdits(subjects, about, goals);
        learners.saveProfile(owner, updated);
        return new ProfileDetails(learner, updated);
    }

    private Learner registeredLearner(StudentNumber studentNumber) {
        return learners.findByStudentNumber(studentNumber)
                .orElseThrow(() -> new LearnerNotFoundException("Learner not found: " + studentNumber));
    }

    /**
     * Ignores capital letters, spaces at the ends, and extra spaces between words.
     */
    private static String normalise(String name) {
        return name.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}