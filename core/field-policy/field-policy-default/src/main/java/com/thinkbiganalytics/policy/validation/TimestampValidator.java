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
@Validator(name = "Timestamp", description = "Validate Hive-friendly timstamp format")
public class TimestampValidator implements ValidationPolicy<String> {

    private static Logger log = LoggerFactory.getLogger(TimestampValidator.class);

    private static final TimestampValidator instance = new TimestampValidator();

    private static final DateTimeFormatter DATETIME_NANOS = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSSSSSSSS");
    private static final DateTimeFormatter DATETIME_MILLIS = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final DateTimeFormatter DATETIME_NOMILLIS = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
    private static final int MIN_LENGTH = 19;
    private static final int MAX_LENGTH = 29;

    private TimestampValidator() {
        super();
    }

    public static TimestampValidator instance() {
        return instance;
    }

    @Override
    public boolean validate(String value) {
        if (!StringUtils.isEmpty(value)) {
            try {
                parseTimestamp(value);
                return true;

            } catch (IllegalArgumentException e) {
                log.debug("Invalid timestamp format [{}]", value);
                return false;
            }
        }
        return false;
    }

    /**
     * Parses the string date and returns the
     */
    public DateTime parseTimestamp(String value) {
        int cnt = value.length();
        if (cnt < MIN_LENGTH || cnt > MAX_LENGTH) {
            throw new IllegalArgumentException("Unexpected format");
        }
        if (cnt == MIN_LENGTH) {
            return DATETIME_NOMILLIS.parseDateTime(value);
        } else if (cnt == MAX_LENGTH) {
            return DATETIME_NANOS.parseDateTime(value);
        } else {
            return DATETIME_MILLIS.parseDateTime(value);
        }
    }

}
