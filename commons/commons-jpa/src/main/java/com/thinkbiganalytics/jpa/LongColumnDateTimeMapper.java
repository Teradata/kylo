package com.thinkbiganalytics.jpa;

import org.jadira.usertype.spi.shared.AbstractVersionableLongColumnMapper;
import org.joda.time.DateTime;
import org.joda.time.Instant;

/**
 * Created by sr186054 on 12/7/16.
 */
public class LongColumnDateTimeMapper extends AbstractVersionableLongColumnMapper<DateTime> {

    private static final long serialVersionUID = 8408450977695192938L;

    public LongColumnDateTimeMapper() {
    }

    public DateTime fromNonNullString(String s) {
        return new Instant(s).toDateTime();
    }

    public DateTime fromNonNullValue(Long value) {
        return new Instant(value).toDateTime();
    }

    public String toNonNullString(DateTime value) {
        return value.toString();
    }

    public Long toNonNullValue(DateTime value) {
        return Long.valueOf(value.getMillis());
    }
}
