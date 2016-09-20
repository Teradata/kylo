/**
 * 
 */
package com.thinkbiganalytics.metadata.api.sla;

import java.beans.Transient;

/**
 *
 * @author Sean Felten
 */
public class DatasourceUpdatedSinceFeedExecuted extends DependentDatasource {

    public DatasourceUpdatedSinceFeedExecuted(){}

    public DatasourceUpdatedSinceFeedExecuted(String datasourceName, String feedName) {
        super(feedName, datasourceName);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.sla.api.Metric#getDescription()
     */
    @Override
    @Transient
    public String getDescription() {
        return "Datasource " + getDatasourceName() + " has been updated since feed " + getFeedName() + " was last executed";
    }
}
