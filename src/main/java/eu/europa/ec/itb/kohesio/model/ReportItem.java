package eu.europa.ec.itb.kohesio.model;

/**
 * Class representing an item to include in the validation report.
 */
public class ReportItem {

    private final String message;
    private final String fieldName;
    private final String value;
    private final long lineNumber;
    private final ViolationLevel violationLevel;

    /**
     * Constructor.
     *
     * @param message The item's message (must be always provided).
     * @param fieldName The field that this error relates to (may be null if there is no such single field).
     * @param lineNumber The input content's line number the error relates to.
     * @param value The input value that caused the error (may be null).
     * @param violationLevel The violation level for this report item.
     */
    public ReportItem(String message, String fieldName, long lineNumber, String value, ViolationLevel violationLevel) {
        if (message == null || violationLevel == null) {
            throw new IllegalArgumentException("Required parameters missing");
        }
        this.message = message;
        this.fieldName = fieldName;
        this.lineNumber = lineNumber;
        this.value = value;
        this.violationLevel = violationLevel;
    }

    /**
     * Get the formatted message for the resulting report.
     *
     * @return The message.
     */
    public String getReportMessage() {
        if (fieldName == null) {
            return String.format("[Row: %s]: %s", lineNumber, getMessage());
        } else {
            return String.format("[Row: %s][Field: %s]: %s", lineNumber, fieldName, getMessage());
        }
    }

    public String getMessage() {
        return message;
    }

    public long getLineNumber() {
        return lineNumber;
    }

    public String getValue() {
        return value;
    }

    public ViolationLevel getViolationLevel() {
        return violationLevel;
    }
}
