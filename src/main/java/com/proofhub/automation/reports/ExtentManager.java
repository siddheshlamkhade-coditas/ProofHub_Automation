package com.proofhub.automation.reports;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.proofhub.automation.config.EnvConfig;

/**
 * Lazily creates and holds the single ExtentReports instance for a test run.
 * The HTML report is written to {@code reports/extent-report.html} (gitignored) and flushed
 * by the TestNG listener when the suite finishes.
 */
public final class ExtentManager {

    private static final String REPORT_PATH = "reports/extent-report.html";

    private static volatile ExtentReports extent;

    private ExtentManager() {
    }

    public static ExtentReports getInstance() {
        if (extent == null) {
            synchronized (ExtentManager.class) {
                if (extent == null) {
                    extent = create();
                }
            }
        }
        return extent;
    }

    private static ExtentReports create() {
        ExtentSparkReporter spark = new ExtentSparkReporter(REPORT_PATH);
        spark.config().setDocumentTitle("ProofHub E2E Report");
        spark.config().setReportName("ProofHub E2E — Playwright + TestNG");
        spark.config().setTheme(Theme.STANDARD);

        ExtentReports reports = new ExtentReports();
        reports.attachReporter(spark);

        EnvConfig config = EnvConfig.get();
        reports.setSystemInfo("Environment", config.getEnv());
        reports.setSystemInfo("Base URL", config.getBaseUrl());
        reports.setSystemInfo("OS", System.getProperty("os.name"));
        reports.setSystemInfo("Java", System.getProperty("java.version"));
        return reports;
    }
}
