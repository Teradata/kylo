/**
 * 
 */
package com.thinkbiganalytics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.joda.time.format.PeriodFormat;
import org.joda.time.format.PeriodFormatter;

/**
 * Defines common time-related formatters and parsing functionality.
 * 
 * @author Sean Felten
 */
public interface Formatters {

    static final PeriodFormatter PERIOD_FORMATTER = PeriodFormat.getDefault();
    static final DateTimeFormatter ISO_DATE_TIME_FORMATTER = ISODateTimeFormat.dateTimeParser();  // No print
    static final DateTimeFormatter ISO_DATE_TIME_FORMATTER_NO_MILLIS = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZZ");
//    static final DateTimeFormatter ISO_DATE_TIME_FORMATTER_NO_MILLIS = ISODateTimeFormat.dateTimeNoMillis();
    static final DateTimeFormatter ISO_TIME_FORMATTER = ISODateTimeFormat.timeParser();  // No print
    static final DateTimeFormatter ISO_DATE_FORMATTER_NO_MILLIS = ISODateTimeFormat.dateOptionalTimeParser(); // No print
    static final DateTimeFormatter ISO_LOCAL_DATE_FORMATTER_NO_MILLIS = ISODateTimeFormat.localDateOptionalTimeParser(); // No print
    static final DateTimeFormatter SHORT_TIME_FORMATTER = DateTimeFormat.shortTime();
    static final DateTimeFormatter HMS_TIME_FORMATTER = DateTimeFormat.forPattern("HH:MM");
    static final DateTimeFormatter HM_TIME_FORMATTER = DateTimeFormat.forPattern("HH:MM");
    
    /** A list of date/time-related formatters in order of most specific to least specific */
    static final DateTimeFormatter[] DATE_TIME_FORMATTERS 
        = new DateTimeFormatter[] { 
                                    ISO_DATE_TIME_FORMATTER_NO_MILLIS, 
                                    ISO_DATE_TIME_FORMATTER 
                                    };
    
    static DateTime parseDateTime(String timeStr) {
        for (DateTimeFormatter formatter : DATE_TIME_FORMATTERS) {
            try {
                return formatter.parseDateTime(timeStr);
            } catch (IllegalArgumentException e) {
            }
        }
        
        throw new IllegalArgumentException("Date/time cannot be parsed - acceptable examples: " + getDateTimeExamples());
    }
    
    static String print(DateTime time) {
        return ISO_DATE_TIME_FORMATTER_NO_MILLIS.print(time);
    }
    
    static Collection<String> getDateTimeExamples() {
        DateTime time = DateTime.now();
        List<String> examples = new ArrayList<>();
        examples.add(ISO_DATE_TIME_FORMATTER_NO_MILLIS.print(time));
        examples.add(SHORT_TIME_FORMATTER.print(time));
        examples.add(HMS_TIME_FORMATTER.print(time));
        examples.add(HM_TIME_FORMATTER.print(time));
        return examples;
    }
}
