/**
 * 
 */
package com.thinkbiganalytics.metadata.jpa.feed.security;

/*-
 * #%L
 * kylo-operational-metadata-jpa
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

import java.io.Serializable;
import java.security.Principal;
import java.security.acl.Group;
import java.util.Objects;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.querydsl.core.annotations.PropertyType;
import com.querydsl.core.annotations.QueryType;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeed;
import com.thinkbiganalytics.metadata.api.feed.security.FeedOpsAclEntry;
import com.thinkbiganalytics.metadata.jpa.feed.JpaOpsManagerFeed;

/**
 *
 */
@Entity
@Table(name = "FEED_ACL_INDEX")
public class JpaFeedOpsAclEntry implements FeedOpsAclEntry, Serializable{

    @EmbeddedId
    private EntryId id;
    
    @Column(name = "feed_id", insertable = false, updatable = false)
    private UUID feedId;
    
    @Column(name = "principal", insertable = false, updatable = false)
    private String principalName;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "principal_type", insertable = false, updatable = false)
    @QueryType(PropertyType.ENUM)
    private com.thinkbiganalytics.metadata.api.feed.security.FeedOpsAclEntry.PrincipalType principalType =  com.thinkbiganalytics.metadata.api.feed.security.FeedOpsAclEntry.PrincipalType.USER;

    @ManyToOne(targetEntity = JpaOpsManagerFeed.class, fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "FEED_ID", nullable = true, insertable = false, updatable = false)
    private OpsManagerFeed feed;
    
    public JpaFeedOpsAclEntry() {
        super();
    }

    public JpaFeedOpsAclEntry(Feed.ID id, Principal principal) {
        this(id, principal.getName(), principal instanceof Group ? PrincipalType.GROUP : PrincipalType.USER);
    }
    
    public JpaFeedOpsAclEntry(Feed.ID id, String principalName, PrincipalType type) {
        this.id = new EntryId(UUID.fromString(id.toString()), principalName, type);
    }
    
    
    @Override
    public UUID getFeedId() {
        return this.feedId != null ? this.feedId : (this.getId() != null ? this.getId().getUuid() : null);
    }

    @Override
    public String getPrincipalName() {
        return this.principalName != null ? this.principalName : (this.getId() != null ? this.getId().getPrincipalName() : null);
    }

    @Override
    public com.thinkbiganalytics.metadata.api.feed.security.FeedOpsAclEntry.PrincipalType getPrincipalType() {
       return this.getId() != null && this.getId().getPrincipalType() != null ? this.getId().getPrincipalType() : this.principalType;
    }

    @Override
    public OpsManagerFeed getFeed() {
        return feed;
    }

    public void setFeed(OpsManagerFeed feed) {
        this.feed = feed;
    }


    @Override
    public EntryId getId() {
        return id;
    }

    public static class EntryId implements ID {

        private static final long serialVersionUID = 1L;

        @Column(name = "feed_id", unique = false, nullable = false)
        private UUID uuid;

        @Column(name = "principal", length = 255, unique = false, nullable = false)
        private String principalName;

        @Enumerated(EnumType.STRING)
        @Column(name = "principal_type", length = 10, unique = false, nullable = false)
        @QueryType(PropertyType.ENUM)
        private com.thinkbiganalytics.metadata.api.feed.security.FeedOpsAclEntry.PrincipalType principalType;

        public EntryId() {
        }

        public EntryId(UUID uuid, String principalName, PrincipalType type) {
            super();
            this.uuid = uuid;
            this.principalName = principalName;
            this.principalType = type;
        }

        public UUID getUuid() {
            return uuid;
        }

        public void setUuid(UUID uuid) {
            this.uuid = uuid;
        }

        public String getPrincipalName() {
            return principalName;
        }

        public void setPrincipalName(String principalName) {
            this.principalName = principalName;
        }

        public PrincipalType getPrincipalType() {
            return principalType;
        }

        public void setPrincipalType(PrincipalType principalType) {
            this.principalType = principalType;
        }

        @Override
        public int hashCode() {
            return Objects.hash(getUuid(), getPrincipalName());
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass().isAssignableFrom(obj.getClass())) {
                EntryId that = (EntryId) obj;
                return Objects.equals(getUuid(), that.getUuid()) && Objects.equals(getPrincipalName(), that.getPrincipalName());
            } else {
                return false;
            }
        }


    }

}
