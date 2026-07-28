package com.proofhub.automation.utils;

import com.microsoft.playwright.Page;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Screenshot capture helpers used by the failure listener and available for
 * mid-test debugging captures.
 */
public final class ScreenshotUtil {

    private static final String DEFAULT_DIR = "screenshots";

    private ScreenshotUtil() {
    }

    /**
     * Captures a screenshot of {@code page} and writes it to {@code <dir>/<sanitizedName>.png}.
     *
     * <p>The name is sanitized: slashes, spaces, and other filesystem-unsafe characters are
     * replaced with hyphens so the output is always a single flat file rather than an
     * unintended nested path.
     *
     * @return the absolute path of the written screenshot file.
     */
    public static Path captureNamed(Page page, String name, String dir) {
        String sanitized = name
                .replaceAll("[/\\\\:\\s*?\"<>|]+", "-")
                .replaceAll("-{2,}", "-")
                .replaceAll("^-|-$", "");

        Path filePath = Paths.get(dir, sanitized + ".png").toAbsolutePath();
        page.screenshot(new Page.ScreenshotOptions().setPath(filePath).setFullPage(true));
        return filePath;
    }

    /** Captures to the default {@code screenshots/} directory. */
    public static Path captureNamed(Page page, String name) {
        return captureNamed(page, name, DEFAULT_DIR);
    }
}
