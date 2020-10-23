package eu.europa.ec.itb.kohesio.rules;

import eu.europa.ec.itb.kohesio.model.ReportItem;
import eu.europa.ec.itb.kohesio.model.ViolationLevel;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.util.List;

/**
 * Rule to check that at least one of the location indicator fields is provided.
 * This rule will be verified for any provided field values. Formatting errors will be separately reported as part
 * of schema field checks.
 */
public class LocationIndicatorRule implements Rule {

    private static final String LOCATION_INDICATOR_POSTCODE = "Location_Indicator_Postcode";
    private static final String LOCATION_INDICATOR_NUTS_CODE = "Location_Indicator_NUTS_code";
    private static final String LOCATION_INDICATOR_LATITUDE_LONGITUDE = "Location_Indicator_latitude_longitude";

    private final List<String> fieldsToCheck = List.of(
            LOCATION_INDICATOR_POSTCODE,
            LOCATION_INDICATOR_NUTS_CODE,
            LOCATION_INDICATOR_LATITUDE_LONGITUDE
    );

    @Override
    public boolean isApplicable(CSVParser parser) {
        return parser.getHeaderMap().keySet().containsAll(fieldsToCheck);
    }

    @Override
    public void validate(CSVRecord record, long lineNumber, List<ReportItem> aggregatedErrors) {
        boolean valueDefined = false;
        for (String fieldToCheck: fieldsToCheck) {
            String value = null;
            if (record.isSet(fieldToCheck)) {
                value = record.get(fieldToCheck);
            }
            if (value != null && !value.isBlank()) {
                valueDefined = true;
                break;
            }
        }
        if (!valueDefined) {
            aggregatedErrors.add(new ReportItem(String.format("At least one of the location indicator fields %s must be provided.", fieldsToCheck), null, lineNumber, null, ViolationLevel.ERROR));
        }
    }

}
