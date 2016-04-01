/**
 * 
 */
package com.thinkbiganalytics.metadata.api.sla;

import java.text.ParseException;

import org.quartz.CronExpression;

/**
 *
 * @author Sean Felten
 */
public class DatasourceUpdatedSinceSchedule extends DependentDatasource {

    private final CronExpression cronExpression;

    public DatasourceUpdatedSinceSchedule(String datasetName, String cronExpression) throws ParseException {
        super(datasetName);
        this.cronExpression = new CronExpression(cronExpression);
    }
    
    public CronExpression getCronExpression() {
        return cronExpression;
    }
    
    @Override
    public String getDescription() {
        return "Datasource " + getDatasourceName() + " has been updated since " + getCronExpression();
    }
}
