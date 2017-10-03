package com.thinkbiganalytics.jobrepo.query.model;

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

import org.joda.time.DateTime;

import java.io.Serializable;

/**
 * Represents information for a feed and its Job status in a JSON friendly format.
 *
 * @see com.thinkbiganalytics.jobrepo.query.model.transform.FeedModelTransform
 */
@SuppressWarnings("UnusedDeclaration")
public class DefaultExecutedFeed implements Serializable, ExecutedFeed {

    private static final long serialVersionUID = 2227858119326404976L;
    private String name;
    private Long feedInstanceId;
    private Long feedExecutionId;
    private DateTime endTime;
    private String exitCode;
    private String exitStatus;
    private DateTime startTime;
    private ExecutionStatus status;
    private Long runTime;
    private Long timeSinceEndTime;

    public DefaultExecutedFeed() {

    }

    public DefaultExecutedFeed(ExecutedFeed feed) {
        this.name = feed.getName();
        this.feedInstanceId = feed.getFeedInstanceId();
        this.feedExecutionId = feed.getFeedExecutionId();
        this.endTime = feed.getEndTime();
        this.exitCode = feed.getExitCode();
        this.exitStatus = feed.getExitStatus();
        this.startTime = feed.getStartTime();
        this.status = feed.getStatus();
        this.runTime = feed.getRunTime();
        this.timeSinceEndTime = feed.getTimeSinceEndTime();
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }


    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Long getFeedInstanceId() {
        return feedInstanceId;
    }

    @Override
    public void setFeedInstanceId(Long feedInstanceId) {
        this.feedInstanceId = feedInstanceId;
    }

    @Override
    public Long getFeedExecutionId() {
        return feedExecutionId;
    }

    @Override
    public void setFeedExecutionId(Long feedExecutionId) {
        this.feedExecutionId = feedExecutionId;
    }

    @Override
    public DateTime getEndTime() {
        return endTime;
    }

    @Override
    public void setEndTime(DateTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public String getExitCode() {
        return exitCode;
    }

    @Override
    public void setExitCode(String exitCode) {
        this.exitCode = exitCode;
    }

    @Override
    public String getExitStatus() {
        return exitStatus;
    }

    @Override
    public void setExitStatus(String exitStatus) {
        this.exitStatus = exitStatus;
    }

    @Override
    public DateTime getStartTime() {
        return startTime;
    }

    @Override
    public void setStartTime(DateTime startTime) {
        this.startTime = startTime;
    }

    @Override
    public ExecutionStatus getStatus() {
        return status;
    }

    @Override
    public void setStatus(ExecutionStatus status) {
        this.status = status;
    }

    @Override
    public Long getRunTime() {
        return runTime;
    }

    @Override
    public void setRunTime(Long runTime) {
        this.runTime = runTime;
    }

    @Override
    public Long getTimeSinceEndTime() {
        return timeSinceEndTime;
    }

    @Override
    public void setTimeSinceEndTime(Long timeSinceEndTime) {
        this.timeSinceEndTime = timeSinceEndTime;
    }

}
