/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.com.thinkbiganalytics.validation;


import com.thinkbiganalytics.policies.validation.FieldValidator;
import com.thinkbiganalytics.policies.validation.Validator;

/**
 * Validates US phone numbers
 */
@FieldValidator(name = "US Phone", description = "Validate US Phone")
public class USPhoneValidator extends RegexValidator implements Validator<String> {

    private static final USPhoneValidator instance = new USPhoneValidator();

    private USPhoneValidator() {
        super("^\\s*(?:\\+?(\\d{1,3}))?[-. (]*(\\d{3})[-. )]*(\\d{3})[-. ]*(\\d{4})(?: *x(\\d+))?\\s*$");
    }

    public static USPhoneValidator instance() {
        return instance;
    }

}
