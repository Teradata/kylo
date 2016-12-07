package com.thinkbiganalytics.metadata.jpa.feed;

import com.thinkbiganalytics.jpa.BaseJpaId;
import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeed;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * Created by sr186054 on 11/28/16.
 */
@Embeddable
public class OpsManagerFeedId extends BaseJpaId implements Serializable, OpsManagerFeed.ID {

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