package com.proofhub.automation.base;

import com.proofhub.automation.config.EnvConfig;
import com.proofhub.automation.core.PlaywrightFactory;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

/**
 * Base class every E2E test class extends.
 *
 * <p>Lifecycle:
 * <ul>
 *   <li>{@code @BeforeSuite} — one-time env validation and default assertion timeout.</li>
 *   <li>{@code @BeforeTest} — per TestNG {@code <test>} tag: launch the browser for that tag.</li>
 *   <li>{@code @BeforeMethod} — fresh context + page + tracing for each test method.</li>
 *   <li>{@code @AfterMethod} — keep trace/video only for failures, close the context.</li>
 *   <li>{@code @AfterTest} — close the browser.</li>
 * </ul>
 */
public abstract class BaseTest {

    protected Page page;

    @BeforeSuite(alwaysRun = true)
    public void globalSetup() {
        EnvConfig config = EnvConfig.get();
        PlaywrightAssertions.setDefaultAssertionTimeout(config.getNavigationTimeoutMs());
    }

    @Parameters("browser")
    @BeforeTest(alwaysRun = true)
    public void launchBrowser(@Optional("chromium") String browser) {
        PlaywrightFactory.initBrowser(browser);
    }

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        page = PlaywrightFactory.createContextAndPage();
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        boolean failed = result.getStatus() == ITestResult.FAILURE;
        String artifactName = result.getMethod().getMethodName() + "-" + PlaywrightFactory.getBrowserName();
        PlaywrightFactory.closeContext(failed, artifactName);
        page = null;
    }

    @AfterTest(alwaysRun = true)
    public void closeBrowser() {
        PlaywrightFactory.closeBrowser();
    }
}
