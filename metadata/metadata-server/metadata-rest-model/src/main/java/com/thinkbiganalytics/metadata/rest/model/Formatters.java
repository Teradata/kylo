/**
 * 
 */
package com.thinkbiganalytics.metadata.rest.model;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.joda.time.format.PeriodFormat;
import org.joda.time.format.PeriodFormatter;

/**
 *
 * @author Sean Felten
 */
public interface Formatters {

    static final PeriodFormatter PERIOD_FORMATTER = PeriodFormat.getDefault();
    static final DateTimeFormatter TIME_FORMATTER = ISODateTimeFormat.dateTimeParser();
    static final DateTimeFormatter TIME_FORMATTER_NO_MILLIS = ISODateTimeFormat.dateTimeNoMillis();
    
    static final DateTimeFormatter[] TIME_FORMATTERS = new DateTimeFormatter[] { TIME_FORMATTER_NO_MILLIS, TIME_FORMATTER };
    
    static DateTime parseDateTime(String timeStr) {
        IllegalArgumentException lastEx = null;
        
        for (DateTimeFormatter formatter : TIME_FORMATTERS) {
            try {
                return formatter.parseDateTime(timeStr);
            } catch (IllegalArgumentException e) {
                lastEx = e;
            }
        }
        
        throw lastEx;
    }
}
