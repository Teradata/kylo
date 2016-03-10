/**
 * 
 */
package com.thinkbiganalytics.metadata.api.feed.precond;

import com.thinkbiganalytics.metadata.sla.api.Metric;

/**
 *
 * @author Sean Felten
 */
public abstract class DependentDatasetMetric implements Metric {
    
    private final String datasetName;
    private final String feedName;

    public DependentDatasetMetric(String datasetName) {
        this(null, datasetName);
    }
    
    public DependentDatasetMetric(String feedName, String datasetName) {
        super();
        this.feedName = feedName;
        this.datasetName = datasetName;
    }

    public String getDatasetName() {
        return datasetName;
    }
    
    public String getFeedName() {
        return feedName;
    }
}
