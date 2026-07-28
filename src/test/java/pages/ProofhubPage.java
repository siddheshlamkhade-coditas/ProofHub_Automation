package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class ProofhubPage {
    private Page page;

    // Locators
    private Locator emailField;
    private Locator nextButton;
    private Locator passwordField;
    private Locator signInButton;
    private Locator project;


    public ProofhubPage(Page page) {
        this.page = page;
        initializeLocators();
    }


    private void initializeLocators() {
        this.emailField = page.locator("//*[@id='textfield-1018-inputWrap']/input");

        this.nextButton = page.locator("//*[@id='button-1038']/span");

        this.passwordField = page.locator("//*[@id='textfield-1046-inputEl']");

        this.signInButton = page.locator("#button-1028-btnEl");

        this.project = page.locator("#ext-element-82 > div > span.icon.far.fa-folder.h-projects");
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
        project.click();
    }


    public void login(String email, String password) {
        enterEmail(email);
        clickNextButton();
        page.waitForTimeout(1000); // Wait for password field to appear
        enterPassword(password);
        clickSignInButton();
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

