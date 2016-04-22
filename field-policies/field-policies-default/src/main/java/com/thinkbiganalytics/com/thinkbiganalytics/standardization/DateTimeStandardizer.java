/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.com.thinkbiganalytics.standardization;

import com.thinkbiganalytics.policies.PolicyProperty;
import com.thinkbiganalytics.policies.PolicyPropertyRef;
import com.thinkbiganalytics.policies.standardization.StandardizationPolicy;
import com.thinkbiganalytics.policies.standardization.Standardizer;

import org.apache.commons.lang3.Validate;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.io.IOException;
import java.util.Date;


/**
 * Convert date time by a provided input format to an ISO8601 format used by Hive.  If the input format is null, the date is assumed to be
 * epoch time, otherwise the formatting pattern is used to convert the date.
 */
@Standardizer(name = "Date/Time" ,description = "Converts any date to ISO8601")
public class DateTimeStandardizer implements StandardizationPolicy {

    public  enum OutputFormats {DATE_ONLY, DATETIME, DATETIME_NOMILLIS };

    @PolicyProperty(name = "Date Format",hint = "Format Example: MM/DD/YYYY")
    private String inputDateFormat;

    @PolicyProperty(name = "Output Format",hint = "Choose an output format", type = PolicyProperty.PROPERTY_TYPE.select, selectableValues ={"DATE_ONLY","DATETIME","DATETIME_NOMILLIS"})
    private OutputFormats outputFormat;

    private transient DateTimeFormatter outputFormatter;

    private transient DateTimeFormatter inputFormatter;



    private boolean valid;

    private int errCount = 0;


    public DateTimeStandardizer( OutputFormats outputFormat) {
        this(null, outputFormat);
    }

    public DateTimeStandardizer(@PolicyPropertyRef(name = "Date Format")String inputDateFormat, @PolicyPropertyRef(name = "Output Format")OutputFormats outputFormat) {

        Validate.notNull(outputFormat);
        this.inputDateFormat = inputDateFormat;
        this.outputFormat = outputFormat;
        initializeFormatters();
    }

    @Override
    public String convertValue(String value) {
        if (!valid) return value;
        try {
            if (inputFormatter != null) {
                DateTime dt = inputFormatter.parseDateTime(value);
                return outputFormatter.withZoneUTC().print(dt);
            }
            // epoch time
            long lValue = Long.parseLong(value);
            return outputFormatter.withZoneUTC().print(lValue);

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
            if (inputDateFormat != null) {
                this.inputFormatter = DateTimeFormat.forPattern(this.inputDateFormat);
            }
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

    public String getInputDateFormat() {
        return inputDateFormat;
    }

    public OutputFormats getOutputFormat() {
        return outputFormat;
    }

    public static void main(String[] args) {
        DateTimeStandardizer epochStandardizer = new DateTimeStandardizer(OutputFormats.DATETIME);
        System.out.println(epochStandardizer.convertValue((new Date().getTime()) + ""));


        DateTimeStandardizer standardizer = new DateTimeStandardizer("MM/dd/YYYY", OutputFormats.DATE_ONLY);

        System.out.println(standardizer.convertValue("1/14/1974"));
        System.out.println(standardizer.convertValue("1/1/1974"));
        System.out.println(standardizer.convertValue("12/01/2014"));
        System.out.println(standardizer.convertValue("1/14/1974"));

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
