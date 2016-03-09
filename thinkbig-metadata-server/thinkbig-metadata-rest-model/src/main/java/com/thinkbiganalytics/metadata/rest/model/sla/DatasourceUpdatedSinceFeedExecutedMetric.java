/**
 * 
 */
package com.thinkbiganalytics.metadata.rest.model.sla;

/**
 *
 * @author Sean Felten
 */
public class DatasourceUpdatedSinceFeedExecutedMetric extends DependentDatasourceMetric {
    
    private String feedId;
    private String feedName;
    
    public static DatasourceUpdatedSinceFeedExecutedMetric named(String datasourceName, String feedName) {
        DatasourceUpdatedSinceFeedExecutedMetric m = new DatasourceUpdatedSinceFeedExecutedMetric();
        m.setDatasourceName(datasourceName);
        m.setFeedName(feedName);
        return m;
    }
    
    public static DatasourceUpdatedSinceFeedExecutedMetric ids(String datasourceId, String feedId) {
        DatasourceUpdatedSinceFeedExecutedMetric m = new DatasourceUpdatedSinceFeedExecutedMetric();
        m.setDatasourceId(datasourceId);
        m.setFeedId(feedId);
        return m;
    }

    public String getFeedId() {
        return feedId;
    }

    public void setFeedId(String sinceFeedId) {
        this.feedId = sinceFeedId;
        this.feedName = null;
    }

    public String getFeedName() {
        return feedName;
    }

    public void setFeedName(String sinceFeedName) {
        this.feedName = sinceFeedName;
        this.feedId = null;
    }

}
