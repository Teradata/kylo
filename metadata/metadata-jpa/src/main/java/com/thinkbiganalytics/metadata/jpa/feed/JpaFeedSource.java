/**
 * 
 */
package com.thinkbiganalytics.metadata.jpa.feed;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.thinkbiganalytics.metadata.api.feed.FeedSource;
import com.thinkbiganalytics.metadata.jpa.datasource.JpaDatasource;
import com.thinkbiganalytics.metadata.jpa.sla.JpaServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;

/**
 *
 * @author Sean Felten
 */
@Entity
@Table(name="FEED_SOURCE")
public class JpaFeedSource extends JpaFeedConnection implements FeedSource {
    
    private static final long serialVersionUID = -6526999444073467663L;

    @EmbeddedId
    private SourceId id;
    
    @OneToOne()
    private JpaServiceLevelAgreement agreement;
    
    public JpaFeedSource() {
    }
    
    public JpaFeedSource(JpaFeed feed, JpaDatasource ds, JpaServiceLevelAgreement agreement) {
        super(feed, ds);
        this.id = SourceId.create();
        this.agreement = agreement;
    }

    @Override
    public ID getId() {
        return this.id;
    }
    
    @Override
    public ServiceLevelAgreement getAgreement() {
        return this.agreement;
    }

    public void setId(SourceId id) {
        this.id = id;
    }
    
    @Override
    protected void addConnection(JpaDatasource ds) {
        ds.addFeedSource(this);
    }


    @Embeddable
    public static class SourceId implements FeedSource.ID {
        
        private static final long serialVersionUID = 241001606640713117L;
        
        //@Column(name="id", columnDefinition="binary(36)")
        @Column(name="id", columnDefinition="binary(16)", length = 16)
        private UUID uuid;
        
        public static SourceId create() {
            return new SourceId(UUID.randomUUID());
        }
        
        public SourceId() {
        }
        
        public UUID getUuid() {
            return uuid;
        }
        
        public void setUuid(UUID uuid) {
            this.uuid = uuid;
        }
        
        public SourceId(Serializable ser) {
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
            if (obj instanceof SourceId) {
                SourceId that = (SourceId) obj;
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
//    public static class SourceId extends BaseId implements FeedSource.ID {
//        
//        private static final long serialVersionUID = -2419178343556356525L;
//        
//        public static SourceId create() {
//            return new SourceId(UUID.randomUUID());
//        }
//        
//        public SourceId() {
//            super();
//        }
//        
//        public SourceId(Serializable ser) {
//            super(ser);
//        } 
//    }

}
