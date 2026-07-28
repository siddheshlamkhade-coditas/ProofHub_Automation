package com.proofhub.automation.listeners;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.proofhub.automation.core.PlaywrightFactory;
import com.proofhub.automation.reports.ExtentManager;
import com.proofhub.automation.utils.ScreenshotUtil;
import com.microsoft.playwright.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.nio.file.Path;

/**
 * TestNG listener wiring every test into the Extent report and the log file.
 *
 * <p>On failure it captures a full-page screenshot from the current thread's Page (fires
 * before {@code @AfterMethod} closes the context) and attaches it to the report entry.
 */
public class ExtentTestListener implements ITestListener {

    private static final Logger LOG = LoggerFactory.getLogger(ExtentTestListener.class);

    private static final ThreadLocal<ExtentTest> CURRENT_TEST = new ThreadLocal<>();

    /** Extent node for the currently running test on this thread (used for mid-test logging). */
    public static ExtentTest currentTest() {
        return CURRENT_TEST.get();
    }

    @Override
    public void onTestStart(ITestResult result) {
        String browser = result.getTestContext().getCurrentXmlTest().getParameter("browser");
        String name = String.format("%s [%s]", result.getMethod().getMethodName(),
                browser == null ? "no-browser" : browser);

        ExtentTest test = ExtentManager.getInstance().createTest(name,
                result.getMethod().getDescription());
        CURRENT_TEST.set(test);
        LOG.info("STARTED  {}", name);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        CURRENT_TEST.get().log(Status.PASS, "Test passed");
        LOG.info("PASSED   {}", result.getMethod().getMethodName());
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ExtentTest test = CURRENT_TEST.get();
        test.fail(result.getThrowable());

        Page page = PlaywrightFactory.getPage();
        if (page != null && !page.isClosed()) {
            try {
                String name = result.getMethod().getMethodName() + "-"
                        + PlaywrightFactory.getBrowserName();
                Path screenshot = ScreenshotUtil.captureNamed(page, name);
                test.addScreenCaptureFromPath(screenshot.toString());
            } catch (RuntimeException e) {
                test.log(Status.WARNING, "Could not capture failure screenshot: " + e.getMessage());
            }
        }
        LOG.error("FAILED   {}", result.getMethod().getMethodName(), result.getThrowable());
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        ExtentTest test = CURRENT_TEST.get();
        if (test != null) {
            test.log(Status.SKIP, "Test skipped"
                    + (result.getThrowable() == null ? "" : ": " + result.getThrowable().getMessage()));
        }
        LOG.warn("SKIPPED  {}", result.getMethod().getMethodName());
    }

    @Override
    public void onFinish(ITestContext context) {
        ExtentManager.getInstance().flush();
    }
}
