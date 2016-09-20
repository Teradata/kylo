package com.thinkbiganalytics.metadata.jpa.feed;

import com.thinkbiganalytics.jpa.BaseJpaId;
import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeed;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

/**
 * This entity is used to Map the Operational Feed Data with the Modeshape JCR feed data. The ID here maps directly to the JCR Modeshape Feed.ID Created by sr186054 on 9/15/16.
 */
@Entity
@Table(name = "FEED")
public class JpaOpsManagerFeed implements OpsManagerFeed {

    @EmbeddedId
    private OpsManagerFeedId id;

    @Column(name = "name", length = 100, unique = true, nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "FEED_TYPE")
    private FeedType feedType = FeedType.FEED;

    public JpaOpsManagerFeed(OpsManagerFeed.ID id, String name) {
        this.id = (OpsManagerFeedId) id;
        this.name = name;
    }

    public JpaOpsManagerFeed() {

    }

    @Embeddable
    public static class OpsManagerFeedId extends BaseJpaId implements Serializable, OpsManagerFeed.ID {

        private static final long serialVersionUID = 6017751710414995750L;

        @Column(name = "id", columnDefinition = "binary(16)")
        private UUID uuid;

        public static OpsManagerFeedId create() {
            return new OpsManagerFeedId(UUID.randomUUID());
        }


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
}
