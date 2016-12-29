/**
 * 
 */
package com.thinkbiganalytics.alerts.rest.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.thinkbiganalytics.alerts.api.Alert;

/**
 *
 * @author Sean Felten
 */
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AlertRange {

    private String firstId;
    private String lastId;
    private int size;
    private List<Alert> alerts;
    
    public AlertRange() {
    }
    
    public AlertRange(List<Alert> alerts) {
        assert alerts != null;
        
        if (alerts.size() > 0) {
            this.firstId = alerts.get(0).getId().toString();
            this.lastId = alerts.get(alerts.size() - 1).getId().toString();
        } else {
            this.firstId = null;
            this.lastId = null;
        }
        
        this.alerts = new ArrayList<>(alerts);
        this.size = alerts.size();
    }

    public String getFirstId() {
        return firstId;
    }

    public void setFirstId(String firstId) {
        this.firstId = firstId;
    }

    public String getLastId() {
        return lastId;
    }

    public void setLastId(String lastId) {
        this.lastId = lastId;
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
