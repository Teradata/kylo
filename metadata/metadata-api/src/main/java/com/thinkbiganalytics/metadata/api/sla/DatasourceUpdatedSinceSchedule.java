/**
 * 
 */
package com.thinkbiganalytics.metadata.api.sla;

import java.beans.Transient;
import java.text.ParseException;

import org.quartz.CronExpression;

/**
 *
 * @author Sean Felten
 */
public class DatasourceUpdatedSinceSchedule extends DependentDatasource {

    private transient CronExpression cronExpression;
    private String cronString;
    
    public DatasourceUpdatedSinceSchedule() {
    }

    public DatasourceUpdatedSinceSchedule(String datasetName, String cronStr) throws ParseException {
        super(datasetName);
        this.cronExpression = new CronExpression(cronStr);
        this.cronString = cronStr;
    }
    
    public DatasourceUpdatedSinceSchedule(String datasetName, CronExpression cronExpression) throws ParseException {
        super(datasetName);
        this.cronExpression = cronExpression;
        this.cronString = cronExpression.toString();
    }
    
    public CronExpression getCronExpression() {
        return cronExpression;
    }
    
    public void setCronExpression(CronExpression cronExpression) {
        this.cronExpression = cronExpression;
        this.cronString = cronExpression.toString();
    }
    
    @Override
    @Transient
    public String getDescription() {
        return "Datasource " + getDatasourceName() + " has been updated since " + getCronExpression();
    }
    
    protected String getCronString() {
        return cronString;
    }
    
    protected void setCronString(String cronString) {
        this.cronString = cronString;
        try {
            this.cronExpression = new CronExpression(cronString);
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
