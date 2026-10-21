package za.co.tapiso.studyroom.repository;

import za.co.tapiso.studyroom.domain.Learner;
import za.co.tapiso.studyroom.domain.LearnerProfile;
import za.co.tapiso.studyroom.domain.StudentNumber;
import za.co.tapiso.studyroom.domain.StudyGoal;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Keeps the learner register in memory. The app starts with sample learners in every grade;
 * tests can pass their own.
 */
public class InMemoryLearnerRepository implements LearnerRepository {

    private static final String EMAIL_DOMAIN = "@learners.tswelopele.example";

    private final Map<StudentNumber, Learner> learners = new LinkedHashMap<>();

    // Profiles can be edited while the app runs, so this map is safe to use from many requests at once
    private final Map<StudentNumber, LearnerProfile> profiles = new ConcurrentHashMap<>();

    public InMemoryLearnerRepository(List<Learner> learners) {
        if (learners == null) {
            throw new IllegalArgumentException("Learners are required");
        }
        learners.forEach(learner -> this.learners.put(learner.studentNumber(), learner));
    }

    public static InMemoryLearnerRepository withSampleLearners() {
        InMemoryLearnerRepository register = new InMemoryLearnerRepository(List.of(
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

        List<String> seniorPhase = List.of("Mathematics", "Natural Sciences", "English FAL",
                "Sesotho HL", "Social Sciences", "Technology", "Life Orientation");

        register.sample("TSS1001-8", "8A", "thabo.mokoena", seniorPhase,
                "I love building things and playing soccer after school.",
                goal("Finish my Technology project early", "Work on it in the Group Room every Tuesday."));
        register.sample("TSS1002-8", "8B", "naledi.dlamini", seniorPhase,
                "I enjoy reading and I want to join the debating team.",
                goal("Read one book a month", "Use the Library Pod during first break."));
        register.sample("TSS2001-9", "9A", "sipho.nkosi", seniorPhase,
                "Science is my favourite subject, especially experiments.",
                goal("Get 70% in Natural Sciences", "Revise each chapter the week it is taught."));
        register.sample("TSS2002-9", "9C", "ayanda.khumalo", seniorPhase,
                "I like drawing and I am learning to code at home.",
                goal("Choose my Grade 10 subjects", "Talk to my teachers before the end of term."));
        register.sample("TSS3001-10", "10B", "lerato.molefe",
                List.of("Mathematics", "Physical Sciences", "Life Sciences", "Geography",
                        "English FAL", "Sesotho HL", "Life Orientation"),
                "I enjoy science, reading and playing netball. I learn best when I work through examples with friends.",
                goal("Build my confidence in Mathematics", "Practise for 30 minutes, four afternoons a week."),
                goal("Make time for focused study", "Book a quiet room once a week to revise Physical Sciences."));
        register.sample("TSS3002-10", "10A", "kagiso.mahlangu",
                List.of("Mathematics", "Accounting", "Business Studies", "Economics",
                        "English FAL", "isiZulu HL", "Life Orientation"),
                "I want to start my own business one day.",
                goal("Understand the accounting equation", "Do two extra exercises after every lesson."));
        register.sample("TSS1234-11", "11C", "karabo.mabena",
                List.of("Mathematics", "Physical Sciences", "Computer Applications Technology",
                        "English FAL", "Sesotho HL", "Life Orientation", "Geography"),
                "I am interested in computers and how websites work.",
                goal("Pass my CAT practical with 80%", "Practise spreadsheets every Thursday in the Computer Lab."));
        register.sample("TSS4001-11", "11B", "palesa.ndlovu",
                List.of("Mathematics", "Physical Sciences", "Life Sciences", "Geography",
                        "English FAL", "Sesotho HL", "Life Orientation"),
                "I want to study medicine after matric. I like studying with a small group.",
                goal("Improve my Physical Sciences mark", "Book the Grade 11 Quiet Room twice a week."),
                goal("Stay on top of Life Sciences", "Make summary notes every Friday."));
        register.sample("TSS4002-11", "11A", "tumelo.sithole",
                List.of("Mathematical Literacy", "History", "Tourism", "Business Studies",
                        "English FAL", "Setswana HL", "Life Orientation"),
                "I enjoy history and learning about different places.",
                goal("Plan my Tourism project", "Finish the research before the June holidays."));
        register.sample("TSS5001-12", "12A", "zanele.mthembu",
                List.of("Mathematics", "Physical Sciences", "Life Sciences", "Geography",
                        "English FAL", "isiZulu HL", "Life Orientation"),
                "Matric year! I am aiming for a university bursary in engineering.",
                goal("Get a distinction in Mathematics", "Do one past paper every weekend."),
                goal("Finish my bursary applications", "Submit all forms before the end of the month."));
        register.sample("TSS5002-12", "12B", "bongani.zulu",
                List.of("Mathematics", "Accounting", "Business Studies", "Economics",
                        "English FAL", "isiZulu HL", "Life Orientation"),
                "I am the class representative and I enjoy helping others study.",
                goal("Lead a weekly study group", "Book the Group Room every Wednesday afternoon."));

        return register;
    }

    @Override
    public Optional<Learner> findByStudentNumber(StudentNumber studentNumber) {
        return Optional.ofNullable(learners.get(studentNumber));
    }

    @Override
    public List<Learner> findAll() {
        return List.copyOf(learners.values());
    }

    @Override
    public LearnerProfile findProfile(StudentNumber studentNumber) {
        return profiles.getOrDefault(studentNumber, LearnerProfile.empty());
    }

    @Override
    public void saveProfile(StudentNumber studentNumber, LearnerProfile profile) {
        if (studentNumber == null || profile == null) {
            throw new IllegalArgumentException("Student number and profile are required");
        }
        profiles.put(studentNumber, profile);
    }

    private void sample(String studentNumber, String className, String emailName,
                        List<String> subjects, String about, StudyGoal... goals) {
        saveProfile(StudentNumber.parse(studentNumber),
                new LearnerProfile(className, emailName + EMAIL_DOMAIN, subjects, about, List.of(goals)));
    }

    private static Learner learner(String studentNumber, String name) {
        return new Learner(StudentNumber.parse(studentNumber), name);
    }

    private static StudyGoal goal(String title, String detail) {
        return new StudyGoal(title, detail);
    }
}