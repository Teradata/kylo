/**
 * 
 */
package com.thinkbiganalytics.metadata.jpa.feed.security;

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
import javax.persistence.Table;

import com.thinkbiganalytics.metadata.api.feed.Feed;

/**
 *
 */
@Entity
@Table(name = "FEED_ACL_INDEX")
public class JpaFeedOpsAclEntry {
    
    public enum PrincipalType { USER, GROUP }

    @EmbeddedId
    private EntryId id;
    
//    @Id
//    @Column(name = "feed_id")
//    private UUID feedId;
//
//    @Id
//    @Column(name = "principal", length = 255, unique = false, nullable = false)
//    private String principalName;
//    
//    @Id
//    @Enumerated(EnumType.STRING)
//    @Column(name = "principal_type")
//    private PrincipalType principalType;
    
    public JpaFeedOpsAclEntry() {
        super();
    }

    public JpaFeedOpsAclEntry(Feed.ID id, Principal principal) {
        this(id, principal.getName(), principal instanceof Group ? PrincipalType.GROUP : PrincipalType.USER);
    }
    
    public JpaFeedOpsAclEntry(Feed.ID id, String principalName, PrincipalType type) {
        this.id = new EntryId(UUID.fromString(id.toString()), principalName, type);
    }
    
    
    public UUID getFeedId() {
        return this.id.getUuid();
    }

    public String getPrincipalName() {
        return this.id.getPrincipalName();
    }



    public static class EntryId implements Serializable {

        private static final long serialVersionUID = 1L;

        @Column(name = "feed_id", unique = false, nullable = false)
        private UUID uuid;
        
        @Column(name = "principal", length = 255, unique = false, nullable = false)
        private String principalName;
        
        @Enumerated(EnumType.STRING)
        @Column(name = "principal_type", length = 10, unique = false, nullable = false)
        private PrincipalType principalType;

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
