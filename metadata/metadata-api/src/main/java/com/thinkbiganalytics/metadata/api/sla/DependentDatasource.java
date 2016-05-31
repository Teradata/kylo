/**
 * 
 */
package com.thinkbiganalytics.metadata.api.sla;

import com.thinkbiganalytics.metadata.sla.api.Metric;

/**
 *
 * @author Sean Felten
 */
public abstract class DependentDatasource implements Metric {
    
    private String datasourceName;
    private String feedName;
    
    public DependentDatasource() {
    }

    public DependentDatasource(String datasetName) {
        this(null, datasetName);
    }
    
    public DependentDatasource(String feedName, String datasetName) {
        super();
        this.feedName = feedName;
        this.datasourceName = datasetName;
    }

    public String getDatasourceName() {
        return datasourceName;
    }
    
    public String getFeedName() {
        return feedName;
    }

    public void setDatasourceName(String datasourceName) {
        this.datasourceName = datasourceName;
    }

    public void setFeedName(String feedName) {
        this.feedName = feedName;
    }
    
}
