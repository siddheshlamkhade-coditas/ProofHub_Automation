package com.proofhub.automation.utils;

/** One data row from the defect-report xlsx, mapped onto a ProofHub ticket. */
public record DefectRow(
        String defectId,
        String description,
        String modulePage,
        String preconditions,
        String testData,
        String stepsToReproduce,
        String expectedResult,
        String actualResult,
        String severity,
        String priority,
        String reportedBy
) {

    /** Ticket title: "<Defect ID> - <Defect Description>". */
    public String toTicketTitle() {
        if (defectId.isBlank()) {
            return description;
        }
        return defectId + " - " + description;
    }

    /** Ticket description: every remaining column, labeled, one per paragraph. */
    public String toTicketDescription() {
        StringBuilder sb = new StringBuilder();
        appendIfPresent(sb, "Module/Page", modulePage);
        appendIfPresent(sb, "Preconditions", preconditions);
        appendIfPresent(sb, "Test Data", testData);
        appendIfPresent(sb, "Steps to Reproduce", stepsToReproduce);
        appendIfPresent(sb, "Expected Result", expectedResult);
        appendIfPresent(sb, "Actual Result", actualResult);
        appendIfPresent(sb, "Severity", severity);
        appendIfPresent(sb, "Priority", priority);
        appendIfPresent(sb, "Reported By", reportedBy);
        return sb.toString().strip();
    }

    private static void appendIfPresent(StringBuilder sb, String label, String value) {
        if (value != null && !value.isBlank()) {
            sb.append(label).append(": ").append(value).append("\n\n");
        }
    }
}
