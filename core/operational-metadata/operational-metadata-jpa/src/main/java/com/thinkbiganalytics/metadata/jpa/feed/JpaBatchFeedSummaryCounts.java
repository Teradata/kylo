package com.thinkbiganalytics.metadata.jpa.feed;

/**
 * Created by sr186054 on 11/28/16.
 */

import com.thinkbiganalytics.jpa.BaseJpaId;
import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeed;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name="BATCH_FEED_SUMMARY_COUNTS_VW")
public class JpaBatchFeedSummaryCounts implements BatchFeedSummaryCounts {

    @OneToOne(targetEntity = JpaOpsManagerFeed.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "FEED_ID")
    OpsManagerFeed feed;

    @EmbeddedId
    BatchFeedSummaryCountsFeedId feedId;

    @Column(name="FEED_NAME", insertable = false,updatable = false)
    String feedName;

    @Column(name="ALL_COUNT")
    Long allCount;

    @Column(name="FAILED_COUNT")
    Long failedCount;

    @Column(name="COMPLETED_COUNT")
    Long completedCount;

    @Column(name="ABANDONED_COUNT")
    Long abandonedCount;

    public JpaBatchFeedSummaryCounts(){

    }


    @Override
    public OpsManagerFeed getFeed() {
        return feed;
    }

    public void setFeed(OpsManagerFeed feed) {
        this.feed = feed;
    }

    @Override
    public OpsManagerFeed.ID getFeedId() {
        return feedId;
    }

    public void setFeedId(OpsManagerFeed.ID feedId) {
        this.feedId = (BatchFeedSummaryCountsFeedId) feedId;
    }

    @Override
    public String getFeedName() {
        return feedName;
    }

    public void setFeedName(String feedName) {
        this.feedName = feedName;
    }

    @Override
    public Long getAllCount() {
        return allCount;
    }

    public void setAllCount(Long allCount) {
        this.allCount = allCount;
    }

    @Override
    public Long getFailedCount() {
        return failedCount;
    }

    public void setFailedCount(Long failedCount) {
        this.failedCount = failedCount;
    }

    @Override
    public Long getCompletedCount() {
        return completedCount;
    }

    public void setCompletedCount(Long completedCount) {
        this.completedCount = completedCount;
    }

    @Override
    public Long getAbandonedCount() {
        return abandonedCount;
    }

    public void setAbandonedCount(Long abandonedCount) {
        this.abandonedCount = abandonedCount;
    }

    @Embeddable
    public static class BatchFeedSummaryCountsFeedId extends BaseJpaId implements Serializable, OpsManagerFeed.ID {

        private static final long serialVersionUID = 6017751710414995750L;

        @Column(name = "feed_id", columnDefinition = "binary(16)")
        private UUID uuid;


        public BatchFeedSummaryCountsFeedId() {
        }

        public BatchFeedSummaryCountsFeedId(Serializable ser) {
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
