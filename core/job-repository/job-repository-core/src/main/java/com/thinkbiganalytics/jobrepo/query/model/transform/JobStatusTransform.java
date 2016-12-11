package com.thinkbiganalytics.jobrepo.query.model.transform;

import com.thinkbiganalytics.DateTimeUtil;
import com.thinkbiganalytics.jobrepo.query.model.JobStatusCount;
import com.thinkbiganalytics.jobrepo.query.model.JobStatusCountResult;

import org.apache.commons.lang3.time.DateUtils;
import org.joda.time.Period;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

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


    /**
     * Enusre that the list contains a date matching Now - the Period.  if not add it to the collection
     */
    public static void ensureDateFromPeriodExists(List<JobStatusCount> jobStatusCounts, Period period) {

        //add in the very first date relative to the period if it doesnt exist with a count of 0
        if (jobStatusCounts != null && !jobStatusCounts.isEmpty()) {
            //get the first min date in the result set
            Date firstDateInResultSet = jobStatusCounts.stream().map(jobStatusCount -> jobStatusCount.getDate()).min(Date::compareTo).get();

            Date firstDate = DateUtils.truncate(DateTimeUtil.getNowUTCTime().minus(period).toDate(), Calendar.DATE);
            boolean containsFirstDate = jobStatusCounts.stream().anyMatch(jobStatusCount -> jobStatusCount.getDate().equals(firstDate));
            if (!containsFirstDate) {
                JobStatusCount first = jobStatusCounts.get(0);
                JobStatusCount min = new JobStatusCountResult(first);
                min.setDate(firstDate);
                min.setCount(new Long(0));
                jobStatusCounts.add(min);
            }
        }

    }

}
