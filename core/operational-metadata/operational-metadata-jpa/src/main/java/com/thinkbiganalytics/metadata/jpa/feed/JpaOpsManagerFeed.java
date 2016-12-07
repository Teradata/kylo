package com.thinkbiganalytics.metadata.jpa.feed;

import com.thinkbiganalytics.jpa.BaseJpaId;
import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeed;
import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecutionParameter;
import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobInstance;
import com.thinkbiganalytics.metadata.jpa.jobrepo.job.JpaBatchJobExecutionParameter;
import com.thinkbiganalytics.metadata.jpa.jobrepo.job.JpaBatchJobInstance;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedStoredProcedureQuery;
import javax.persistence.OneToMany;
import javax.persistence.ParameterMode;
import javax.persistence.StoredProcedureParameter;
import javax.persistence.Table;

/**
 * This entity is used to Map the Operational Feed Data with the Modeshape JCR feed data. The ID here maps directly to the JCR Modeshape Feed.ID Created by sr186054 on 9/15/16.
 */
@Entity
@Table(name = "FEED")
@NamedStoredProcedureQuery(name = "OpsManagerFeed.deleteFeedJobs", procedureName = "delete_feed_jobs", parameters = {
    @StoredProcedureParameter(mode = ParameterMode.IN, name = "category", type = String.class),
    @StoredProcedureParameter(mode = ParameterMode.IN, name = "feed", type = String.class) })
public class JpaOpsManagerFeed implements OpsManagerFeed {

    @EmbeddedId
    private OpsManagerFeedId id;

    @Column(name = "name", length = 100, unique = true, nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "FEED_TYPE")
    private FeedType feedType = FeedType.FEED;

    @OneToMany(targetEntity = JpaBatchJobInstance.class, mappedBy = "feed",fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<BatchJobInstance> jobInstances = new HashSet<>();




    @ManyToMany(targetEntity=JpaOpsManagerFeed.class,cascade = {CascadeType.ALL})
    @JoinTable(name="FEED_CHECK_DATA_FEEDS",
               joinColumns={@JoinColumn(name="FEED_ID")},
               inverseJoinColumns={@JoinColumn(name="CHECK_DATA_FEED_ID")})
    private Set<OpsManagerFeed> checkDataFeeds = new HashSet<OpsManagerFeed>();

    @ManyToMany(targetEntity=JpaOpsManagerFeed.class,mappedBy="checkDataFeeds")
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
        if(checkDataFeeds == null){
            checkDataFeeds = new HashSet<>();
        }
        return checkDataFeeds;
    }

    public void setCheckDataFeeds(Set<OpsManagerFeed> checkDataFeeds) {
        this.checkDataFeeds = checkDataFeeds;
    }

    public Set<OpsManagerFeed> getFeedsToCheck() {
        if(feedsToCheck == null){
            feedsToCheck = new HashSet<>();
        }
        return feedsToCheck;
    }

    public void setFeedsToCheck(Set<OpsManagerFeed> feedsToCheck) {
        this.feedsToCheck = feedsToCheck;
    }



}
