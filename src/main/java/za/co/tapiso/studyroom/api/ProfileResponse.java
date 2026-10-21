package za.co.tapiso.studyroom.api;

import za.co.tapiso.studyroom.domain.LearnerProfile;
import za.co.tapiso.studyroom.domain.StudyGoal;
import za.co.tapiso.studyroom.service.ProfileDetails;

import java.util.List;

/**
 * The JSON for a learner's profile page.
 */
public record ProfileResponse(String studentNumber, String name, String firstName, String surname,
                              int grade, String className, String email,
                              List<String> subjects, String about, List<StudyGoal> goals) {

    public static ProfileResponse from(ProfileDetails details) {
        String name = details.learner().name().trim();
        // "Palesa Ndlovu" -> first name "Palesa", surname "Ndlovu"
        String[] parts = name.split("\\s+", 2);
        LearnerProfile profile = details.profile();

        return new ProfileResponse(
                details.learner().studentNumber().value(),
                name,
                parts[0],
                parts.length > 1 ? parts[1] : "",
                details.learner().grade(),
                profile.className(),
                profile.email(),
                profile.subjects(),
                profile.about(),
                profile.goals());
    }
}