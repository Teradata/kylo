/**
 * 
 */
package com.thinkbiganalytics.metadata.api.sla;

/**
 *
 * @author Sean Felten
 */
public class FeedExecutedSinceFeed extends DependentFeed {

    private final String sinceName;

    public FeedExecutedSinceFeed(String hasRunFeed, String sinceFeed) {
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
