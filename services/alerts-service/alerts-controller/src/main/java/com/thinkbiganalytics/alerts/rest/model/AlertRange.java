/**
 * 
 */
package com.thinkbiganalytics.alerts.rest.model;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Contains the range of alerts as a result of a query.  There are also attributes for 
 * the result size and the alert IDs that bound the result.
 * @author Sean Felten
 */
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AlertRange {

    private DateTime newestTime;
    private DateTime oldestTime;
    private int size;
    private List<Alert> alerts;
    
    public AlertRange() {
    }
    
    public AlertRange(List<Alert> alerts) {
        assert alerts != null;
        
        if (alerts.size() > 0) {
            this.newestTime = alerts.get(0).getCreatedTime();
            this.oldestTime = alerts.get(alerts.size() - 1).getCreatedTime();
        } else {
            this.newestTime = null;
            this.newestTime = null;
        }
        
        this.alerts = new ArrayList<>(alerts);
        this.size = alerts.size();
    }

    public DateTime getNewestTime() {
        return newestTime;
    }

    public void setNewestTime(DateTime firstTime) {
        this.newestTime = firstTime;
    }

    public DateTime getOldestTime() {
        return oldestTime;
    }

    public void setOldestTime(DateTime lastTime) {
        this.oldestTime = lastTime;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public List<Alert> getAlerts() {
        return alerts;
    }

    public void setAlerts(List<Alert> alerts) {
        this.alerts = alerts;
    }

}
