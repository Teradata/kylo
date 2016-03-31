/**
 * 
 */
package com.thinkbiganalytics.metadata.api.sla;

/**
 *
 * @author Sean Felten
 */
public class DatasourceUpdatedSinceFeedExecuted extends DependentDataset {

    private final String feedName;
    
    public DatasourceUpdatedSinceFeedExecuted(String datasetName, String feedName) {
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
