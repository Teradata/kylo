/**
 * 
 */
package com.thinkbiganalytics.metadata.jpa.feed;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.thinkbiganalytics.metadata.api.feed.FeedDestination;
import com.thinkbiganalytics.metadata.api.op.DataOperation;
import com.thinkbiganalytics.metadata.jpa.datasource.JpaDatasource;
import com.thinkbiganalytics.metadata.jpa.op.JpaDataOperation;

/**
 *
 * @author Sean Felten
 */
@Entity
@Table(name="FEED_DESTINATION")
public class JpaFeedDestination extends JpaFeedData implements FeedDestination {

    private static final long serialVersionUID = 241001606640713117L;
    
    @EmbeddedId
    private DestinationId id;
    
    @OneToMany(targetEntity=JpaDataOperation.class, mappedBy = "producer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DataOperation> operations;

    public JpaFeedDestination() {
    }
    
    public JpaFeedDestination(JpaFeed feed, JpaDatasource ds) {
        super(feed, ds);
        this.id = DestinationId.create();
    }

    public void setId(DestinationId id) {
        this.id = id;
    }

    @Override
    public ID getId() {
        return this.id;
    }
    
    
    @Embeddable
    public static class DestinationId implements FeedDestination.ID {
        
        private static final long serialVersionUID = 241001606640713117L;
        
        private UUID uuid;
        
        public static DestinationId create() {
            return new DestinationId(UUID.randomUUID());
        }
        
        public DestinationId() {
        }
        
        public UUID getUuid() {
            return uuid;
        }
        
        public void setUuid(UUID uuid) {
            this.uuid = uuid;
        }
        
        public DestinationId(Serializable ser) {
            if (ser instanceof String) {
                this.uuid = UUID.fromString((String) ser);
            } else if (ser instanceof UUID) {
                this.uuid = (UUID) ser;
            } else {
                throw new IllegalArgumentException("Unknown ID value: " + ser);
            }
        }
        
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof DestinationId) {
                DestinationId that = (DestinationId) obj;
                return Objects.equals(this.uuid, that.uuid);
            } else {
                return false;
            }
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(getClass(), this.uuid);
        }
        
        @Override
        public String toString() {
            return this.uuid.toString();
        }
    }
//    
//    
//    @Embeddable
//    public static class DestinationId extends BaseId implements FeedDestination.ID {
//        
//        private static final long serialVersionUID = -1229908599357170293L;
//        
//        public static DestinationId create() {
//            return new DestinationId(UUID.randomUUID());
//        }
//        
//        public DestinationId() {
//            super();
//        }
//        
//        public DestinationId(Serializable ser) {
//            super(ser);
//        } 
//    }

}
