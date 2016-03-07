/**
 * 
 */
package com.thinkbiganalytics.metadata.rest.model.sla;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 *
 * @author Sean Felten
 */
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DatasourceUpdatedSinceMetric extends Metric {

    private String datasourceId;
    private String datasourceName;
    private String cronSchedule;
    
    public static DatasourceUpdatedSinceMetric named(String feedName, String schedule) {
        DatasourceUpdatedSinceMetric m = new DatasourceUpdatedSinceMetric();
        m.setDatasourceName(feedName);
        m.setCronSchedule(schedule);
        return m;
    }
    
    public static DatasourceUpdatedSinceMetric id(String id, String schedule) {
        DatasourceUpdatedSinceMetric m = new DatasourceUpdatedSinceMetric();
        m.setDatasourceId(id);
        m.setCronSchedule(schedule);
        return m;
    }

    public String getDatasourceId() {
        return datasourceId;
    }

    public void setDatasourceId(String testedFeed) {
        this.datasourceId = testedFeed;
    }
    
    public String getDatasourceName() {
        return datasourceName;
    }
    
    public void setDatasourceName(String datasourceName) {
        this.datasourceName = datasourceName;
    }

    public String getCronSchedule() {
        return cronSchedule;
    }

    public void setCronSchedule(String cronSchedule) {
        this.cronSchedule = cronSchedule;
    }

}
