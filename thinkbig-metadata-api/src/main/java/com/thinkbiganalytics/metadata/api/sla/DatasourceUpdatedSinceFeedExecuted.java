/**
 * 
 */
package com.thinkbiganalytics.metadata.api.sla;

/**
 *
 * @author Sean Felten
 */
public class DatasourceUpdatedSinceFeedExecuted extends DependentDatasource {

    private final String feedName;
    
    public DatasourceUpdatedSinceFeedExecuted(String datasourceName, String feedName) {
        super(datasourceName);
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
        return "Datasource " + getDatasourceName() + " has been updated since feed " + getFeedName() + " was last executed";
    }

}
