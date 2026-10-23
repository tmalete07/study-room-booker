package za.co.tapiso.studyroom.acceptance;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import io.cucumber.java.After;
import io.cucumber.java.AfterAll;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.javalin.Javalin;
import za.co.tapiso.studyroom.App;
import za.co.tapiso.studyroom.acceptance.pages.LoginPage;
import za.co.tapiso.studyroom.acceptance.pages.ProfilePage;
import za.co.tapiso.studyroom.domain.BookingPolicy;
import za.co.tapiso.studyroom.repository.InMemoryBookingRepository;
import za.co.tapiso.studyroom.repository.InMemoryLearnerRepository;
import za.co.tapiso.studyroom.repository.InMemoryRoomRepository;
import za.co.tapiso.studyroom.service.BookingService;

import java.time.Clock;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Step definitions for login.feature.
 * Each scenario gets its own server (fresh in-memory data) and its own browser context
 * (empty sessionStorage), so scenarios can never affect each other.
 */
public class LoginSteps {

    // Starting Chromium is slow, so one browser is shared by the whole run
    private static Playwright playwright;
    private static Browser browser;

    private Javalin server;
    private BrowserContext context;
    private Page page;
    private LoginPage loginPage;
    private ProfilePage profilePage;

    @BeforeAll
    public static void launchBrowser() {
        playwright = Playwright.create();
        // Run with -Dheadless=false to watch the browser do the steps
        boolean headless = Boolean.parseBoolean(System.getProperty("headless", "true"));
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(headless));
    }

    @AfterAll
    public static void closeBrowser() {
        browser.close();
        playwright.close();
    }

    @Before
    public void startServerAndOpenTab() {
        // In-memory bookings, so scenarios never touch the real database file
        BookingService bookings = new BookingService(
                new InMemoryBookingRepository(),
                new BookingPolicy(Clock.systemDefaultZone()));

        // Port 0 lets the operating system pick any free port, so a running copy on 7070 is not a problem
        server = App.create(
                InMemoryRoomRepository.withDefaultRooms(),
                bookings,
                InMemoryLearnerRepository.withSampleLearners()).start(0);
        String baseUrl = "http://localhost:" + server.port();

        context = browser.newContext();
        page = context.newPage();
        loginPage = new LoginPage(page, baseUrl);
        profilePage = new ProfilePage(page, baseUrl);
    }

    @After
    public void closeTabAndStopServer() {
        context.close();
        server.stop();
    }

    // ---------- Given ----------

    @Given("I am on the login page")
    public void iAmOnTheLoginPage() {
        loginPage.open();
    }

    // ---------- When ----------

    @When("I log in as {string} with student number {string}")
    public void iLogInAs(String name, String studentNumber) {
        loginPage.logIn(name, studentNumber);
    }

    // ---------- Then ----------

    // Playwright's assertThat keeps retrying until it passes or times out, so no sleeps are needed
    @Then("I see my profile page")
    public void iSeeMyProfilePage() {
        assertThat(page).hasURL(profilePage.url());
        assertThat(profilePage.title()).hasText("My learner profile");
    }

    @Then("my name {string} is shown")
    public void myNameIsShown(String name) {
        assertThat(profilePage.learnerName()).containsText(name);
    }

    @Then("I am still on the login page")
    public void iAmStillOnTheLoginPage() {
        assertThat(page).hasURL(LoginPage.URL);
    }

    @Then("I see a login error")
    public void iSeeALoginError() {
        assertThat(loginPage.message()).hasClass(Pattern.compile("\\berror\\b"));
        assertThat(loginPage.message()).not().isEmpty();
    }
}