package za.co.tapiso.studyroom.repository;

import za.co.tapiso.studyroom.domain.Learner;
import za.co.tapiso.studyroom.domain.StudentNumber;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Keeps the learner register in memory. The app starts with sample learners in every grade;
 * tests can pass their own.
 */
public class InMemoryLearnerRepository implements LearnerRepository {

    private final Map<StudentNumber, Learner> learners = new LinkedHashMap<>();

    public InMemoryLearnerRepository(List<Learner> learners) {
        if (learners == null) {
            throw new IllegalArgumentException("Learners are required");
        }
        learners.forEach(learner -> this.learners.put(learner.studentNumber(), learner));
    }

    public static InMemoryLearnerRepository withSampleLearners() {
        return new InMemoryLearnerRepository(List.of(
                learner("TSS1001-8", "Thabo Mokoena"),
                learner("TSS1002-8", "Naledi Dlamini"),
                learner("TSS2001-9", "Sipho Nkosi"),
                learner("TSS2002-9", "Ayanda Khumalo"),
                learner("TSS3001-10", "Lerato Molefe"),
                learner("TSS3002-10", "Kagiso Mahlangu"),
                learner("TSS1234-11", "Karabo Mabena"),
                learner("TSS4001-11", "Palesa Ndlovu"),
                learner("TSS4002-11", "Tumelo Sithole"),
                learner("TSS5001-12", "Zanele Mthembu"),
                learner("TSS5002-12", "Bongani Zulu")));
    }

    @Override
    public Optional<Learner> findByStudentNumber(StudentNumber studentNumber) {
        return Optional.ofNullable(learners.get(studentNumber));
    }

    @Override
    public List<Learner> findAll() {
        return List.copyOf(learners.values());
    }

    private static Learner learner(String studentNumber, String name) {
        return new Learner(StudentNumber.parse(studentNumber), name);
    }
}