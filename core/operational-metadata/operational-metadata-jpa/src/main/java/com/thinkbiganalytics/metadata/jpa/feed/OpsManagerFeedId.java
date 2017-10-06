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

import com.thinkbiganalytics.jpa.BaseJpaId;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeed;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * The primary key for a {@link OpsManagerFeed}
 */
@Embeddable
public class OpsManagerFeedId extends BaseJpaId implements Serializable, OpsManagerFeed.ID, Feed.ID {

    private static final long serialVersionUID = 6017751710414995750L;

    @Column(name = "id")
    private UUID uuid;

    public OpsManagerFeedId() {
    }


    public OpsManagerFeedId(Serializable ser) {
        super(ser);
    }

    public static OpsManagerFeedId create() {
        return new OpsManagerFeedId(UUID.randomUUID());
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
