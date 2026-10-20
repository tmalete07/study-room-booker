package za.co.tapiso.studyroom.api;

import za.co.tapiso.studyroom.domain.Learner;

/**
 * The JSON sent back after a successful login, for example:
 * {"studentNumber": "TSS4001-11", "name": "Palesa Ndlovu", "grade": 11}
 */
public record LoginResponse(String studentNumber, String name, int grade) {

    public static LoginResponse from(Learner learner) {
        return new LoginResponse(learner.studentNumber().value(), learner.name(), learner.grade());
    }
}