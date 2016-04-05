/**
 * 
 */
package com.thinkbiganalytics.metadata.jpa.feed;

import java.io.Serializable;
import java.util.UUID;

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
public class JpaFeedSource extends JpaFeedData implements FeedSource {
    
    private static final long serialVersionUID = -6526999444073467663L;

    @EmbeddedId
    private SourceId id;
    
    @OneToOne
    private JpaServiceLevelAgreement agreemenet;
    
    public JpaFeedSource(JpaFeed feed, JpaDatasource ds, JpaServiceLevelAgreement agreement) {
        super(feed, ds);
        this.id = new SourceId();
        this.agreemenet = agreement;
    }

    @Override
    public ID getId() {
        return this.id;
    }
    
    @Override
    public ServiceLevelAgreement getAgreement() {
        return this.agreemenet;
    }
    
    public ServiceLevelAgreement getAgreemenet() {
        return agreemenet;
    }

    public void setAgreemenet(JpaServiceLevelAgreement agreemenet) {
        this.agreemenet = agreemenet;
    }

    public void setId(SourceId id) {
        this.id = id;
    }


    public static class SourceId extends BaseId implements FeedSource.ID {
        
        private static final long serialVersionUID = -2419178343556356525L;

        public static SourceId create() {
            return new SourceId(UUID.randomUUID());
        }

        public SourceId() {
            super();
        }

        public SourceId(Serializable ser) {
            super(ser);
        } 
    }

}
