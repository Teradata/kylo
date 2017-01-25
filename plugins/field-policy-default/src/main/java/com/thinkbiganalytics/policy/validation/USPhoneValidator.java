package com.thinkbiganalytics.policy.validation;


/**
 * Validates US phone numbers
 */
@Validator(name = "US Phone", description = "Validate US Phone")
public class USPhoneValidator extends RegexValidator implements ValidationPolicy<String> {

    private static final USPhoneValidator instance = new USPhoneValidator();

    private USPhoneValidator() {
        super("^\\s*(?:\\+?(\\d{1,3}))?[-. (]*(\\d{3})[-. )]*(\\d{3})[-. ]*(\\d{4})(?: *x(\\d+))?\\s*$");
    }

    public static USPhoneValidator instance() {
        return instance;
    }

}
