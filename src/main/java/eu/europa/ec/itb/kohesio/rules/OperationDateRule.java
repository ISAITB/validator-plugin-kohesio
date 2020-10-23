package eu.europa.ec.itb.kohesio.rules;

import eu.europa.ec.itb.kohesio.model.ReportItem;
import eu.europa.ec.itb.kohesio.model.ViolationLevel;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Rule to check that the operation start date is always before the operation end date.
 * This rule will only be verified if both values are provided and can be parsed correctly.
 */
public class OperationDateRule implements Rule {

    private static final String OPERATION_START_DATE = "Operation_Start_Date";
    private static final String OPERATION_END_DATE = "Operation_End_Date";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public boolean isApplicable(CSVParser parser) {
        return parser.getHeaderMap().containsKey(OPERATION_START_DATE) && parser.getHeaderMap().containsKey(OPERATION_END_DATE);
    }

    @Override
    public void validate(CSVRecord record, long lineNumber, List<ReportItem> aggregatedErrors) {
        if (record.isSet(OPERATION_START_DATE) && record.isSet(OPERATION_END_DATE)) {
            String startDateStr = record.get(OPERATION_START_DATE);
            LocalDate startDate = parseDate(startDateStr);
            LocalDate endDate = parseDate(record.get(OPERATION_END_DATE));
            if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
                aggregatedErrors.add(new ReportItem(String.format("The operation start date '%s' must be before the operation end date '%s'.", DATE_FORMATTER.format(startDate), DATE_FORMATTER.format(endDate)), OPERATION_START_DATE, lineNumber, startDateStr, ViolationLevel.ERROR));
            }
        }
    }

    private LocalDate parseDate(String dateValueStr) {
        LocalDate result = null;
        if (dateValueStr != null && !dateValueStr.isBlank()) {
            try {
                result = LocalDate.parse(dateValueStr, DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                // Ignore - this is reported as part of schema checks.
            }
        }
        return result;
    }

}
