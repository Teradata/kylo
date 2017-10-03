package com.thinkbiganalytics.jobrepo.query.model.transform;

/*-
 * #%L
 * thinkbig-job-repository-core
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.thinkbiganalytics.DateTimeUtil;
import com.thinkbiganalytics.jobrepo.query.model.JobStatusCount;
import com.thinkbiganalytics.jobrepo.query.model.JobStatusCountResult;

import org.apache.commons.lang3.time.DateUtils;
import org.joda.time.Period;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 */
public class JobStatusTransform {

    public static JobStatusCount jobStatusCount(com.thinkbiganalytics.metadata.api.jobrepo.job.JobStatusCount domain) {
        JobStatusCount count = new JobStatusCountResult();
        count.setCount(domain.getCount());
        if (domain.getDate() != null) {
            count.setDate(domain.getDate().toDate());
        }
        count.setFeedName(domain.getFeedName());
        count.setFeedId(domain.getFeedId());
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
