/**
 * 
 */
package com.thinkbiganalytics.controller.precond.metric;

import java.text.ParseException;

/**
 *
 * @author Sean Felten
 */
public class FeedExecutedSinceFeedMetric extends DependentDatasetMetric {

    private String sinceName;

    public FeedExecutedSinceFeedMetric(String hasRunFeed, String sinceFeed) throws ParseException {
        super(hasRunFeed);
        this.sinceName = sinceFeed;
    }
    
    public String getSinceName() {
        return sinceName;
    }
    
    @Override
    public String getDescription() {
        return "dataset " + getDatasetName() + " has executed successfully since feed " + getSinceName();
    }
}
