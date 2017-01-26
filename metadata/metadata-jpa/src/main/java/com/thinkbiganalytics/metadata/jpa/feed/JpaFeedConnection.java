/**
 * 
 */
package com.thinkbiganalytics.metadata.jpa.feed;

import com.thinkbiganalytics.jpa.AbstractAuditedEntity;
import com.thinkbiganalytics.metadata.api.datasource.Datasource;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedConnection;
import com.thinkbiganalytics.metadata.jpa.datasource.JpaDatasource;

import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

/**
 *
 * @author Sean Felten
 */
@MappedSuperclass
public abstract class JpaFeedConnection extends AbstractAuditedEntity implements FeedConnection {

    private static final long serialVersionUID = -1752094328137424635L;

    @ManyToOne
    private JpaFeed feed;
    
    @ManyToOne
    private JpaDatasource datasource;
    
    public JpaFeedConnection() {
    }
    
    public JpaFeedConnection(JpaFeed feed, JpaDatasource ds) {
        setFeed(feed);
        addConnection(ds);
    }
    
    @Override
    public Feed getFeed() {
        return this.feed;
    }
    
    @Override
    public Datasource getDatasource() {
        return this.datasource;
    }

    public void setDatasource(JpaDatasource dataset) {
        this.datasource = dataset;
    }

    public void setFeed(JpaFeed feed) {
        this.feed = feed;
    }
    
    protected abstract void addConnection(JpaDatasource ds);
    
}
