/**
 * 
 */
package com.thinkbiganalytics.metadata.jpa.feed;

import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

import com.thinkbiganalytics.metadata.api.datasource.Datasource;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedConnection;
import com.thinkbiganalytics.metadata.jpa.datasource.JpaDatasource;

/**
 *
 * @author Sean Felten
 */
@MappedSuperclass
public abstract class JpaFeedConnection implements FeedConnection {

    private static final long serialVersionUID = -1752094328137424635L;

    @ManyToOne
    private JpaFeed feed;
    
    @ManyToOne
    private JpaDatasource dataset;
    
    public JpaFeedConnection() {
    }
    
    public JpaFeedConnection(JpaFeed feed, JpaDatasource ds) {
        setFeed(feed);
        addConnection(ds);
    }
    
    protected abstract void addConnection(JpaDatasource ds);

    public JpaDatasource getDataset() {
        return dataset;
    }
    
    @Override
    public Feed getFeed() {
        return this.feed;
    }
    
    @Override
    public Datasource getDatasource() {
        return this.dataset;
    }

    public void setDataset(JpaDatasource dataset) {
        this.dataset = dataset;
    }

    public void setFeed(JpaFeed feed) {
        this.feed = feed;
    }
    
}
