package eu.europa.ec.itb.kohesio;

import com.gitb.core.AnyContent;
import com.gitb.core.ValidationModule;
import com.gitb.tr.ObjectFactory;
import com.gitb.tr.*;
import com.gitb.vs.Void;
import com.gitb.vs.*;
import eu.europa.ec.itb.kohesio.model.ReportItem;
import eu.europa.ec.itb.kohesio.model.ViolationLevel;
import eu.europa.ec.itb.kohesio.rules.LocationIndicatorRule;
import eu.europa.ec.itb.kohesio.rules.OperationDateRule;
import eu.europa.ec.itb.kohesio.rules.Rule;
import eu.europa.ec.itb.kohesio.rules.TotalEligibleExpenditureRule;
import eu.europa.ec.itb.kohesio.util.BomStrippingReader;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.IOException;
import java.io.Reader;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The entry point for the validation plugin's implementation.
 *
 * Validation is driven through the validate method, whereas the getModuleDefinition method is optional but is implemented
 * to improve logging in the overall validator.
 */
public class PluginInterface implements ValidationService {

    private static final String INPUT__CONTENT_TO_VALIDATE = "contentToValidate";
    private static final String INPUT__QUOTE = "quote";
    private static final String INPUT__DELIMITER = "delimiter";
    private static final long MAXIMUM_REPORT_ITEMS = 50000L;

    private final ObjectFactory objectFactory = new ObjectFactory();

    /*
    Define the list of rules to process. This could also be done via a reflection library but there is no real need here.
    In addition, defining them like this we avoid an extra dependency that is not strictly needed.
     */
    private final Rule[] rules = new Rule[] {
            new OperationDateRule(),
            new LocationIndicatorRule(),
            new TotalEligibleExpenditureRule()
    };

    @Override
    public GetModuleDefinitionResponse getModuleDefinition(Void aVoid) {
        GetModuleDefinitionResponse response = new GetModuleDefinitionResponse();
        response.setModule(new ValidationModule());
        response.getModule().setId("KohesioPlugin");
        return response;
    }

    @Override
    public ValidationResponse validate(ValidateRequest request) {
        // Collect the inputs need. These should always be present given that they are defined by the core validator.
        String inputFilePath = request.getInput().stream().filter((input) -> INPUT__CONTENT_TO_VALIDATE.equals(input.getName())).findFirst().orElseThrow(() -> new IllegalArgumentException(String.format("The [%s] input is required", INPUT__CONTENT_TO_VALIDATE))).getValue();
        char quote = request.getInput().stream().filter((input) -> INPUT__QUOTE.equals(input.getName())).findFirst().orElseThrow(() -> new IllegalArgumentException(String.format("The [%s] input is required", INPUT__QUOTE))).getValue().charAt(0);
        char delimiter = request.getInput().stream().filter((input) -> INPUT__DELIMITER.equals(input.getName())).findFirst().orElseThrow(() -> new IllegalArgumentException(String.format("The [%s] input is required", INPUT__DELIMITER))).getValue().charAt(0);
        // Define the syntax options for the CSV file parsing.
        CSVFormat format = CSVFormat.RFC4180
                .withIgnoreHeaderCase(false)
                .withAllowDuplicateHeaderNames(true)
                .withIgnoreSurroundingSpaces(true)
                .withRecordSeparator("\n")
                .withDelimiter(delimiter)
                .withQuote(quote)
                .withHeader();
        // Read the CSV file's records and validate each row.
        final long[] counterErrors = {0L};
        final long[] counterWarnings = {0L};
        final long[] counterInformationMessages = {0L};
        List<ReportItem> errors = new ArrayList<>();
        ViolationReporter reporter = (reportItem) -> {
            if (reportItem.getViolationLevel() == ViolationLevel.ERROR) {
                counterErrors[0] += 1;
            } else if (reportItem.getViolationLevel() == ViolationLevel.WARNING) {
                counterWarnings[0] += 1;
            } else if (reportItem.getViolationLevel() == ViolationLevel.INFO) {
                counterInformationMessages[0] += 1;
            }
            if (errors.size() < MAXIMUM_REPORT_ITEMS) {
                errors.add(reportItem);
            }
        };
        try {
            try (
                    Reader inputReader = new BomStrippingReader(Files.newInputStream(Path.of(inputFilePath)));
                    CSVParser parser = new CSVParser(inputReader, format)
            ) {
                // First determine the rules that are meaningful to call (based on the available input fields).
                List<Rule> rulesToCheck = Arrays.stream(rules).filter((rule) -> rule.isApplicable(parser)).collect(Collectors.toList());
                long previousLineNumber = -1;
                // Validation per row.
                for (CSVRecord record: parser) {
                    // Determine the correct line number.
                    long reportedLineNumber = parser.getCurrentLineNumber();
                    if (reportedLineNumber == previousLineNumber) {
                        // This can come up in the last record if there is no EOL at the end of the file.
                        reportedLineNumber += 1;
                    }
                    // Call all rules for the parsed record.
                    for (Rule rule: rulesToCheck) {
                        rule.validate(record, reportedLineNumber, reporter);
                    }
                    previousLineNumber = reportedLineNumber;
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        ValidationResponse response = new ValidationResponse();
        response.setReport(toTAR(errors, counterErrors[0], counterWarnings[0], counterInformationMessages[0]));
        return response;
    }

    /**
     * Create the current date/time for the resulting report.
     *
     * @return The date/time.
     */
    private XMLGregorianCalendar getXMLGregorianCalendarDateTime() {
        GregorianCalendar calendar = new GregorianCalendar();
        try {
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
        } catch (DatatypeConfigurationException e) {
            throw new IllegalStateException("Unable to construct data type factory for date", e);
        }
    }

    /**
     * Convert the collected error messages to the standard report format that will be processed by the
     * core validator.
     *
     * @param errorMessages The error messages to consider.
     * @return The plugin report.
     */
    private TAR toTAR(List<ReportItem> errorMessages, long counterErrors, long counterWarnings, long counterInformationMessages) {
        TAR report = new TAR();
        report.setDate(getXMLGregorianCalendarDateTime());
        report.setCounters(new ValidationCounters());
        report.setReports(new TestAssertionGroupReportsType());
        report.setContext(new AnyContent());
        if (errorMessages != null) {
            for (ReportItem errorMessage : errorMessages) {
                BAR error = new BAR();
                error.setDescription(errorMessage.getReportMessage());
                error.setLocation(String.format("%s:%s:0", INPUT__CONTENT_TO_VALIDATE, errorMessage.getLineNumber()));
                error.setValue(errorMessage.getValue());
                switch (errorMessage.getViolationLevel()) {
                    case ERROR:
                        report.getReports().getInfoOrWarningOrError().add(objectFactory.createTestAssertionGroupReportsTypeError(error));
                        break;
                    case WARNING:
                        report.getReports().getInfoOrWarningOrError().add(objectFactory.createTestAssertionGroupReportsTypeWarning(error));
                        break;
                    case INFO:
                        report.getReports().getInfoOrWarningOrError().add(objectFactory.createTestAssertionGroupReportsTypeInfo(error));
                        break;
                    case NONE:
                        // Nothing.
                        break;
                }
            }
        }
        if (counterErrors > 0) {
            report.setResult(TestResultType.FAILURE);
        } else if (counterWarnings > 0) {
            report.setResult(TestResultType.WARNING);
        } else {
            report.setResult(TestResultType.SUCCESS);
        }
        report.getCounters().setNrOfErrors(BigInteger.valueOf(counterErrors));
        report.getCounters().setNrOfWarnings(BigInteger.valueOf(counterWarnings));
        report.getCounters().setNrOfAssertions(BigInteger.valueOf(counterInformationMessages));
        return report;
    }

}
