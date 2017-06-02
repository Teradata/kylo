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


import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 */
public class GroupedStats extends BaseStatistics implements Serializable {

    public static final String DEFAULT_SOURCE_CONNECTION_ID = "DEFAULT";

    private static final Logger log = LoggerFactory.getLogger(GroupedStats.class);

    /**
     * Unique Key for this grouping of events
     */
    private String groupKey;
    /**
     * Min Time for the events in this group
     */
    private Long minTime;
    /**
     * Max time for the events in this group
     */
    private Long maxTime;

    public GroupedStats() {
        super();
        setSourceConnectionIdentifier(DEFAULT_SOURCE_CONNECTION_ID);

    }

    public GroupedStats(String sourceConnectionIdentifier) {
        super();
        setSourceConnectionIdentifier(sourceConnectionIdentifier);

    }

    public GroupedStats(GroupedStats other) {
        super(other);
        this.groupKey = other.groupKey;
        this.minTime = other.minTime;
        this.maxTime = other.maxTime;
    }

    public Long getMinTime() {
        return minTime;
    }

    public void setMinTime(Long minTime) {
        this.minTime = minTime;
    }

    public Long getMaxTime() {
        return maxTime;
    }

    public void setMaxTime(Long maxTime) {
        this.maxTime = maxTime;
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
