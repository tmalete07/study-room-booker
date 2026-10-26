package za.co.tapiso.studyroom.acceptance.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/**
 * The learner's profile page, which is where a learner lands after logging in.
 */
public class ProfilePage {

    private final Page page;
    private final String baseUrl;

    public ProfilePage(Page page, String baseUrl) {
        this.page = page;
        this.baseUrl = baseUrl;
    }

    public String url() {
        return baseUrl + "/";
    }

    public Locator title() {
        return page.locator(".page-title");
    }

    public Locator learnerName() {
        return page.getByTestId("profile-name");
    }

    public Locator photo() {
        return page.getByTestId("profile-photo");
    }

    public Locator initials() {
        return page.getByTestId("profile-initials");
    }
}