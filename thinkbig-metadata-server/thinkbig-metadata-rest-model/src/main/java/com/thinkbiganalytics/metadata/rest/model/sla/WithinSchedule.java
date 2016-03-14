/**
 * 
 */
package com.thinkbiganalytics.metadata.rest.model.sla;

/**
 *
 * @author Sean Felten
 */
public class WithinSchedule extends Metric {

    private String cronSchedule;
    private String period;

    public WithinSchedule() {
        super();
    }
    
    public WithinSchedule(String cronSchedule, String period) {
        super();
        this.cronSchedule = cronSchedule;
        this.period = period;
    }

    public String getCronSchedule() {
        return cronSchedule;
    }

    public void setCronSchedule(String cronSchedule) {
        this.cronSchedule = cronSchedule;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

}
