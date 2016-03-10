/**
 * 
 */
package com.thinkbiganalytics.metadata.api.feed.precond;

/**
 *
 * @author Sean Felten
 */
public class FeedExecutedSinceFeedMetric extends DependentFeedMetric {

    private final String sinceName;

    public FeedExecutedSinceFeedMetric(String hasRunFeed, String sinceFeed) {
        super(hasRunFeed);
        this.sinceName = sinceFeed;
    }
    
    public String getSinceName() {
        return sinceName;
    }
    
    @Override
    public String getDescription() {
        return "Check if feed " + getFeedName() + " has executed successfully since feed " + getSinceName();
    }
}
