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
    private Locator projects;
    private Locator projectField;
    private Locator addTaskBtn;
    private Locator inputfield;
    private Locator desc;
    private Locator getAddTaskBtn;

    public ProofhubPage(Page page) {
        this.page = page;
        initializeLocators();
    }


    private void initializeLocators() {
        this.emailField = page.locator("//input[@name='userNameEmail']");

        this.nextButton = page.locator("//span[text()='NEXT']");

        this.passwordField = page.locator("//input[@name='userPassword']");

        this.signInButton = page.locator("//a[@role='button' and .//span[text()='LOG IN']]");

        this.projects = page.locator("//span[contains(@class,'h-projects')][1]");

        this.projectField = page.locator("//span[@data-qtip='Collabricks']");

        this.addTaskBtn =page.locator("//span[text()='Add']");

        this.inputfield = page.locator("//input[@name='title']");

        this.desc = page.locator("//body[@data-id='taskDesc']");

        this.getAddTaskBtn =page.locator("//a[contains(@class,'h-taskSubmit')]");
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

    public void clickProjectField() {
        projectField.click();
    }

    public void clickAddTaskBtn() {
        addTaskBtn.click();
    }
    public void clickInputfield() {
        inputfield.click();
    }

    public void enterTicketTitle(String title){
        inputfield.fill(title);
    }

    public void enterDescription(String description){
        desc.fill(description);
    }

    public void clickgetAddTaskBtn() {
        getAddTaskBtn.click();
    }

}

