/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.policy.validation;


/**
 * Validates US phone numbers
 */
@Validator(name = "US Zip", description = "Validate US Zip")
public class USZipCodeValidator extends RegexValidator implements ValidationPolicy<String> {

    private static final USZipCodeValidator instance = new USZipCodeValidator();

    private USZipCodeValidator() {
        super("[0-9]{5}([- /]?[0-9]{4})?$");
    }

    public static USZipCodeValidator instance() {
        return instance;
    }

}