package za.co.tapiso.studyroom.service;

import za.co.tapiso.studyroom.domain.Learner;
import za.co.tapiso.studyroom.domain.StudentNumber;
import za.co.tapiso.studyroom.repository.LearnerRepository;

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
     * Ignores capital letters, spaces at the ends, and extra spaces between words.
     */
    private static String normalise(String name) {
        return name.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}