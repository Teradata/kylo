package com.thinkbiganalytics.jpa;

import org.jadira.usertype.spi.shared.AbstractVersionableUserType;
import org.joda.time.DateTime;

/**
 * Created by sr186054 on 12/7/16.
 */
public class PersistentDateTimeAsMillisLong extends AbstractVersionableUserType<DateTime, Long, LongColumnDateTimeMapper> {

    private static final long serialVersionUID = 2654706404517200613L;

    public PersistentDateTimeAsMillisLong() {
    }

    public int compare(Object o1, Object o2) {
        return ((DateTime) o1).compareTo((DateTime) o2);
    }
}
