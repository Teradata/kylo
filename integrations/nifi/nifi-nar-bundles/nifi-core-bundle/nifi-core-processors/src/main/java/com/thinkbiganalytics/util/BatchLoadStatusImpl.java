/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.util;

import com.thinkbiganalytics.nifi.core.api.metadata.BatchLoadStatus;

import java.util.Date;

@Deprecated
public class BatchLoadStatusImpl implements BatchLoadStatus {

    private Long batchId;
    private Date lastLoadDate;

    @Override
    public Long getBatchId() {
        return batchId;
    }

    public void setBatchId(long batchId) {
        this.batchId = batchId;
    }

    @Override
    public Date getLastLoadDate() {
        return lastLoadDate;
    }

    public void setLastLoadDate(Date lastLoadDate) {
        this.lastLoadDate = lastLoadDate;
    }

    @Override
    public String toString() {
        return "BatchLoadStatusImpl{" +
                "batchId=" + batchId +
                ", lastLoadDate=" + lastLoadDate +
                '}';
    }
}
