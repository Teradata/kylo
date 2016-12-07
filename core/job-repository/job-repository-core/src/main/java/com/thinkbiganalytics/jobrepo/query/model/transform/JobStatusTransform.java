package com.thinkbiganalytics.jobrepo.query.model.transform;

import com.thinkbiganalytics.jobrepo.query.model.JobStatusCount;
import com.thinkbiganalytics.jobrepo.query.model.JobStatusCountResult;

/**
 * Created by sr186054 on 12/2/16.
 */
public class JobStatusTransform {

    public static JobStatusCount jobStatusCount(com.thinkbiganalytics.metadata.api.jobrepo.job.JobStatusCount domain) {
        JobStatusCount count = new JobStatusCountResult();
        count.setCount(domain.getCount());
        if (domain.getDate() != null) {
            count.setDate(domain.getDate().toDate());
        }
        count.setFeedName(domain.getFeedName());
        count.setStatus(domain.getStatus());
        return count;
    }

}
