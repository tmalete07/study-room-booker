package za.co.tapiso.studyroom.acceptance.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.util.regex.Pattern;

/**
 * The login page: knows its address and how to fill in and send the login form.
 * Tests use this instead of the page's HTML details, so a change to the page is fixed in one place.
 */
public class LoginPage {

    public static final Pattern URL = Pattern.compile(".*/login\\.html$");

    private final Page page;
    private final String baseUrl;

    public LoginPage(Page page, String baseUrl) {
        this.page = page;
        this.baseUrl = baseUrl;
    }

    public LoginPage open() {
        page.navigate(baseUrl + "/login.html");
        return this;
    }

    public void logIn(String name, String studentNumber) {
        page.getByTestId("learner-name").fill(name);
        page.getByTestId("student-number").fill(studentNumber);
        page.getByTestId("login-button").click();
    }

    public Locator message() {
        return page.getByTestId("login-message");
    }
}