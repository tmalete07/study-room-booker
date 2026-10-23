package za.co.tapiso.studyroom.acceptance;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;
import io.cucumber.java.After;
import io.cucumber.java.AfterAll;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;

/**
 * Starts and stops things around the scenarios: the browser once for the whole run,
 * and a fresh server and browser tab for every scenario.
 */
public class Hooks {

    // Starting Chromium is slow, so one browser is shared by the whole run
    private static Playwright playwright;
    private static Browser browser;

    private final TestWorld world;

    public Hooks(TestWorld world) {
        this.world = world;
    }

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
    public void openScenario() {
        world.open(browser);
    }

    @After
    public void closeScenario() {
        world.close();
    }
}