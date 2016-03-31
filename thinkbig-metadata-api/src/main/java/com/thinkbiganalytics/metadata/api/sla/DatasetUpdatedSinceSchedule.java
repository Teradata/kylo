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
public class DatasetUpdatedSinceSchedule extends DependentDataset {

    private final CronExpression cronExpression;

    public DatasetUpdatedSinceSchedule(String datasetName, String cronExpression) throws ParseException {
        super(datasetName);
        this.cronExpression = new CronExpression(cronExpression);
    }
    
    public CronExpression getCronExpression() {
        return cronExpression;
    }
    
    @Override
    public String getDescription() {
        return "dataset " + getDatasetName() + " has been updated since " + getCronExpression();
    }
}
