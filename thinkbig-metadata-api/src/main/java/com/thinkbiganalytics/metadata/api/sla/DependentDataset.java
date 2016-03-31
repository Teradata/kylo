/**
 * 
 */
package com.thinkbiganalytics.metadata.api.sla;

import com.thinkbiganalytics.metadata.sla.api.Metric;

/**
 *
 * @author Sean Felten
 */
public abstract class DependentDataset implements Metric {
    
    private final String datasetName;
    private final String feedName;

    public DependentDataset(String datasetName) {
        this(null, datasetName);
    }
    
    public DependentDataset(String feedName, String datasetName) {
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
