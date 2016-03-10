/**
 * 
 */
package com.thinkbiganalytics.metadata.api.feed.precond;

/**
 *
 * @author Sean Felten
 */
public class DatasourceUpdatedSinceFeedExecutedMetric extends DependentDatasetMetric {

    private final String feedName;
    
    public DatasourceUpdatedSinceFeedExecutedMetric(String datasetName, String feedName) {
        super(datasetName);
        this.feedName = feedName;
    }
    
    public String getFeedName() {
        return feedName;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.sla.api.Metric#getDescription()
     */
    @Override
    public String getDescription() {
        return "Datasource " + getDatasetName() + " has been updated since feed " + getFeedName() + " was last executed";
    }

}
