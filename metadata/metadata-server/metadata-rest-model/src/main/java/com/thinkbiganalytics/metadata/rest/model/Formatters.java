/**
 * 
 */
package com.thinkbiganalytics.metadata.rest.model;

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
    static final DateTimeFormatter TIME_FORMATTER = ISODateTimeFormat.dateTime();
}
