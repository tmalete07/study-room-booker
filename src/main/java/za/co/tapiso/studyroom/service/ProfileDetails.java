package za.co.tapiso.studyroom.service;

import za.co.tapiso.studyroom.domain.Learner;
import za.co.tapiso.studyroom.domain.LearnerProfile;

/**
 * Everything shown on a learner's profile page: who they are, and their profile.
 */
public record ProfileDetails(Learner learner, LearnerProfile profile) {
}