package com.proofhub.automation.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Abstract base class for all page objects.
 *
 * <p>Wraps common Playwright operations with consistent behavior. Concrete page objects
 * extend this class and add page-specific locators and actions.
 */
public abstract class BasePage {

    protected final Page page;

    protected BasePage(Page page) {
        this.page = page;
    }

    /**
     * Waits for the page to reach a stable network state.
     * Use after navigations triggered by form submissions or link clicks.
     */
    public void waitForPageLoad() {
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    /**
     * Clicks the given locator.
     *
     * <p>Delegates to Playwright's own actionability auto-waiting (visible, stable, enabled,
     * receives events) rather than a manual wait + immediate click.
     */
    public void click(Locator locator) {
        locator.click();
    }

    /**
     * Clears the field identified by {@code locator} and types {@code value}.
     * Uses {@code Locator.fill()} directly, which relies on Playwright's own actionability
     * auto-waiting.
     */
    public void fill(Locator locator, String value) {
        locator.fill(value);
    }

    /**
     * Asserts that {@code locator} is visible.
     * Uses Playwright's own web-first assertion, which auto-retries until the default
     * assertion timeout before raising a Playwright-native assertion error.
     */
    public void expectVisible(Locator locator) {
        assertThat(locator).isVisible();
    }

    /** Navigates to an absolute or context-baseURL-relative {@code url}. */
    public void navigateTo(String url) {
        page.navigate(url);
    }
}
