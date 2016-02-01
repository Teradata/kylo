/**
 * 
 */
package com.thinkbiganalytics.metadata.core.feed;

import java.util.List;

import com.thinkbiganalytics.metadata.api.dataset.Dataset.ID;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedCriteria;
import com.thinkbiganalytics.metadata.api.feed.FeedDestination;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.feed.FeedSource;

/**
 *
 * @author Sean Felten
 */
public class InMemoryFeedProvider implements FeedProvider {

    public FeedSource createFeedSource(ID id) {
        // TODO Auto-generated method stub
        return null;
    }

    public FeedSource createFeedSource(ID id, com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement.ID slaId) {
        // TODO Auto-generated method stub
        return null;
    }

    public FeedDestination createFeedDestination(ID dsId) {
        // TODO Auto-generated method stub
        return null;
    }

    public Feed createFeed(String name, String descr, ID srcId, ID destId) {
        // TODO Auto-generated method stub
        return null;
    }

    public Feed createFeed(String name, String descr, FeedSource src, FeedDestination dest) {
        // TODO Auto-generated method stub
        return null;
    }

    public FeedCriteria feedCriteria() {
        // TODO Auto-generated method stub
        return null;
    }

    public Feed getFeed(com.thinkbiganalytics.metadata.api.feed.Feed.ID id) {
        // TODO Auto-generated method stub
        return null;
    }

    public List<Feed> getFeeds() {
        // TODO Auto-generated method stub
        return null;
    }

    public List<Feed> getFeeds(FeedCriteria criteria) {
        // TODO Auto-generated method stub
        return null;
    }

}
