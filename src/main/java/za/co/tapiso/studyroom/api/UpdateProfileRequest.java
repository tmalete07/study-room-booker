package za.co.tapiso.studyroom.api;

import za.co.tapiso.studyroom.domain.StudyGoal;

import java.util.ArrayList;
import java.util.List;

/**
 * The JSON a learner sends to edit their profile, for example:
 * {"subjects": ["Mathematics"], "about": "I like science.", "goals": [{"title": "Pass Maths", "detail": "Study daily."}]}
 * Only these three parts can be edited; any other field makes the request invalid.
 */
public record UpdateProfileRequest(List<String> subjects, String about, List<GoalRequest> goals) {

    /**
     * One goal as it arrives in JSON. It becomes a StudyGoal only after the request is read,
     * so a rule like "Goal title is required" comes back as that message, not as "invalid JSON".
     */
    public record GoalRequest(String title, String detail) {
    }

    public List<StudyGoal> studyGoals() {
        if (goals == null) {
            return null;
        }
        List<StudyGoal> studyGoals = new ArrayList<>();
        for (GoalRequest goal : goals) {
            studyGoals.add(goal == null ? null : new StudyGoal(goal.title(), goal.detail()));
        }
        return studyGoals;
    }
}