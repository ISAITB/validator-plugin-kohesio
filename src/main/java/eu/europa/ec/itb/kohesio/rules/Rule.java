package eu.europa.ec.itb.kohesio.rules;

import eu.europa.ec.itb.kohesio.model.ReportItem;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.util.List;

/**
 * Common interface for validation rules.
 *
 * Each implementing class will need to be registered with the PluginInterface class.
 */
public interface Rule {

    /**
     * Check the CSV parser to see if the current rule can be executed.
     * This is used to avoid checks per record that can be determined once.
     *
     * @param parser The parser.
     * @return Whether or not the rule should be considered as applicable.
     */
    default boolean isApplicable(CSVParser parser) {
        return true;
    }

    /**
     * Validate the provided CSV record and record any applicable errors.
     *
     * @param record The record to validate.
     * @param lineNumber The line number to consider when reporting errors.
     * @param aggregatedErrors The currently aggregated errors to which new ones should be added.
     */
    void validate(CSVRecord record, long lineNumber, List<ReportItem> aggregatedErrors);

}
