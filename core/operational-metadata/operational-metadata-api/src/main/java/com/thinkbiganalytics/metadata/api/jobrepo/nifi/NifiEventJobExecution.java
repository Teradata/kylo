package com.thinkbiganalytics.metadata.api.jobrepo.nifi;

import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecution;

/**
 * Created by sr186054 on 9/18/16.
 */
public interface NifiEventJobExecution {

    BatchJobExecution getJobExecution();

    void setEventId(Long eventId);

    Long getEventId();
}
