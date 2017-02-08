package com.thinkbiganalytics.nifi.provenance.model.stats;

/*-
 * #%L
 * thinkbig-nifi-provenance-model
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


import com.thinkbiganalytics.nifi.provenance.model.FeedFlowFile;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 */
public class GroupedStats extends BaseStatistics implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(GroupedStats.class);

    /**
     * Unique Key for this grouping of events
     */
    private String groupKey;
    /**
     * Min Time for the events in this group
     */
    private DateTime minTime;
    /**
     * Max time for the events in this group
     */
    private DateTime maxTime;

    public GroupedStats() {

    }

    public void add(ProvenanceEventRecordDTO event) {

        FeedFlowFile feedFlowFile = event.getFeedFlowFile();
        this.bytesIn = event.getInputContentClaimFileSizeBytes() != null ? event.getInputContentClaimFileSizeBytes() : 0L;
        this.bytesOut += event.getOutputContentClaimFileSizeBytes() != null ? event.getOutputContentClaimFileSizeBytes() : 0L;
        this.duration += event.getEventDuration() != null ? event.getEventDuration() : 0L;
        this.processorsFailed += event.isFailure() ? 1L : 0L;
        this.flowFilesStarted += event.isStartOfFlowFile() ? 1L : 0L;
        this.flowFilesFinished += event.isEndingFlowFileEvent() ? 1L : 0L;
        this.jobsStarted += feedFlowFile.getFirstEventId().equals(event.getEventId()) ? 1L : 0L;
        if (event.isEndOfJob()) {
            this.jobsFinished += 1L;
            Long jobTime = feedFlowFile.calculateJobDuration(event);
            this.jobDuration += jobTime;
            if (feedFlowFile.hasFailedEvents()) {
                this.jobsFailed += 1L;
            } else {
                this.successfulJobDuration += jobTime;
            }
        }
        if (this.time == null) {
            this.time = event.getEventTime();
        }

        if (this.minTime == null) {
            this.minTime = event.getEventTime();
        }

        if (this.maxTime == null) {
            this.maxTime = event.getEventTime();
        }
        this.maxTime = (event.getEventTime()).isAfter(this.maxTime) ? event.getEventTime() : this.maxTime;
        this.minTime = (event.getEventTime()).isBefore(this.minTime) ? event.getEventTime() : this.minTime;
        this.time = this.minTime;
        if (this.maxEventId < event.getEventId()) {
            this.maxEventId = event.getEventId();
        }

        if (StringUtils.isBlank(this.clusterNodeAddress)) {
            this.clusterNodeAddress = event.getClusterNodeAddress();
        }

        if (StringUtils.isBlank(this.clusterNodeId)) {
            this.clusterNodeId = event.getClusterNodeId();
        }

        this.totalCount++;
    }

    public DateTime getMinTime() {
        return minTime;
    }

    public DateTime getMaxTime() {
        return maxTime;
    }

    public String getGroupKey() {
        return groupKey;
    }

    public void setGroupKey(String groupKey) {
        this.groupKey = groupKey;
    }

    public void clear() {
        super.clear();
        this.groupKey = null;
        this.maxTime = null;
        this.minTime = null;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("GroupedStats{");
        sb.append("jobsFinished=").append(getJobsFinished());
        sb.append('}');
        return sb.toString();
    }
}
