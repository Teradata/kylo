package com.thinkbiganalytics.metadata.jpa.jobrepo.nifi;

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
import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeed;
import com.thinkbiganalytics.metadata.api.jobrepo.nifi.NifiFeedStats;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "NIFI_FEED_STATS")
public class JpaNifiFeedStats implements NifiFeedStats {

    @Id
    @Column(name = "FEED_NAME")
    private String feedName;

    @Column(name = "FEED_ID")
    private OpsManagerFeedId feedId;

    @Column(name = "RUNNING_FEED_FLOWS")
    private Long runningFeedFlows;

    @Column(name = "TIME")
    private Long time;

    @Column(name = "LAST_ACTIVITY_TIMESTAMP")
    private Long lastActivityTimestamp;

    @Transient
    private boolean isStream;

    public JpaNifiFeedStats() {

    }

    public JpaNifiFeedStats(String feedName) {
        this.feedName = feedName;
    }

    public JpaNifiFeedStats(String feedName, OpsManagerFeedId feedId) {
        this(feedName);
        this.feedId = feedId;
    }

    @Override
    public String getFeedName() {
        return feedName;
    }

    @Override
    public OpsManagerFeedId getFeedId() {
        return feedId;
    }

    public void setFeedId(OpsManagerFeedId feedId) {
        this.feedId = feedId;
    }

    @Override
    public Long getRunningFeedFlows() {
        if(runningFeedFlows == null){
            runningFeedFlows = 0L;
        }
        return runningFeedFlows;
    }

    public void setRunningFeedFlows(Long runningFeedFlows) {
        this.runningFeedFlows = runningFeedFlows;
    }

    public void addRunningFeedFlows(Long runningFeedFlows) {
        if (runningFeedFlows != null) {
            if (this.runningFeedFlows == null) {
                this.runningFeedFlows = 0L;
            }
            this.runningFeedFlows += runningFeedFlows;
        }
    }

    @Override
    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public void setFeedName(String feedName) {
        this.feedName = feedName;
    }

    public Long getLastActivityTimestamp() {
        return lastActivityTimestamp;
    }

    public void setLastActivityTimestamp(Long lastActivityTimestamp) {
        this.lastActivityTimestamp = lastActivityTimestamp;
    }

    public boolean isStream() {
        return isStream;
    }

    public void setStream(boolean stream) {
        isStream = stream;
    }

    @Embeddable
    public static class OpsManagerFeedId extends BaseJpaId implements Serializable, OpsManagerFeed.ID {

        private static final long serialVersionUID = 6017751710414995750L;

        @Column(name = "FEED_ID")
        private UUID uuid;

        public OpsManagerFeedId() {
        }


        public OpsManagerFeedId(Serializable ser) {
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
