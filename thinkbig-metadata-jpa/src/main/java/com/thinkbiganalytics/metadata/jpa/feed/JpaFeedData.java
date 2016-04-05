/**
 * 
 */
package com.thinkbiganalytics.metadata.jpa.feed;

import javax.persistence.ManyToOne;

import com.thinkbiganalytics.metadata.api.datasource.Datasource;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedData;
import com.thinkbiganalytics.metadata.jpa.datasource.JpaDatasource;

/**
 *
 * @author Sean Felten
 */
@SuppressWarnings("serial")
public abstract class JpaFeedData implements FeedData {

    @ManyToOne
    private JpaFeed feed;
    
    @ManyToOne
    private JpaDatasource dataset;
    
    public JpaFeedData() {
    }
    
    public JpaFeedData(JpaFeed feed, JpaDatasource ds) {
        setFeed(feed);
        setDataset(ds);
    }
    
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
