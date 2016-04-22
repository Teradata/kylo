/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.com.thinkbiganalytics.validation;


import com.thinkbiganalytics.policies.validation.FieldValidator;
import com.thinkbiganalytics.policies.validation.Validator;

/**
 * Validates ISO8601 format
 */
@FieldValidator(name = "Timestamp", description = "Validate ISO8601 format")
public class TimestampValidator extends RegexValidator implements Validator<String> {

    private static final TimestampValidator instance = new TimestampValidator();

    private TimestampValidator() {
        super("((\\d\\d\\d\\d)-?(\\d\\d)-?(\\d\\d)(T|\\s)?(\\d\\d):?(\\d\\d)(?::?(\\d\\d)(\\.\\d+)*?)?(Z|[+-])(?:(\\d\\d):?(\\d\\d))?|20\\d{2}(-|\\/)((0[1-9])|(1[0-2]))(-|\\/)((0[1-9])|([1-2][0-9])|(3[0-1]))(T|\\s)(([0-1][0-9])|(2[0-3])):([0-5][0-9]):([0-5][0-9]))");
    }

    public static TimestampValidator instance() {
        return instance;
    }

}
