package com.thinkbiganalytics.policy.standardization;

import com.thinkbiganalytics.policy.PolicyProperty;
import com.thinkbiganalytics.policy.PolicyPropertyRef;
import com.thinkbiganalytics.policy.PolicyPropertyTypes;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.TimeZone;

/**
 * Convert date time by a provided input format to an ISO8601 format used by Hive.  If the input format is null, the date is
 * assumed to be Java epoch time, otherwise the formatting pattern is used to convert the date.
 */
@Standardizer(name = "Date/Time", description = "Converts any date to Hive-friendly format with optional timezone conversion")
public class DateTimeStandardizer implements StandardizationPolicy {

    private static final Logger log = LoggerFactory.getLogger(DateTimeStandardizer.class);

    public enum OutputFormats {DATE_ONLY, DATETIME, DATETIME_NOMILLIS}

    @PolicyProperty(name = "Date Format", hint = "Format Example: MM/dd/YYYY", required = true)
    private String inputDateFormat;

    @PolicyProperty(name = "Output Format", hint = "Choose an output format", type = PolicyPropertyTypes.PROPERTY_TYPE.select,
                    selectableValues = {"DATE_ONLY", "DATETIME", "DATETIME_NOMILLIS"}, required = true)
    private OutputFormats outputFormat = OutputFormats.DATE_ONLY;


    /**
     * Unix timestamp is in seconds.. not ms.  detect if the string has only 10 chars being its in seconds, not ms
     * @param value
     * @return
     */
    private boolean isInputUnixTimestamp(String value){
        return StringUtils.isNotBlank(value) && StringUtils.isNumeric(value) && value.length() == 10;
    }

    /**
     * Whether the reference timezone is encoded in the ISO8601 date or specified as configuration
     */
    @PolicyProperty(name = "Input timezone", hint = "Input timezone (optional)", type = PolicyPropertyTypes.PROPERTY_TYPE.select,
                    selectableValues = {"", "ACT",
                                        "AET",
                                        "AGT",
                                        "ART",
                                        "AST",
                                        "BET",
                                        "BST",
                                        "CAT",
                                        "CNT",
                                        "CST",
                                        "CTT",
                                        "EAT",
                                        "ECT",
                                        "IET",
                                        "IST",
                                        "JST",
                                        "MIT",
                                        "NET",
                                        "NST",
                                        "PLT",
                                        "PNT",
                                        "PRT",
                                        "PST",
                                        "SST",
                                        "UTC",
                                        "VST",
                                        "EST",
                                        "MST",
                                        "HST"}, value = "")
    private String inputTimezone;

    /**
     * Whether the reference timezone is encoded in the ISO8601 date or specified as configuration
     */
    @PolicyProperty(name = "Output timezone", hint = "Targeted timezone (optional)", type = PolicyPropertyTypes.PROPERTY_TYPE.select,
                    selectableValues = {"", "ACT",
                                        "AET",
                                        "AGT",
                                        "ART",
                                        "AST",
                                        "BET",
                                        "BST",
                                        "CAT",
                                        "CNT",
                                        "CST",
                                        "CTT",
                                        "EAT",
                                        "ECT",
                                        "IET",
                                        "IST",
                                        "JST",
                                        "MIT",
                                        "NET",
                                        "NST",
                                        "PLT",
                                        "PNT",
                                        "PRT",
                                        "PST",
                                        "SST",
                                        "UTC",
                                        "VST",
                                        "EST",
                                        "MST",
                                        "HST"}, value = "")
    private String outputTimezone;

    private transient DateTimeFormatter outputFormatter;

    private transient DateTimeFormatter inputFormatter;

    private boolean valid;

    public DateTimeStandardizer(OutputFormats outputFormat) {
        this(null, outputFormat, null, null);
    }

    public DateTimeStandardizer(String inputDateFormat, OutputFormats outputFormat) {
        this(inputDateFormat, outputFormat, null, null);
    }

    public DateTimeStandardizer(@PolicyPropertyRef(name = "Date Format") String inputDateFormat,
                                @PolicyPropertyRef(name = "Output Format") OutputFormats outputFormat,
                                @PolicyPropertyRef(name = "Input Timezone") String inputTimezone,
                                @PolicyPropertyRef(name = "Output Timezone") String outputTimezone) {

        Validate.notNull(outputFormat);
        this.inputDateFormat = inputDateFormat;
        this.outputFormat = outputFormat;
        this.inputTimezone = inputTimezone;
        this.outputTimezone = outputTimezone;
        initializeFormatters();
    }

    @Override
    public String convertValue(String value) {
        if (valid) {
            try {
                if (inputFormatter == null) {
                    if(isInputUnixTimestamp(value)) {
                        //unix timestamp are in seconds
                        long lValue = Long.parseLong(value);
                        lValue *= 1000;
                        return outputFormatter.print(lValue);
                    }
                    else {
                        long lValue = Long.parseLong(value);
                        return outputFormatter.print(lValue);
                    }
                }


                DateTime dt = inputFormatter.parseDateTime(value);

                return outputFormatter.print(dt);

            } catch (IllegalArgumentException e) {
                log.debug("Failed to convert string [{}] to date pattern [{}], value, inputDateFormat");
            }
        }
        return value;
    }

    /**
     * Returns a time formatter for the specified timezone
     *
     * @param format   the current formatter
     * @param timezone the timezone string
     * @return a time formatter for the specified timezone
     */
    protected DateTimeFormatter formatterForTimezone(DateTimeFormatter format, String timezone) {

        if (StringUtils.isEmpty(timezone)) {
            return format;
        }
        if ("UTC".equals(timezone)) {
            return format.withZoneUTC();
        }
        return format.withZone(DateTimeZone.forTimeZone(TimeZone.getTimeZone(timezone)));

    }

    protected void initializeFormatters() {
        try {
            valid = false;
            if (outputFormat == null) {
                outputFormat = OutputFormats.DATE_ONLY;
            }
            switch (outputFormat) {
                case DATE_ONLY:
                    this.outputFormatter = DateTimeFormat.forPattern("yyyy-MM-dd");
                    break;
                case DATETIME:
                    this.outputFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS");
                    break;

                case DATETIME_NOMILLIS:
                    this.outputFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
                    break;
            }
            this.outputFormatter = formatterForTimezone(this.outputFormatter, outputTimezone);
            if (inputDateFormat != null) {
                this.inputFormatter = DateTimeFormat.forPattern(this.inputDateFormat);
                this.inputFormatter = formatterForTimezone(this.inputFormatter, inputTimezone);
            }
            valid = true;
        } catch (IllegalArgumentException e) {
            log.warn("Illegal configuration input format [{}], tz [{}] Output format  [{}], tz [{}]"
                      + "]. Standardizer will be skipped.", inputDateFormat, inputTimezone, outputFormat, outputTimezone);
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

}
