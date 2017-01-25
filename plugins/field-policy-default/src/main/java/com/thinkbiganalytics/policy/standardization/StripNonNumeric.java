package com.thinkbiganalytics.policy.standardization;


/**
 * Strips any non-digit value other than decimal
 */
@Standardizer(name = "Strip Non Numeric", description = "Remove any characters that are not numeric")
public class StripNonNumeric extends SimpleRegexReplacer {

    private static final StripNonNumeric instance = new StripNonNumeric();

    private StripNonNumeric() {
        super("[^\\d.]", "");
    }

    @Override
    public String convertValue(String value) {
        return super.convertValue(value);
    }

    public static StripNonNumeric instance() {
        return instance;
    }
}
