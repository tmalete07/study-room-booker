package za.co.tapiso.studyroom.acceptance.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

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