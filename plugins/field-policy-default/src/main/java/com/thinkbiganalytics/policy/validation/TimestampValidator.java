/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.policy.validation;


import com.thinkbiganalytics.policy.PolicyProperty;
import com.thinkbiganalytics.policy.PolicyPropertyRef;
import com.thinkbiganalytics.policy.PolicyPropertyTypes;
import com.thinkbiganalytics.policy.PropertyLabelValue;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validates format
 */
@Validator(name = "Timestamp", description = "Validate Hive-friendly timstamp format")
public class TimestampValidator implements ValidationPolicy<String> {

    private static Logger log = LoggerFactory.getLogger(TimestampValidator.class);

    private static final DateTimeFormatter DATETIME_NANOS = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSSSSSSSS");
    private static final DateTimeFormatter DATETIME_MILLIS = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final DateTimeFormatter DATETIME_NOMILLIS = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATETIME_ISO8601 = ISODateTimeFormat.dateTimeParser();

    private static final int MIN_LENGTH = 19;
    private static final int MAX_LENGTH = 29;


    @PolicyProperty(name = "allowNull", value = "false", displayName = "Allow Null Values",
                    hint = "Null values are considered to be valid", type = PolicyPropertyTypes.PROPERTY_TYPE.select,
                    labelValues = {@PropertyLabelValue(label = "Yes", value = "true"),
                                   @PropertyLabelValue(label = "No", value = "false")})
    private boolean allowNull = false;


    public TimestampValidator(@PolicyPropertyRef(name = "allowNull") boolean allowNull) {
        super();
        this.allowNull = allowNull;
    }

    public TimestampValidator() {
        super();
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
        } else {
            return allowNull;
        }
    }

    /**
     * Parses the string date and returns the
     * Sqoop treats null values as \N.
     */
    public DateTime parseTimestamp(String value) {
        // Check if the value is consider a null
        if ((allowNull) && (value.toUpperCase().equals("NULL") || (value.toUpperCase().equals("N")))) {
            return new DateTime();
        }

        int cnt = value.length();
        if (cnt < MIN_LENGTH || cnt > MAX_LENGTH) {
            throw new IllegalArgumentException("Unexpected format");
        }
        if (value.charAt(10) == 'T') {
            return DATETIME_ISO8601.parseDateTime(value);
        } else if (cnt == MIN_LENGTH) {
            return DATETIME_NOMILLIS.parseDateTime(value);
        } else if (cnt == MAX_LENGTH) {
            return DATETIME_NANOS.parseDateTime(value);
        } else {
            return DATETIME_MILLIS.parseDateTime(value);
        }
    }

    public boolean getAllowNull() {
        return allowNull;
    }

    public boolean isAllowNull() {
        return allowNull;
    }

    public void setAllowNull(boolean allowNull) {
        this.allowNull = allowNull;
    }
}
