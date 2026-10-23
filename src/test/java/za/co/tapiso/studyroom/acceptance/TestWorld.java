package za.co.tapiso.studyroom.acceptance;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import io.javalin.Javalin;
import za.co.tapiso.studyroom.App;
import za.co.tapiso.studyroom.acceptance.pages.BookPage;
import za.co.tapiso.studyroom.acceptance.pages.LoginPage;
import za.co.tapiso.studyroom.acceptance.pages.ProfilePage;
import za.co.tapiso.studyroom.domain.BookingPolicy;
import za.co.tapiso.studyroom.repository.InMemoryBookingRepository;
import za.co.tapiso.studyroom.repository.InMemoryLearnerRepository;
import za.co.tapiso.studyroom.repository.InMemoryRoomRepository;
import za.co.tapiso.studyroom.service.BookingService;

import java.time.Clock;

/**
 * Everything one scenario shares between its step classes: its own server and its own browser tab.
 * PicoContainer creates a new TestWorld for every scenario and gives the same one to every step class.
 */
public class TestWorld {

    private Javalin server;
    private String baseUrl;
    private BrowserContext context;
    private Page page;

    void open(Browser browser) {
        // In-memory bookings, so scenarios never touch the real database file
        BookingService bookings = new BookingService(
                new InMemoryBookingRepository(),
                new BookingPolicy(Clock.systemDefaultZone()));

        // Port 0 lets the operating system pick any free port, so a running copy on 7070 is not a problem
        server = App.create(
                InMemoryRoomRepository.withDefaultRooms(),
                bookings,
                InMemoryLearnerRepository.withSampleLearners()).start(0);
        baseUrl = "http://localhost:" + server.port();

        // A new context is like a fresh private window: no sessionStorage left over from another scenario
        context = browser.newContext();
        page = context.newPage();
    }

    void close() {
        if (context != null) {
            context.close();
        }
        if (server != null) {
            server.stop();
        }
    }

    public Page page() {
        return page;
    }

    public String baseUrl() {
        return baseUrl;
    }

    public LoginPage loginPage() {
        return new LoginPage(page, baseUrl);
    }

    public ProfilePage profilePage() {
        return new ProfilePage(page, baseUrl);
    }

    public BookPage bookPage() {
        return new BookPage(page, baseUrl);
    }
}