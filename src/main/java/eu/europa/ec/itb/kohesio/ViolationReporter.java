package eu.europa.ec.itb.kohesio;

import eu.europa.ec.itb.kohesio.model.ReportItem;

/**
 * Interface for classes that will be collecting report items.
 */
public interface ViolationReporter {

    /**
     * Receive a report item to be recorded in the plugin's report.
     *
     * @param reportItem The item to record.
     */
    void record(ReportItem reportItem);

}
