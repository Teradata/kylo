/**
 * 
 */
package com.thinkbiganalytics.controller.precond.metric;

import com.thinkbiganalytics.metadata.sla.api.Metric;

/**
 *
 * @author Sean Felten
 */
public abstract class DependentFeedMetric implements Metric {

    private String feedName;

    public DependentFeedMetric(String feedName) {
        super();
        this.feedName = feedName;
    }
    
    public String getFeedName() {
        return feedName;
    }
}
