package za.co.tapiso.studyroom.repository;

import za.co.tapiso.studyroom.domain.Learner;
import za.co.tapiso.studyroom.domain.LearnerProfile;
import za.co.tapiso.studyroom.domain.StudentNumber;

import java.util.List;
import java.util.Optional;

/**
 * The admin office register of learners.
 */
public interface LearnerRepository {

    Optional<Learner> findByStudentNumber(StudentNumber studentNumber);

    List<Learner> findAll();

    /**
     * Returns the learner's profile, or an empty profile if nothing has been saved for them yet.
     */
    LearnerProfile findProfile(StudentNumber studentNumber);

    void saveProfile(StudentNumber studentNumber, LearnerProfile profile);
}