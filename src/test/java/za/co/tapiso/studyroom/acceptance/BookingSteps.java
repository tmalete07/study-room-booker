package za.co.tapiso.studyroom.acceptance;

import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.options.RequestOptions;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import za.co.tapiso.studyroom.acceptance.pages.BookPage;

import java.time.LocalDate;
import java.util.Map;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Step definitions for booking a room.
 * Dates are always "tomorrow", worked out when the test runs, so the scenarios never go out of date.
 */
public class BookingSteps {

    private final TestWorld world;

    public BookingSteps(TestWorld world) {
        this.world = world;
    }

    // ---------- Given ----------

    @Given("I am logged in as {string} with student number {string}")
    public void iAmLoggedInAs(String name, String studentNumber) {
        world.loginPage().open().logIn(name, studentNumber);
        // Wait for the login to finish, otherwise the next page would find no learner and send us back
        assertThat(world.page()).hasURL(world.profilePage().url());
    }

    @Given("{string} has already booked room {string} tomorrow from {string} to {string}")
    public void someoneHasAlreadyBooked(String studentNumber, String roomId, String start, String end) {
        // Set up through the API: quicker than the browser, and this part is not what the scenario tests
        APIResponse response = world.page().request().post(world.baseUrl() + "/api/bookings",
                RequestOptions.create().setData(Map.of(
                        "roomId", roomId,
                        "studentId", studentNumber,
                        "start", tomorrow() + "T" + start,
                        "end", tomorrow() + "T" + end)));

        // A failed set-up must stop the scenario here, not cause a confusing failure later
        if (!response.ok()) {
            throw new IllegalStateException(
                    "Could not set up the booking: " + response.status() + " " + response.text());
        }
    }

    // ---------- When ----------

    @When("I book room {string} tomorrow from {string} to {string}")
    public void iBookRoom(String roomId, String start, String end) {
        world.bookPage().open().book(roomId, tomorrow(), start, end);
    }

    // ---------- Then ----------

    @Then("I see the booking confirmed for {string}")
    public void iSeeTheBookingConfirmed(String times) {
        BookPage bookPage = world.bookPage();
        assertThat(bookPage.successDialog()).isVisible();
        assertThat(bookPage.successDetails()).containsText(times);
    }

    @Then("I see the booking rejected because {string}")
    public void iSeeTheBookingRejected(String reason) {
        BookPage bookPage = world.bookPage();
        assertThat(bookPage.errorDialog()).isVisible();
        assertThat(bookPage.errorDetails()).containsText(reason);
    }

    private static String tomorrow() {
        return LocalDate.now().plusDays(1).toString();
    }
}