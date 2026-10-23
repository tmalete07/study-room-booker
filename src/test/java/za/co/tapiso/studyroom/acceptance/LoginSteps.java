package za.co.tapiso.studyroom.acceptance;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import za.co.tapiso.studyroom.acceptance.pages.LoginPage;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Step definitions for login.feature.
 * The server and browser tab come from the scenario's TestWorld, set up by Hooks.
 */
public class LoginSteps {

    private final TestWorld world;

    public LoginSteps(TestWorld world) {
        this.world = world;
    }

    // ---------- Given ----------

    @Given("I am on the login page")
    public void iAmOnTheLoginPage() {
        world.loginPage().open();
    }

    // ---------- When ----------

    @When("I log in as {string} with student number {string}")
    public void iLogInAs(String name, String studentNumber) {
        world.loginPage().logIn(name, studentNumber);
    }

    // ---------- Then ----------

    // Playwright's assertThat keeps retrying until it passes or times out, so no sleeps are needed
    @Then("I see my profile page")
    public void iSeeMyProfilePage() {
        assertThat(world.page()).hasURL(world.profilePage().url());
        assertThat(world.profilePage().title()).hasText("My learner profile");
    }

    @Then("my name {string} is shown")
    public void myNameIsShown(String name) {
        assertThat(world.profilePage().learnerName()).containsText(name);
    }

    @Then("I am still on the login page")
    public void iAmStillOnTheLoginPage() {
        assertThat(world.page()).hasURL(LoginPage.URL);
    }

    @Then("I see a login error")
    public void iSeeALoginError() {
        assertThat(world.loginPage().message()).hasClass(Pattern.compile("\\berror\\b"));
        assertThat(world.loginPage().message()).not().isEmpty();
    }
}