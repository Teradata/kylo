package com.thinkbiganalytics.policy.standardization;

/**
 * Removes all control characters from a strings including newlines, tabs
 */
@Standardizer(name = "Control characters", description = "Remove Control Characters")
public class RemoveControlCharsStandardizer extends SimpleRegexReplacer {

    private static final RemoveControlCharsStandardizer instance = new RemoveControlCharsStandardizer();

    private RemoveControlCharsStandardizer() {
        super("\\p{Cc}", "");
    }

    public static RemoveControlCharsStandardizer instance() {
        return instance;
    }


}
