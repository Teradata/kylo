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
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;

/**
 * Created by sr186054 on 6/16/17.
 */
public class StreamFeedRunningJobExecution {

    private String feedName;

    private ProvenanceEventRecordDTO lastCompletedEvent;

    private Long jobExecutionId;

    public StreamFeedRunningJobExecution()
    {

    }

    public StreamFeedRunningJobExecution(String feedName, ProvenanceEventRecordDTO lastCompletedEvent, Long jobExecutionId) {
        this.feedName = feedName;
        this.lastCompletedEvent = lastCompletedEvent;
        this.jobExecutionId = jobExecutionId;
    }

    public String getFeedName() {
        return feedName;
    }

    public void setFeedName(String feedName) {
        this.feedName = feedName;
    }

    public ProvenanceEventRecordDTO getLastCompletedEvent() {
        return lastCompletedEvent;
    }

    public void setLastCompletedEvent(ProvenanceEventRecordDTO lastCompletedEvent) {
        this.lastCompletedEvent = lastCompletedEvent;
    }

    public Long getJobExecutionId() {
        return jobExecutionId;
    }

    public void setJobExecutionId(Long jobExecutionId) {
        this.jobExecutionId = jobExecutionId;
    }
}
