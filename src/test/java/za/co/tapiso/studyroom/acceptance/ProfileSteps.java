package za.co.tapiso.studyroom.acceptance;

import com.microsoft.playwright.Route;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import za.co.tapiso.studyroom.acceptance.pages.ProfilePage;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Step definitions for profile.feature: the learner's photo, and the initials when there is no photo.
 */
public class ProfileSteps {

    private final TestWorld world;

    public ProfileSteps(TestWorld world) {
        this.world = world;
    }

    // ---------- Given ----------

    @Given("my photo cannot be loaded")
    public void myPhotoCannotBeLoaded() {
        // Network stub: every learner photo request gets a 404, as if the file did not exist
        world.page().route("**/img/learners/**",
                route -> route.fulfill(new Route.FulfillOptions().setStatus(404)));
    }

    // ---------- Then ----------

    @Then("I see my photo for {string}")
    public void iSeeMyPhotoFor(String studentNumber) {
        ProfilePage profilePage = world.profilePage();
        // The photo only becomes visible once it has really loaded, so this also proves the file exists
        assertThat(profilePage.photo()).isVisible();
        // An exact match, not a regex: Playwright runs regexes in the browser as JavaScript,
        // which does not understand Java-only syntax such as Pattern.quote's \Q...\E
        assertThat(profilePage.photo()).hasAttribute("src", "/img/learners/" + studentNumber + ".jpg");
        assertThat(profilePage.initials()).isHidden();
    }

    @Then("I see my initials {string} instead of a photo")
    public void iSeeMyInitialsInsteadOfAPhoto(String initials) {
        ProfilePage profilePage = world.profilePage();
        assertThat(profilePage.initials()).isVisible();
        assertThat(profilePage.initials()).hasText(initials);
        assertThat(profilePage.photo()).isHidden();
    }
}