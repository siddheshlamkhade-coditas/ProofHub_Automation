package base;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;


public class BaseTest {
    protected Playwright playwright;
    protected Browser browser;
    protected Page page;

    @BeforeClass
    public void setUp() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions()
                        .setHeadless(false)
                        .setSlowMo(3000));
        page = browser.newPage();
    }

    @AfterClass
    public void tearDown() {
        try {
            if (page != null) {
                page.close();
            }
        } catch (Throwable t) {
            System.err.println("Warning: page.close() failed: " + t.getMessage());
        } finally {
            page = null;
        }

        try {
            if (browser != null) {
                browser.close();
            }
        } catch (Throwable t) {
            System.err.println("Warning: browser.close() failed: " + t.getMessage());
        } finally {
            browser = null;
        }

        try {
            if (playwright != null) {
                playwright.close();
            }
        } catch (Throwable t) {
            System.err.println("Warning: playwright.close() failed: " + t.getMessage());
        } finally {
            playwright = null;
        }
    }
}
