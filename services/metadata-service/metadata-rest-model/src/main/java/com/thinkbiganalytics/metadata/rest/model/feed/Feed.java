/**
 *
 */
package com.thinkbiganalytics.metadata.rest.model.feed;

/*-
 * #%L
 * thinkbig-metadata-rest-model
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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 *
 */
@SuppressWarnings("serial")
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Feed implements Serializable {

    private String id;
    private String systemName;
    private String displayName;
    private String description;
    private String owner;
    private State state;
    private DateTime createdTime;
    // TODO versions
    private FeedPrecondition precondition;
    private Set<FeedSource> sources = new HashSet<>();
    private Set<FeedDestination> destinations = new HashSet<>();
    private Properties properties = new Properties();
    private FeedCategory category;
    private InitializationStatus currentInitStatus;
    /**
     * Feeds that this feed is dependent upon  (parents)
     */
    private Set<Feed> dependentFeeds;
    private Set<String> dependentFeedIds;
    /**
     * Feeds that depend upon this feed (children)
     */
    private Set<Feed> usedByFeeds;
    private Set<String> usedByFeedIds;

    /**
     * Last modified time
     */
    private DateTime modifiedTime;

    public Feed() {
        super();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public DateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(DateTime createdTime) {
        this.createdTime = createdTime;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public FeedPrecondition getPrecondition() {
        return precondition;
    }

    public void setPrecondition(FeedPrecondition trigger) {
        this.precondition = trigger;
    }

    public Set<FeedSource> getSources() {
        return sources;
    }

    public void setSources(Set<FeedSource> sources) {
        this.sources = sources;
    }

    public Set<FeedDestination> getDestinations() {
        return destinations;
    }

    public void setDestinations(Set<FeedDestination> destinations) {
        this.destinations = destinations;
    }

    public FeedDestination getDestination(String datasourceId) {
        for (FeedDestination dest : this.destinations) {
            if (datasourceId.equals(dest.getDatasourceId()) ||
                dest.getDatasource() != null && datasourceId.equals(dest.getDatasource().getId())) {
                return dest;
            }
        }

        return null;
    }

    public FeedCategory getCategory() {
        return category;
    }

    public void setCategory(FeedCategory category) {
        this.category = category;
    }

    public InitializationStatus getCurrentInitStatus() {
        return currentInitStatus;
    }

    public void setCurrentInitStatus(InitializationStatus currentInitStatus) {
        this.currentInitStatus = currentInitStatus;
    }

    public Set<Feed> getDependentFeeds() {
        if (dependentFeeds == null) {
            dependentFeeds = new HashSet<>();
        }
        return dependentFeeds;
    }

    public void setDependentFeeds(Set<Feed> dependentFeeds) {
        this.dependentFeeds = dependentFeeds;
        //mark the inverse relationship
        if (dependentFeeds != null) {
            dependentFeeds.stream().forEach(dependentFeed -> {
                dependentFeed.getUsedByFeeds().add(this);
            });
        }
    }

    public Set<Feed> getUsedByFeeds() {
        if (usedByFeeds == null) {
            usedByFeeds = new HashSet<>();
        }
        return usedByFeeds;
    }

    public Set<String> getDependentFeedIds() {
        return dependentFeedIds;
    }

    public void setDependentFeedIds(Set<String> dependentFeedIds) {
        this.dependentFeedIds = dependentFeedIds;
    }

    public Set<String> getUsedByFeedIds() {
        return usedByFeedIds;
    }

    public void setUsedByFeedIds(Set<String> usedByFeedIds) {
        this.usedByFeedIds = usedByFeedIds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Feed feed = (Feed) o;

        if (id != null ? !id.equals(feed.id) : feed.id != null) {
            return false;
        }
        return !(systemName != null ? !systemName.equals(feed.systemName) : feed.systemName != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (systemName != null ? systemName.hashCode() : 0);
        return result;
    }

    public enum State {ENABLED, DISABLED, DELETED}

    public DateTime getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(DateTime modifiedTime) {
        this.modifiedTime = modifiedTime;
    }
}
