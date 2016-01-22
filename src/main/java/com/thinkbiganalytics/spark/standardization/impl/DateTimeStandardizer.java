/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.spark.standardization.impl;

import com.thinkbiganalytics.spark.standardization.StandardizationPolicy;
import org.apache.commons.lang.Validate;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.io.IOException;


/**
 * Convert date time to an ISO8601 format used by Hive
 */
public class DateTimeStandardizer implements StandardizationPolicy {

    public enum OutputFormats { DATE_ONLY, DATETIME, DATETIME_NOMILLIS };

    private OutputFormats outputFormat;

    private transient DateTimeFormatter outputFormatter;

    private transient DateTimeFormatter formatter;

    private String inputDateFormat;

    private boolean valid;

    private boolean useUtc;

    private int errCount = 0;

    public DateTimeStandardizer(String inputDateFormat, OutputFormats outputFormat) {

        Validate.notEmpty(inputDateFormat);
        Validate.notNull(outputFormat);
        this.inputDateFormat = inputDateFormat;
        this.outputFormat = outputFormat;
        initializeFormatters();
    }

    @Override
    public String convertValue(String value) {
        if (!valid) return value;
        try {
            DateTime dt = formatter.withZoneUTC().parseDateTime(value);
            return outputFormatter.print(dt);

        } catch (IllegalArgumentException e) {
            // Don't overload logs with errors
            if (errCount++ < 10) {
                System.out.println("Failed to convert string [" + value + "] to date pattern [" + inputDateFormat + "]");
            }
        }
        return value;
    }

    private void initializeFormatters() {
        try {
            valid = false;
            switch (outputFormat) {
                case DATE_ONLY:
                    this.outputFormatter = ISODateTimeFormat.date();
                    break;
                case DATETIME:
                    this.outputFormatter = ISODateTimeFormat.dateTime();
                    break;

                case DATETIME_NOMILLIS:
                    this.outputFormatter = ISODateTimeFormat.dateTimeNoMillis();
                    break;
            }
            this.formatter = DateTimeFormat.forPattern(this.inputDateFormat);
            valid = true;
        } catch (IllegalArgumentException e) {
            System.out.println("Illegal date parser format [" + inputDateFormat + "]. Standardizer will be skipped.");
        }
    }

    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        initializeFormatters();
    }

    public static void main(String[] args) {
        DateTimeStandardizer standardizer = new DateTimeStandardizer("MM/dd/YYYY", OutputFormats.DATE_ONLY);

        System.out.println(standardizer.convertValue("1/14/1974"));
        System.out.println(standardizer.convertValue("1/1/1974"));
        System.out.println(standardizer.convertValue("12/01/2014"));

        standardizer = new DateTimeStandardizer("MM/dd/YYYY HH:mm:ss", OutputFormats.DATETIME_NOMILLIS);
        System.out.println(standardizer.convertValue("1/14/1974 6:00:00"));

        standardizer = new DateTimeStandardizer("MM/dd/YYYY HH:mm:ss", OutputFormats.DATETIME);
        System.out.println(standardizer.convertValue("1/14/1974 6:00:00"));
        standardizer = new DateTimeStandardizer("MM/dd/YYYY HH:mm:ss Z", OutputFormats.DATETIME);
        System.out.println(standardizer.convertValue("1/14/1974 6:25:12 -0800"));
        standardizer = new DateTimeStandardizer("MM/dd/YYYY HH:mm:ss Z", OutputFormats.DATETIME);
        System.out.println(standardizer.convertValue("1/14/1974 6:00:00 -0800"));
    }

}
