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
import org.joda.time.ReadableInstant;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.joda.time.format.PeriodFormat;
import org.joda.time.format.PeriodFormatter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Defines common time-related formatter's and parsing functionality.
 */
public interface Formatters {

    static final PeriodFormatter PERIOD_FORMATTER = PeriodFormat.getDefault();
    static final DateTimeFormatter ISO_DATE_TIME_FORMATTER = ISODateTimeFormat.dateTimeParser();
    static final DateTimeFormatter ISO_DATE_TIME_FORMATTER_NO_MILLIS = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZZ");
    static final DateTimeFormatter ISO_TIME_FORMATTER = ISODateTimeFormat.timeParser();
    static final DateTimeFormatter ISO_DATE_FORMATTER_NO_MILLIS = ISODateTimeFormat.dateOptionalTimeParser();
    static final DateTimeFormatter ISO_LOCAL_DATE_FORMATTER_NO_MILLIS = ISODateTimeFormat.localDateOptionalTimeParser();
    static final DateTimeFormatter SHORT_TIME_FORMATTER = DateTimeFormat.shortTime();
    static final DateTimeFormatter HMS_TIME_FORMATTER = DateTimeFormat.forPattern("HH:MM");
    static final DateTimeFormatter HM_TIME_FORMATTER = DateTimeFormat.forPattern("HH:MM");
    static final DateTimeFormatter MILLIS_FORMATTER = new MillisDateTimeFormat();

    /**
     * A list of date/time-related formatters in order of most specific to least specific
     */
    static final DateTimeFormatter[] DATE_TIME_FORMATTERS
        = new DateTimeFormatter[]{
        ISO_DATE_TIME_FORMATTER_NO_MILLIS,
        ISO_DATE_TIME_FORMATTER,
        SHORT_TIME_FORMATTER,
        HMS_TIME_FORMATTER,
        HM_TIME_FORMATTER,
        MILLIS_FORMATTER
    };

    /**
     * convert the String to a DateTime field using the defined formatters
     */
    static DateTime parseDateTime(String timeStr) {
        for (DateTimeFormatter formatter : DATE_TIME_FORMATTERS) {
            try {
                return formatter.parseDateTime(timeStr);
            } catch (IllegalArgumentException e) {
            }
        }

        throw new IllegalArgumentException("Date/time cannot be parsed - acceptable examples: " + getDateTimeExamples());
    }

    /**
     * Return the DateTime as a String in ISO format, formatting the string in the format: "yyyy-MM-dd'T'HH:mm:ssZZ"
     *
     * @return the string format as "yyyy-MM-dd'T'HH:mm:ssZZ" of the DateTime object passed in
     */
    static String print(DateTime time) {
        return ISO_DATE_TIME_FORMATTER_NO_MILLIS.print(time);
    }

    /**
     * Return a list of example DateTime formatted Strings
     *
     * @return a list of strings with various formatting
     */
    static Collection<String> getDateTimeExamples() {
        DateTime time = DateTime.now();
        List<String> examples = new ArrayList<>();
        examples.add(ISO_DATE_TIME_FORMATTER_NO_MILLIS.print(time));
        examples.add(SHORT_TIME_FORMATTER.print(time));
        examples.add(HMS_TIME_FORMATTER.print(time));
        examples.add(HM_TIME_FORMATTER.print(time));
        examples.add(MILLIS_FORMATTER.print(time));
        return examples;
    }


    class MillisDateTimeFormat extends DateTimeFormatter {

        public MillisDateTimeFormat() {
            super(null, null);
        }

        @Override
        public DateTime parseDateTime(String text) {
            try {
                return new DateTime(Long.parseLong(text), DateTimeZone.UTC);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(e);
            }
        }

        @Override
        public String print(ReadableInstant instant) {
            return Long.toString(instant.getMillis());
        }
    }
}
