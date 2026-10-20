package za.co.tapiso.studyroom.repository;

import za.co.tapiso.studyroom.domain.Learner;
import za.co.tapiso.studyroom.domain.StudentNumber;

import java.util.List;
import java.util.Optional;

/**
 * The admin office register of learners.
 */
public interface LearnerRepository {

    Optional<Learner> findByStudentNumber(StudentNumber studentNumber);

    List<Learner> findAll();
}