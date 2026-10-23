package za.co.tapiso.studyroom.acceptance.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;

/**
 * The booking page: choose a room, a date and a time, then book.
 * Shows a success pop-up (tick) or an error pop-up (red cross) with the reason.
 */
public class BookPage {

    private final Page page;
    private final String baseUrl;

    public BookPage(Page page, String baseUrl) {
        this.page = page;
        this.baseUrl = baseUrl;
    }

    public BookPage open() {
        page.navigate(baseUrl + "/book.html");
        return this;
    }

    /**
     * Opens the page the way the Rooms page links to it, with a room already asked for in the address.
     */
    public BookPage openForRoom(String roomId) {
        page.navigate(baseUrl + "/book.html?room=" + roomId);
        return this;
    }

    /**
     * Fills in and sends the booking form. The date is yyyy-MM-dd and the times are HH:mm.
     */
    public void book(String roomId, String date, String start, String end) {
        // selectOption waits until the room is in the list, so it is safe even while rooms are still loading
        page.getByTestId("room-select").selectOption(roomId);
        page.getByTestId("booking-date").fill(date);
        page.getByTestId("start-time").fill(start);
        page.getByTestId("end-time").fill(end);
        page.getByTestId("book-button").click();
    }

    public Locator roomSelect() {
        return page.getByTestId("room-select");
    }

    public Locator roomOption(String roomId) {
        return page.locator("[data-testid='room-select'] option[value='" + roomId + "']");
    }

    /**
     * Waits until the rooms from the API are in the list (any option other than "Choose a room").
     * Checks that a room is NOT there must wait for this first, or they would pass before the list has loaded.
     */
    public void waitForRoomsToLoad() {
        page.locator("[data-testid='room-select'] option:not([value=''])").first()
                .waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.ATTACHED));
    }

    public Locator successDialog() {
        return page.getByTestId("booking-dialog");
    }

    public Locator successDetails() {
        return page.getByTestId("booking-dialog-details");
    }

    public Locator errorDialog() {
        return page.getByTestId("error-dialog");
    }

    public Locator errorDetails() {
        return page.getByTestId("error-dialog-details");
    }
}