/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.com.thinkbiganalytics.policy.standardization;


import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

public enum ISODateTimeFormats {
  DATE_ONLY(ISODateTimeFormat.date()),
  DATE_TIME_NO_MILLIS(ISODateTimeFormat.dateTimeNoMillis()),
  DATE_TIME(ISODateTimeFormat.dateTime());

  private final DateTimeFormatter formatter;

  ISODateTimeFormats(DateTimeFormatter formatter) {
    this.formatter = formatter;
  }

  public DateTimeFormatter getFormatter() {
    return this.formatter;
  }
}

