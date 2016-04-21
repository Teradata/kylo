/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.metadata;

import java.util.Date;

/**
 * Represents  an incremental batch load
 */
public interface BatchLoadStatus {

    Long getBatchId();

    Date getLastLoadDate();

}
