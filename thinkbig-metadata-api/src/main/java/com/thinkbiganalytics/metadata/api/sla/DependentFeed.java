/**
 * 
 */
package com.thinkbiganalytics.metadata.api.sla;

import com.thinkbiganalytics.metadata.sla.api.Metric;

/**
 *
 * @author Sean Felten
 */
public abstract class DependentFeed implements Metric {

    private final String feedName;

    public DependentFeed(String feedName) {
        super();
        this.feedName = feedName;
    }
    
    public String getFeedName() {
        return feedName;
    }
}
