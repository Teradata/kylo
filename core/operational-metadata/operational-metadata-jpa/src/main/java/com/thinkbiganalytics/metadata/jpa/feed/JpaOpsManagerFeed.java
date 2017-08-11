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

import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeed;
import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobInstance;
import com.thinkbiganalytics.metadata.jpa.jobrepo.job.JpaBatchJobInstance;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedStoredProcedureQueries;
import javax.persistence.NamedStoredProcedureQuery;
import javax.persistence.OneToMany;
import javax.persistence.ParameterMode;
import javax.persistence.StoredProcedureParameter;
import javax.persistence.Table;

/**
 * This entity is used to Map the Operational Feed Data with the Modeshape JCR feed data. The ID here maps directly to the JCR Modeshape Feed.ID
 * Two stored procedures are mapped here to delete jobs for a given feed, and abandon all failed jobs for a feed.
 */
@Entity
@Table(name = "FEED")
@NamedStoredProcedureQueries({
                                 @NamedStoredProcedureQuery(name = "OpsManagerFeed.deleteFeedJobs", procedureName = "delete_feed_jobs", parameters = {
                                     @StoredProcedureParameter(mode = ParameterMode.IN, name = "category", type = String.class),
                                     @StoredProcedureParameter(mode = ParameterMode.IN, name = "feed", type = String.class),
                                     @StoredProcedureParameter(mode = ParameterMode.OUT, name = "result", type = Integer.class)
                                 }),
                                 @NamedStoredProcedureQuery(name = "OpsManagerFeed.abandonFeedJobs", procedureName = "abandon_feed_jobs", parameters = {
                                     @StoredProcedureParameter(mode = ParameterMode.IN, name = "feed", type = String.class),
                                     @StoredProcedureParameter(mode = ParameterMode.IN, name = "exitMessage", type = String.class),
                                     @StoredProcedureParameter(mode = ParameterMode.IN, name = "username", type = String.class),
                                     @StoredProcedureParameter(mode = ParameterMode.OUT, name = "res", type = Integer.class)
                                 })
                             })
public class JpaOpsManagerFeed implements OpsManagerFeed {

    @EmbeddedId
    private OpsManagerFeedId id;

    @Column(name = "name", length = 100, unique = true, nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "FEED_TYPE")
    private FeedType feedType = FeedType.FEED;

    @Column(name = "IS_STREAM", length = 1)
    @org.hibernate.annotations.Type(type = "yes_no")
    private boolean isStream;

    @Column(name =" TIME_BTWN_BATCH_JOBS")
    private Long timeBetweenBatchJobs;

    @OneToMany(targetEntity = JpaBatchJobInstance.class, mappedBy = "feed", fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<BatchJobInstance> jobInstances = new HashSet<>();


    /**
     * The FEED_CHECK_DATA_FEEDS is a many to many table linking a Feed to any other feeds which are registered to check the data of the related feed.
     */
    @ManyToMany(targetEntity = JpaOpsManagerFeed.class, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "FEED_CHECK_DATA_FEEDS",
               joinColumns = {@JoinColumn(name = "FEED_ID")},
               inverseJoinColumns = {@JoinColumn(name = "CHECK_DATA_FEED_ID")})
    private Set<OpsManagerFeed> checkDataFeeds = new HashSet<OpsManagerFeed>();

    @ManyToMany(targetEntity = JpaOpsManagerFeed.class, mappedBy = "checkDataFeeds")
    private Set<OpsManagerFeed> feedsToCheck = new HashSet<OpsManagerFeed>();

    public JpaOpsManagerFeed(OpsManagerFeed.ID id, String name) {
        this.id = (OpsManagerFeedId) id;
        this.name = name;
    }

    public JpaOpsManagerFeed() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public OpsManagerFeedId getId() {
        return id;
    }

    public void setId(OpsManagerFeedId id) {
        this.id = id;
    }

    public FeedType getFeedType() {
        return feedType;
    }

    public void setFeedType(FeedType feedType) {
        this.feedType = feedType;
    }

    public Set<BatchJobInstance> getJobInstances() {
        return jobInstances;
    }

    public void setJobInstances(Set<BatchJobInstance> jobInstances) {
        this.jobInstances = jobInstances;
    }

    public Set<OpsManagerFeed> getCheckDataFeeds() {
        if (checkDataFeeds == null) {
            checkDataFeeds = new HashSet<>();
        }
        return checkDataFeeds;
    }

    public void setCheckDataFeeds(Set<OpsManagerFeed> checkDataFeeds) {
        this.checkDataFeeds = checkDataFeeds;
    }

    public Set<OpsManagerFeed> getFeedsToCheck() {
        if (feedsToCheck == null) {
            feedsToCheck = new HashSet<>();
        }
        return feedsToCheck;
    }

    public void setFeedsToCheck(Set<OpsManagerFeed> feedsToCheck) {
        this.feedsToCheck = feedsToCheck;
    }

    @Override
    public boolean isStream() {
        return isStream;
    }

    public void setStream(boolean stream) {
        isStream = stream;
    }

    public Long getTimeBetweenBatchJobs() {
        return timeBetweenBatchJobs;
    }

    public void setTimeBetweenBatchJobs(Long timeBetweenBatchJobs) {
        this.timeBetweenBatchJobs = timeBetweenBatchJobs;
    }
}
