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

import com.thinkbiganalytics.metadata.api.feed.FeedSummary;
import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeed;
import com.thinkbiganalytics.metadata.api.jobrepo.ExecutionConstants;
import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecution;

import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;


@Entity
@Table(name = "FEED_SUMMARY_VIEW")
@Immutable
public class JpaFeedSummary implements FeedSummary {

    @EmbeddedId
    private JpaFeedSummaryId feedSummaryId;

    @Column(name = "FEED_ID", insertable = false, updatable = false)
    private UUID feedId;

    @Column(name = "FEED_NAME", insertable = false, updatable = false)
    private String feedName;

    @Enumerated(EnumType.STRING)
    @Column(name = "FEED_TYPE")
    private OpsManagerFeed.FeedType feedType = OpsManagerFeed.FeedType.FEED;

    @Column(name = "IS_STREAM", length = 1)
    @org.hibernate.annotations.Type(type = "yes_no")
    private boolean isStream;

    @Column(name = "JOB_EXECUTION_ID", insertable = false, updatable = false)
    private Long jobExecutionId;

    @Column(name = "JOB_INSTANCE_ID", insertable = false, updatable = false)
    private Long jobInstanceId;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "RUN_STATUS", insertable = false, updatable = false)
    private RunStatus runStatus;

    @Column(name = "ALL_COUNT")
    private Long allCount;

    @Column(name = "FAILED_COUNT")
    private Long failedCount;

    @Column(name = "COMPLETED_COUNT")
    private Long completedCount;

    @Column(name = "ABANDONED_COUNT")
    private Long abandonedCount;

    @Column(name = "RUNNING_COUNT", insertable = false, updatable = false)
    private Long runningCount;

    public JpaFeedSummaryId getFeedSummaryId() {
        return feedSummaryId;
    }

    public void setFeedSummaryId(JpaFeedSummaryId feedSummaryId) {
        this.feedSummaryId = feedSummaryId;
    }

    public void setFeedId(UUID feedId) {
        this.feedId = feedId;
    }

    @Override
    public UUID getFeedId() {
        return feedId;
    }

    @Override
    public String getFeedIdAsString() {
        return feedId.toString();
    }

    public void setFeedId(String feedId) {
        this.feedId = UUID.fromString(feedId);
    }

    @Override
    public String getFeedName() {
        return feedName;
    }

    public void setFeedName(String feedName) {
        this.feedName = feedName;
    }

    @Override
    public OpsManagerFeed.FeedType getFeedType() {
        return feedType;
    }

    public void setFeedType(OpsManagerFeed.FeedType feedType) {
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
    public Long getJobExecutionId() {
        return jobExecutionId;
    }

    public void setJobExecutionId(Long jobExecutionId) {
        this.jobExecutionId = jobExecutionId;
    }

    @Override
    public Long getJobInstanceId() {
        return jobInstanceId;
    }

    public void setJobInstanceId(Long jobInstanceId) {
        this.jobInstanceId = jobInstanceId;
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
    public RunStatus getRunStatus() {
        return runStatus;
    }

    public void setRunStatus(RunStatus runStatus) {
        this.runStatus = runStatus;
    }

    public Long getAllCount() {
        return allCount;
    }

    public void setAllCount(Long allCount) {
        this.allCount = allCount;
    }

    public Long getFailedCount() {
        return failedCount;
    }

    public void setFailedCount(Long failedCount) {
        this.failedCount = failedCount;
    }

    public Long getCompletedCount() {
        return completedCount;
    }

    public void setCompletedCount(Long completedCount) {
        this.completedCount = completedCount;
    }

    public Long getAbandonedCount() {
        return abandonedCount;
    }

    public void setAbandonedCount(Long abandonedCount) {
        this.abandonedCount = abandonedCount;
    }

    public Long getRunningCount() {
        return runningCount;
    }

    public void setRunningCount(Long runningCount) {
        this.runningCount = runningCount;
    }

    @Embeddable
    public static class JpaFeedSummaryId implements Serializable, FeedSummary.ID {

        private static final long serialVersionUID = 6017751710414995750L;

        @Column(name = "ID")
        private String id;


        public JpaFeedSummaryId() {
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            JpaFeedSummaryId that = (JpaFeedSummaryId) o;

            return id != null ? id.equals(that.id) : that.id == null;
        }

        @Override
        public int hashCode() {
            return id != null ? id.hashCode() : 0;
        }
    }
}
