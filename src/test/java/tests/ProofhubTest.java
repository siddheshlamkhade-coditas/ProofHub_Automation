package tests;

import base.BaseTest;
import pages.ProofhubPage;
import org.testng.annotations.Test;

public class ProofhubTest extends BaseTest {

    @Test
    public void testLogin() {
        ProofhubPage proofhubPage = new ProofhubPage(page);

        // ✅ Navigate to base login URL, not the post-login page
        proofhubPage.navigateToLoginPage("https://coditas.proofhub.com/bapplite/#app/login");

        // Enter email and click Next
        proofhubPage.enterEmail("siddhesh.lamkhade@coditas.com");
        proofhubPage.clickNextButton();

        // ⚠️ Replace hardcoded wait with smart wait
        page.waitForTimeout(8000); // temporary — see note below

        // Enter password and log in
        proofhubPage.enterPassword("$idL@mkh@de.2");
        proofhubPage.clickSignInButton();

        // Navigate to project
        proofhubPage.clickProject();
        proofhubPage.clickProjectField();
        proofhubPage.clickAddTaskBtn();
        proofhubPage.clickInputfield();
        proofhubPage.enterTicketTitle("Auth Test Cases Ticket 2");
        // proofhubPage.enterDescription("Description: Validate add/archive company flows");
        proofhubPage.clickgetAddTaskBtn();
    }
}