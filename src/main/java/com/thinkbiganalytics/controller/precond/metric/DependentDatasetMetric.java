/**
 * 
 */
package com.thinkbiganalytics.controller.precond.metric;

import com.thinkbiganalytics.metadata.sla.api.Metric;

/**
 *
 * @author Sean Felten
 */
public abstract class DependentDatasetMetric implements Metric {
    
    private String datasetName;
    private String feedName;

    public DependentDatasetMetric(String datasetName) {
        super();
        this.datasetName = datasetName;
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
