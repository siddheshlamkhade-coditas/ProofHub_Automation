package com.proofhub.automation.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class ProofhubPage extends BasePage {

    // Locators
    private final Locator emailField;
    private final Locator nextButton;
    private final Locator passwordField;
    private final Locator signInButton;
    private final Locator projects;
    private final Locator addTaskBtn;
    private final Locator inputfield;
    private final Locator desc;
    private final Locator submitTicketBtn;

    public ProofhubPage(Page page) {
        super(page);

        this.emailField = page.locator("//input[@name='userNameEmail']");
        this.nextButton = page.locator("//span[text()='NEXT']");
        this.passwordField = page.locator("//input[@name='userPassword']");
        this.signInButton = page.locator("//a[@role='button' and .//span[text()='LOG IN']]");
        this.projects = page.locator("//span[contains(@class,'h-projects')][1]");
        // After the first task is created, ProofHub renders an "Add" span per board column
        // (empty columns get one too), so the same text match now resolves to multiple
        // elements. .first() pins it to the same one that worked correctly before any
        // tasks existed.
        this.addTaskBtn = page.locator("//span[text()='Add']").first();
        this.inputfield = page.locator("//input[@name='title']");
        // The description field is a TinyMCE rich-text editor rendered inside its own
        // iframe (#taskDesc_ifr) — the editable body lives in that iframe's document,
        // not the main page, so it must be reached via frameLocator().
        this.desc = page.frameLocator("#taskDesc_ifr").locator("body[data-id='taskDesc']");
        this.submitTicketBtn = page.locator("//a[contains(@class,'h-taskSubmit')]");
    }

    public void navigateToLoginPage(String url) {
        page.navigate(url);
    }

    public void enterEmail(String email) {
        emailField.fill(email);
    }

    public void clickNextButton() {
        nextButton.click();
    }

    public void enterPassword(String password) {
        passwordField.fill(password);
    }

    public void clickSignInButton() {
        signInButton.click();
    }

    public void clickProject() {
        projects.click();
    }

    public void clickProjectField(String projectName) {
        page.locator("//span[@data-qtip='" + projectName + "']").click();
    }

    public void clickSprint(String sprintName) {
        page.locator("//div[contains(@class,'title') and normalize-space(text())='" + sprintName + "']").click();
    }

    public void clickAddTaskBtn() {
        addTaskBtn.click();
    }

    public void clickInputfield() {
        inputfield.click();
    }

    public void enterTicketTitle(String title) {
        inputfield.fill(title);
    }

    public void enterDescription(String description) {
        desc.fill(description);
    }

    public void submitTicket() {
        submitTicketBtn.click();
    }

    public boolean isEmailFieldVisible() {
        return emailField.isVisible();
    }

    public boolean isPasswordFieldVisible() {
        return passwordField.isVisible();
    }

    public boolean isSignInButtonVisible() {
        return signInButton.isVisible();
    }

    public Page getPage() {
        return page;
    }
}
