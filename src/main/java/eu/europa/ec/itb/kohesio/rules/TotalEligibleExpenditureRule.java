package eu.europa.ec.itb.kohesio.rules;

import eu.europa.ec.itb.kohesio.model.ReportItem;
import eu.europa.ec.itb.kohesio.model.ViolationLevel;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.util.List;
import java.util.Set;

/**
 * Rule ensuring that the total eligible expenditure exchange rate is provided if the currency is not EUR.
 * This rule will succeed for any value provided for the exchange rate. The format of this as well as value boundaries
 * are enforced through schema field checks.
 */
public class TotalEligibleExpenditureRule implements Rule {

    private static final String EXCHANGE_RATE = "Total_Eligible_Expenditure_Exchange_Rate";
    private static final String EXCHANGE_CURRENCY = "Total_Eligible_Expenditure_Currency";

    private static final Set<String> CURRENCIES_WITH_OPTIONAL_RATE = Set.of("EUR");

    @Override
    public boolean isApplicable(CSVParser parser) {
        return parser.getHeaderMap().containsKey(EXCHANGE_CURRENCY) && parser.getHeaderMap().containsKey(EXCHANGE_RATE);
    }

    @Override
    public void validate(CSVRecord record, long lineNumber, List<ReportItem> aggregatedErrors) {
        String currency = record.get(EXCHANGE_CURRENCY);
        if (currency != null && !currency.isBlank() && !CURRENCIES_WITH_OPTIONAL_RATE.contains(currency)) {
            String rate = record.get(EXCHANGE_RATE);
            if (rate == null || rate.isBlank()) {
                aggregatedErrors.add(new ReportItem(String.format("The total eligible expenditure exchange rate is required for the provided currency '%s'.", currency), EXCHANGE_RATE, lineNumber, null, ViolationLevel.ERROR));
            }
        }
    }

}
