package com.proofhub.automation.tests;

import com.proofhub.automation.base.BaseTest;
import com.proofhub.automation.config.EnvConfig;
import com.proofhub.automation.pages.ProofhubPage;
import com.proofhub.automation.utils.DefectReportReader;
import com.proofhub.automation.utils.DefectRow;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.util.List;

public class ProofhubTest extends BaseTest {

    @Test(description = "user logs in and creates ticket(s)")
    public void testLogin() {
        EnvConfig config = EnvConfig.get();
        ProofhubPage proofhubPage = new ProofhubPage(page);

        proofhubPage.navigateToLoginPage(config.getBaseUrl());

        // Enter email and click Next
        proofhubPage.enterEmail(config.getTestEmail());
        proofhubPage.clickNextButton();

        // TODO: replace hardcoded wait with a smart wait once the password field's
        // own locator-based auto-wait is confirmed sufficient.
        page.waitForTimeout(8000);

        // Enter password and log in
        proofhubPage.enterPassword(config.getTestPassword());
        proofhubPage.clickSignInButton();

        // Navigate to project and sprint
        proofhubPage.clickProject();
        String projectName = System.getProperty("projectName", "Collabricks");
        proofhubPage.clickProjectField(projectName);
        String sprintName = System.getProperty("sprintName", "Sprint 2");
        proofhubPage.clickSprint(sprintName);

        String defectFilePath = System.getProperty("defectFilePath");
        if (defectFilePath != null && !defectFilePath.isBlank()) {
            // One ticket per row in the uploaded defect-report xlsx, optionally restricted to
            // an inclusive Excel row range (e.g. -DstartRow=3 -DendRow=8).
            Integer startRow = parseIntProperty("startRow");
            Integer endRow = parseIntProperty("endRow");
            List<DefectRow> defects = DefectReportReader.read(Path.of(defectFilePath), startRow, endRow);
            for (DefectRow defect : defects) {
                createTicket(proofhubPage, defect.toTicketTitle(), defect.toTicketDescription());
            }
        } else {
            // No file supplied — single ticket from system properties (or their defaults).
            String ticketTitle = System.getProperty("ticketTitle", "Auth Test Cases Ticket 2");
            String ticketDescription = System.getProperty("ticketDescription", "Created via Playwright automation");
            createTicket(proofhubPage, ticketTitle, ticketDescription);
        }
    }

    private void createTicket(ProofhubPage proofhubPage, String title, String description) {
        proofhubPage.clickAddTaskBtn();
        proofhubPage.clickInputfield();
        proofhubPage.enterTicketTitle(title);
        proofhubPage.enterDescription(description);
        proofhubPage.submitTicket();
    }

    private static Integer parseIntProperty(String key) {
        String value = System.getProperty(key);
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format(
                    "[ProofhubTest] -D%s=\"%s\" is not a valid integer.", key, value), e);
        }
    }
}
