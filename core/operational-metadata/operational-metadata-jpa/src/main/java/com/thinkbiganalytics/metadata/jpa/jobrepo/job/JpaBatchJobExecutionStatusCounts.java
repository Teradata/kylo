package com.thinkbiganalytics.metadata.jpa.jobrepo.job;

/*-
 * #%L
 * thinkbig-operational-metadata-jpa
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


import com.thinkbiganalytics.metadata.api.jobrepo.job.JobStatusCount;

import org.joda.time.DateTime;

/**
 * Created by sr186054 on 12/1/16.
 */
public class JpaBatchJobExecutionStatusCounts  implements JobStatusCount {

    private String feedName;
    private String jobName;
    private String status;
    private DateTime date;
    private Long count;

    public JpaBatchJobExecutionStatusCounts() {

    }

    public JpaBatchJobExecutionStatusCounts(String status, Long count) {
        this.status = status;
        this.count = count;
    }

    public JpaBatchJobExecutionStatusCounts(String status,  Integer year, Integer month, Integer day,Long count) {
        this.status = status;
        this.count = count;
        this.date = new DateTime().withDate(year,month,day);
    }
    public JpaBatchJobExecutionStatusCounts(String status,  String feedName, Integer year, Integer month, Integer day,Long count) {
        this.status = status;
        this.count = count;
        this.feedName = feedName;
        this.date = new DateTime().withDate(year, month, day).withMillisOfDay(0);
    }



    public JpaBatchJobExecutionStatusCounts(JobStatusCount jobStatusCount) {
        this.feedName = jobStatusCount.getFeedName();
        this.jobName = jobStatusCount.getJobName();
        this.status = jobStatusCount.getStatus();
        this.date = jobStatusCount.getDate();
        this.count = jobStatusCount.getCount();
    }

    @Override
    public Long getCount() {
        return count;
    }

    @Override
    public void setCount(Long count) {
        this.count = count;
    }

    @Override
    public String getFeedName() {
        return feedName;
    }

    @Override
    public void setFeedName(String feedName) {
        this.feedName = feedName;
    }

    @Override
    public String getJobName() {
        return jobName;
    }

    @Override
    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    @Override
    public String getStatus() {
        return status;
    }

    @Override
    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public DateTime getDate() {
        return date;
    }

    @Override
    public void setDate(DateTime date) {
        this.date = date;
    }


}
