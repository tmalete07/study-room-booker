package za.co.tapiso.studyroom.acceptance;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import za.co.tapiso.studyroom.acceptance.pages.BookPage;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Step definitions for room-access.feature: which rooms a learner can choose on the booking page.
 */
public class RoomAccessSteps {

    private final TestWorld world;

    public RoomAccessSteps(TestWorld world) {
        this.world = world;
    }

    // ---------- When ----------

    @When("I open the booking page")
    public void iOpenTheBookingPage() {
        world.bookPage().open();
    }

    @When("I open the booking page for room {string}")
    public void iOpenTheBookingPageForRoom(String roomId) {
        world.bookPage().openForRoom(roomId);
    }

    // ---------- Then ----------

    @Then("I can choose room {string}")
    public void iCanChooseRoom(String roomId) {
        assertThat(world.bookPage().roomOption(roomId)).hasCount(1);
    }

    @Then("I cannot choose room {string}")
    public void iCannotChooseRoom(String roomId) {
        BookPage bookPage = world.bookPage();
        bookPage.waitForRoomsToLoad();
        assertThat(bookPage.roomOption(roomId)).hasCount(0);
    }

    @Then("no room is chosen")
    public void noRoomIsChosen() {
        BookPage bookPage = world.bookPage();
        bookPage.waitForRoomsToLoad();
        // An empty value means the "Choose a room" placeholder is still selected
        assertThat(bookPage.roomSelect()).hasValue("");
    }
}