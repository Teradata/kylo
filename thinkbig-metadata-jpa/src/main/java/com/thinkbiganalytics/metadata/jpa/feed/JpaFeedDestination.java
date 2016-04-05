/**
 * 
 */
package com.thinkbiganalytics.metadata.jpa.feed;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.thinkbiganalytics.metadata.api.feed.FeedDestination;
import com.thinkbiganalytics.metadata.jpa.datasource.JpaDatasource;
import com.thinkbiganalytics.metadata.jpa.feed.JpaFeedSource.SourceId;

/**
 *
 * @author Sean Felten
 */
@Entity
@Table(name="FEED_DESTINATION")
public class JpaFeedDestination extends JpaFeedData implements FeedDestination {

    private static final long serialVersionUID = 6911145271954695318L;
    
    @EmbeddedId
    private DestinationId id;

    public JpaFeedDestination() {
    }
    
    public JpaFeedDestination(JpaFeed feed, JpaDatasource ds) {
        super(feed, ds);
        this.id = new DestinationId();
    }

    public void setId(DestinationId id) {
        this.id = id;
    }

    @Override
    public ID getId() {
        return this.id;
    }
    
    
    public static class DestinationId extends BaseId implements FeedDestination.ID {

        private static final long serialVersionUID = -1229908599357170293L;

        public static SourceId create() {
            return new SourceId(UUID.randomUUID());
        }
        
        public DestinationId() {
            super();
        }

        public DestinationId(Serializable ser) {
            super(ser);
        } 
    }

}
