package com.thinkbiganalytics;

/*-
 * #%L
 * thinkbig-commons-util
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import org.joda.time.ReadWritablePeriod;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.joda.time.format.PeriodParser;

import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

/**
 * Utility for common DateTime functions
 */
public class DateTimeUtil {

    static PeriodFormatter periodFormatter = new PeriodFormatterBuilder().append(null, new StringPeriodParser()).toFormatter();

    static final PeriodFormatter STANDARD_PERIOD_FORMAT = new PeriodFormatterBuilder()
        .appendDays()
        .appendSuffix(" day", " days")
        .appendSeparator(" ")
        .minimumPrintedDigits(2)
        .appendHours()
        .appendSuffix(" hr ", " hrs ")
        .appendMinutes()
        .minimumPrintedDigits(2)
        .appendSuffix(" min ", " min ")
        .appendSeconds()
        .printZeroIfSupported()
        .minimumPrintedDigits(2)
        .appendSuffix(" sec ", " sec ")
        .toFormatter();

    public static final DateTimeFormatter utcDateTimeFormat = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS").withZoneUTC();

    public static final DateTimeFormatter dateTimeFormatWithTimeZone = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss z");


    public static Date convertToUTC(Date date) {
        DateTime time = new DateTime(date.getTime());
        DateTimeZone dtZone = DateTimeZone.forID("UTC");
        DateTime utc = time.withZone(dtZone);
        return utc.toDate();
    }

    public static DateTime convertToUTC(DateTime date) {
        DateTimeZone dtZone = DateTimeZone.forID("UTC");
        DateTime utc = date.withZone(dtZone);
        return new DateTime(utc);
    }

    public static DateTime convertToUTC(Long time) {
        DateTimeZone dtZone = DateTimeZone.forID("UTC");
        DateTime date = new DateTime(time);
        DateTime utc = date.withZone(dtZone);
        return new DateTime(utc);
    }

    public static Date getUTCTime() {
        return convertToUTC(new Date());
    }

    public static DateTime getNowUTCTime() {
        return convertToUTC(DateTime.now());
    }

    /**
     * @return a Date Time string along with the current time zone
     */
    public static String getNowFormattedWithTimeZone() {
        return dateTimeFormatWithTimeZone.print(DateTime.now());
    }


    public static String formatPeriod(Period period) {
        return period.toString(STANDARD_PERIOD_FORMAT);
    }

    /**
     * Parse a string period into a Joda time period
     * i.e. 3Y, 20W
     *
     * @param period a string period (i.e. 3Y, 20W)
     * @return return the period
     */
    public static Period period(String period) {

        return periodFormatter.parsePeriod(period);
    }

    public static class StringPeriodParser implements PeriodParser {

        private Integer getValue(String part, String unit) {
            String val = part.substring(0, part.indexOf(unit));
            return val.length() > 0 ? Integer.valueOf(val) : 0;
        }

        private DATE_PART getDatePart(String part) {
            if (part.contains(DATE_PART.YEAR.abbreviation)) {
                return DATE_PART.YEAR;
            } else if (part.contains(DATE_PART.MONTH.abbreviation)) {
                return DATE_PART.MONTH;
            } else if (part.contains(DATE_PART.WEEK.abbreviation)) {
                return DATE_PART.WEEK;
            } else if (part.contains(DATE_PART.DAY.abbreviation)) {
                return DATE_PART.DAY;
            } else if (part.contains(DATE_PART.HOUR.abbreviation)) {
                return DATE_PART.HOUR;
            } else if (part.contains(DATE_PART.MINUTE.abbreviation)) {
                return DATE_PART.MINUTE;
            } else if (part.contains(DATE_PART.SECOND.abbreviation)) {
                return DATE_PART.SECOND;
            } else {
                return null;
            }
        }

        private void addToPeriod(ReadWritablePeriod period, String part) {
            DATE_PART datePart = getDatePart(part);
            Integer value = getValue(part, datePart.abbreviation);

            switch (datePart) {
                case YEAR:
                    period.addYears(getValue(part, datePart.abbreviation));
                    break;
                case MONTH:
                    period.addMonths(getValue(part, datePart.abbreviation));
                    break;
                case WEEK:
                    period.addWeeks(getValue(part, datePart.abbreviation));
                    break;
                case DAY:
                    period.addDays(getValue(part, datePart.abbreviation));
                    break;
                case HOUR:
                    period.addHours(getValue(part, datePart.abbreviation));
                    break;
                case MINUTE:
                    period.addMinutes(getValue(part, datePart.abbreviation));
                    break;
                case SECOND:
                    period.addSeconds(getValue(part, datePart.abbreviation));
                    break;
                default:
                    break;
            }
        }

        @Override
        public int parseInto(ReadWritablePeriod period, String periodStr,
                             int position, Locale locale) {
            String parts[] = periodStr.split(" ");
            period.addYears(0);
            period.addMonths(0);
            period.addWeeks(0);
            period.addDays(0);
            period.addHours(0);
            period.addMinutes(0);
            period.addSeconds(0);
            Arrays.asList(parts).stream().forEach(part -> addToPeriod(period, part));
            return periodStr.length();
        }


        enum DATE_PART {
            YEAR("Y"), MONTH("M"), WEEK("W"), DAY("D"), HOUR("h"), MINUTE("m"), SECOND("s");
            private String abbreviation;

            DATE_PART(String abbreviation) {
                this.abbreviation = abbreviation;
            }
        }
    }
}
