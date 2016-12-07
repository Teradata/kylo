package com.thinkbiganalytics.metadata.api.jobrepo.job;

import org.joda.time.DateTime;


/**
 * Created by sr186054 on 12/2/16.
 */
public interface JobStatusCount {

    Long getCount();

    void setCount(Long count);

    String getFeedName();

    void setFeedName(String feedName);

    String getJobName();

    void setJobName(String jobName);

    String getStatus();

    void setStatus(String status);

    DateTime getDate();

    void setDate(DateTime date);

}
