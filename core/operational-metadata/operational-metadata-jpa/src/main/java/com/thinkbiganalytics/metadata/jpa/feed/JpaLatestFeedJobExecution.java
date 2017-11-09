package com.thinkbiganalytics.metadata.jpa.feed;

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

import com.thinkbiganalytics.jpa.BaseJpaId;
import com.thinkbiganalytics.metadata.api.feed.LatestFeedJobExecution;
import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeed;
import com.thinkbiganalytics.metadata.api.jobrepo.ExecutionConstants;
import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecution;
import com.thinkbiganalytics.metadata.jpa.jobrepo.job.JpaBatchJobExecution;

import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 */
@Entity
@Table(name = "LATEST_FINISHED_FEED_JOB_VW")
public class JpaLatestFeedJobExecution implements LatestFeedJobExecution {

    @ManyToOne(targetEntity = JpaOpsManagerFeed.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "FEED_ID", insertable = false, updatable = false)
    private OpsManagerFeed feed;

    private LatestJobExecutionFeedId feedId;

    @Column(name = "FEED_NAME", insertable = false, updatable = false)
    private String feedName;

    @Column(name = "FEED_TYPE", insertable = false, updatable = false)
    String feedType;

    @Id
    @Column(name = "JOB_EXECUTION_ID", insertable = false, updatable = false)
    private Long jobExecutionId;

    @Column(name = "JOB_INSTANCE_ID", insertable = false, updatable = false)
    private Long jobInstanceId;


    @ManyToOne(targetEntity = JpaBatchJobExecution.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "JOB_EXECUTION_ID", insertable = false, updatable = false)
    private BatchJobExecution jobExecution;


    @Type(type = "com.thinkbiganalytics.jpa.PersistentDateTimeAsMillisLong")
    @Column(name = "START_TIME")
    private DateTime startTime;

    @Type(type = "com.thinkbiganalytics.jpa.PersistentDateTimeAsMillisLong")
    @Column(name = "END_TIME")
    private DateTime endTime;


    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", length = 10, nullable = false)
    private BatchJobExecution.JobStatus status = BatchJobExecution.JobStatus.UNKNOWN;


    @Enumerated(EnumType.STRING)
    @Column(name = "EXIT_CODE")
    private ExecutionConstants.ExitCode exitCode = ExecutionConstants.ExitCode.UNKNOWN;

    @Column(name = "EXIT_MESSAGE")
    @Type(type = "com.thinkbiganalytics.jpa.TruncateStringUserType", parameters = {@Parameter(name = "length", value = "2500")})
    private String exitMessage;

    @Column(name = "IS_STREAM", length = 1)
    @org.hibernate.annotations.Type(type = "yes_no")
    private boolean isStream;


    public JpaLatestFeedJobExecution() {

    }

    @Override
    public OpsManagerFeed getFeed() {
        return feed;
    }

    public void setFeed(OpsManagerFeed feed) {
        this.feed = feed;
    }

    @Override
    public String getFeedName() {
        return feedName;
    }

    public void setFeedName(String feedName) {
        this.feedName = feedName;
    }

    @Override
    public BatchJobExecution getJobExecution() {
        return jobExecution;
    }

    public void setJobExecution(BatchJobExecution jobExecution) {
        this.jobExecution = jobExecution;
    }

    @Override
    public DateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(DateTime startTime) {
        this.startTime = startTime;
    }

    @Override
    public DateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(DateTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public BatchJobExecution.JobStatus getStatus() {
        return status;
    }

    public void setStatus(BatchJobExecution.JobStatus status) {
        this.status = status;
    }

    @Override
    public ExecutionConstants.ExitCode getExitCode() {
        return exitCode;
    }

    public void setExitCode(ExecutionConstants.ExitCode exitCode) {
        this.exitCode = exitCode;
    }

    @Override
    public String getExitMessage() {
        return exitMessage;
    }

    public void setExitMessage(String exitMessage) {
        this.exitMessage = exitMessage;
    }

    @Override
    public Long getJobExecutionId() {
        return jobExecutionId;
    }

    public void setJobExecutionId(Long jobExecutionId) {
        this.jobExecutionId = jobExecutionId;
    }

    public Long getJobInstanceId() {
        return jobInstanceId;
    }

    public void setJobInstanceId(Long jobInstanceId) {
        this.jobInstanceId = jobInstanceId;
    }

    public String getFeedType() {
        return feedType;
    }

    public void setFeedType(String feedType) {
        this.feedType = feedType;
    }

    @Override
    public boolean isStream() {
        return isStream;
    }

    public void setStream(boolean stream) {
        isStream = stream;
    }


    @Override
    public String getFeedId() {
        return feedId != null ? feedId.toString() : null;
    }

    public void setFeedId(LatestJobExecutionFeedId feedId) {
        this.feedId = feedId;
    }


    @Embeddable
    public static class LatestJobExecutionFeedId extends BaseJpaId implements Serializable, OpsManagerFeed.ID {

        private static final long serialVersionUID = 6017751710414995750L;

        @Column(name = "FEED_ID",insertable = false, updatable = false)
        private UUID uuid;

        public LatestJobExecutionFeedId() {
        }

        public LatestJobExecutionFeedId(Serializable ser) {
            super(ser);
        }

        @Override
        public UUID getUuid() {
            return this.uuid;
        }

        @Override
        public void setUuid(UUID uuid) {
            this.uuid = uuid;
        }
    }
}
