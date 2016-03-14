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
public class DatasourceUpdatedSinceScheduleMetric extends DependentDatasourceMetric {

    private String cronSchedule;

    public static DependentDatasourceMetric named(String datasourceName, String schedule) {
        DatasourceUpdatedSinceScheduleMetric m = new DatasourceUpdatedSinceScheduleMetric();
        m.setDatasourceName(datasourceName);
        m.setCronSchedule(schedule);
        return m;
    }

    public static DependentDatasourceMetric id(String id, String schedule) {
        DatasourceUpdatedSinceScheduleMetric m = new DatasourceUpdatedSinceScheduleMetric();
        m.setDatasourceId(id);
        m.setCronSchedule(schedule);
        return m;
    }

    public DatasourceUpdatedSinceScheduleMetric() {
        super();
    }

    public String getCronSchedule() {
        return cronSchedule;
    }

    public void setCronSchedule(String cronSchedule) {
        this.cronSchedule = cronSchedule;
    }

}
