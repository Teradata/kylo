/**
 * 
 */
package com.thinkbiganalytics.metadata.api.sla;

import java.beans.Transient;

/**
 *
 * @author Sean Felten
 */
public class FeedExecutedSinceFeed extends DependentFeed {

    private String sinceName;
    
    public FeedExecutedSinceFeed() {
    }

    public FeedExecutedSinceFeed(String hasRunFeed, String sinceFeed) {
        super(hasRunFeed);
        this.sinceName = sinceFeed;
    }
    
    public String getSinceName() {
        return sinceName;
    }
    
    public void setSinceName(String sinceName) {
        this.sinceName = sinceName;
    }
    
    @Override
    @Transient
    public String getDescription() {
        return "Check if feed " + getFeedName() + " has executed successfully since feed " + getSinceName();
    }
}
