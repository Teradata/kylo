package com.thinkbiganalytics.nifi.core.api.metadata;

import java.util.Date;

/**
 * Represents  an incremental batch load
 */
@Deprecated
public interface BatchLoadStatus {

    Long getBatchId();

    Date getLastLoadDate();

    void setLastLoadDate(Date date);

}
