package com.proofhub.automation.core;

import com.proofhub.automation.config.EnvConfig;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Tracing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Thread-local Playwright lifecycle manager.
 *
 * <p>Lifecycle contract (driven by {@code BaseTest}):
 * <ul>
 *   <li>{@link #initBrowser(String)} — once per TestNG {@code <test>} tag (per thread):
 *       creates the Playwright process and launches the requested browser.</li>
 *   <li>{@link #createContextAndPage()} — once per test method: fresh BrowserContext + page,
 *       video recording, and tracing started.</li>
 *   <li>{@link #closeContext(boolean, String)} — once per test method: saves trace and
 *       keeps video only when the test failed, then closes the context.</li>
 *   <li>{@link #closeBrowser()} — once per {@code <test>} tag: closes browser + Playwright.</li>
 * </ul>
 *
 * <p>Everything is ThreadLocal so parallel TestNG execution never shares mutable state.
 */
public final class PlaywrightFactory {

    private static final Logger LOG = LoggerFactory.getLogger(PlaywrightFactory.class);

    private static final Path VIDEO_DIR = Paths.get("videos");
    private static final Path TRACE_DIR = Paths.get("traces");

    private static final ThreadLocal<Playwright> PLAYWRIGHT = new ThreadLocal<>();
    private static final ThreadLocal<Browser> BROWSER = new ThreadLocal<>();
    private static final ThreadLocal<BrowserContext> CONTEXT = new ThreadLocal<>();
    private static final ThreadLocal<Page> PAGE = new ThreadLocal<>();
    private static final ThreadLocal<String> BROWSER_NAME = new ThreadLocal<>();

    private PlaywrightFactory() {
    }

    /**
     * Creates the thread's Playwright process and launches the requested browser.
     *
     * @param browserName {@code chromium}, {@code firefox}, or {@code webkit}.
     */
    public static void initBrowser(String browserName) {
        EnvConfig config = EnvConfig.get();
        Playwright playwright = Playwright.create();
        PLAYWRIGHT.set(playwright);
        BROWSER_NAME.set(browserName);

        Browser browser = switch (browserName) {
            case "chromium" -> playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setHeadless(config.isHeadless()));
            case "firefox" -> playwright.firefox().launch(new BrowserType.LaunchOptions()
                    .setHeadless(config.isHeadless()));
            case "webkit" -> playwright.webkit().launch(new BrowserType.LaunchOptions()
                    .setHeadless(config.isHeadless()));
            default -> throw new IllegalArgumentException(String.format(
                    "[PlaywrightFactory] Unknown browser \"%s\". Valid values: chromium, firefox, webkit.",
                    browserName));
        };
        BROWSER.set(browser);
        LOG.info("Launched {} (headless={})", browserName, config.isHeadless());
    }

    /**
     * Creates a fresh BrowserContext + Page for one test method: records video and starts
     * tracing with screenshots and DOM snapshots. Artifacts are discarded later for passing
     * tests.
     */
    public static Page createContextAndPage() {
        EnvConfig config = EnvConfig.get();

        Browser.NewContextOptions options = new Browser.NewContextOptions()
                .setRecordVideoDir(VIDEO_DIR);

        BrowserContext context = requireBrowser().newContext(options);
        context.setDefaultTimeout(config.getNavigationTimeoutMs());
        context.setDefaultNavigationTimeout(config.getNavigationTimeoutMs());
        context.tracing().start(new Tracing.StartOptions()
                .setScreenshots(true)
                .setSnapshots(true)
                .setSources(true));

        Page page = context.newPage();
        CONTEXT.set(context);
        PAGE.set(page);
        return page;
    }

    /**
     * Ends one test method's context: saves the trace zip and keeps the video only when the
     * test failed; otherwise both artifacts are discarded. Always closes the context.
     */
    public static void closeContext(boolean testFailed, String artifactName) {
        BrowserContext context = CONTEXT.get();
        Page page = PAGE.get();
        if (context == null) {
            return;
        }

        try {
            if (testFailed) {
                Path tracePath = TRACE_DIR.resolve(artifactName + ".zip");
                context.tracing().stop(new Tracing.StopOptions().setPath(tracePath));
                LOG.info("Trace for failed test saved to {}", tracePath.toAbsolutePath());
            } else {
                context.tracing().stop();
            }
        } finally {
            context.close();
            if (!testFailed && page != null && page.video() != null) {
                // Video files finalize on context close; passing runs don't need them.
                page.video().delete();
            }
            CONTEXT.remove();
            PAGE.remove();
        }
    }

    /** Closes the thread's browser and Playwright process. */
    public static void closeBrowser() {
        Browser browser = BROWSER.get();
        if (browser != null) {
            browser.close();
            BROWSER.remove();
        }

        Playwright playwright = PLAYWRIGHT.get();
        if (playwright != null) {
            playwright.close();
            PLAYWRIGHT.remove();
        }
        BROWSER_NAME.remove();
    }

    /** Current thread's page, or null outside a test method's context window. */
    public static Page getPage() {
        return PAGE.get();
    }

    /** Browser name for the current thread, or null. */
    public static String getBrowserName() {
        return BROWSER_NAME.get();
    }

    private static Browser requireBrowser() {
        Browser browser = BROWSER.get();
        if (browser == null) {
            throw new IllegalStateException(
                    "[PlaywrightFactory] No browser launched on this thread — initBrowser() must run first.");
        }
        return browser;
    }
}
