/**
 * 
 */
package com.thinkbiganalytics.metadata.rest.model.sla;

import java.text.ParseException;

/**
 *
 * @author Sean Felten
 */
public class FeedExecutedSinceFeedMetric extends DependentFeedMetric {

    private String sinceFeedName;

    public FeedExecutedSinceFeedMetric(String hasRunFeed, String sinceFeed) {
        super(hasRunFeed);
        this.sinceFeedName = sinceFeed;
    }
    
    public String getSinceName() {
        return sinceFeedName;
    }

    public String getSinceFeedName() {
        return sinceFeedName;
    }

    public void setSinceFeedName(String sinceFeedName) {
        this.sinceFeedName = sinceFeedName;
    }
    
}
