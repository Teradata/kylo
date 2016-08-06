/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.policy.validation;


import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validates format
 */
@Validator(name = "Date", description = "Validate Hive-friendly date format")
public class DateValidator implements ValidationPolicy<String> {

    private static Logger log = LoggerFactory.getLogger(DateValidator.class);

    private static final DateValidator instance = new DateValidator();

    private static final DateTimeFormatter DATE = DateTimeFormat.forPattern("yyyy-MM-dd");
    private static final int LENGTH = 10;

    private DateValidator() {
        super();
    }

    public static DateValidator instance() {
        return instance;
    }

    @Override
    public boolean validate(String value) {
        if (!StringUtils.isEmpty(value)) {
            try {
                parseDate(value);
                return true;

            } catch (IllegalArgumentException e) {
                log.debug("Invalid date format [{}]", value);
                return false;
            }
        }
        return false;
    }

    /**
     * Parses the string date and returns the
     */
    public DateTime parseDate(String value) {
        int cnt = value.length();
        if (cnt == LENGTH) {
            return DATE.parseDateTime(value);
        } else {
            throw new IllegalArgumentException("Expecting yyyy-MM-dd");
        }
    }

}