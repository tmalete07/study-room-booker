package za.co.tapiso.studyroom.api;

/**
 * The JSON a learner sends to log in, for example:
 * {"name": "Palesa Ndlovu", "studentNumber": "TSS4001-11"}
 */
public record LoginRequest(String name, String studentNumber) {
}