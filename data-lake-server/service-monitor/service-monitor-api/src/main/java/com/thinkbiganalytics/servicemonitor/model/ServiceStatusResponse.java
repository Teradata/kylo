package com.thinkbiganalytics.servicemonitor.model;

import java.util.Date;
import java.util.List;

/**
 * Created by sr186054 on 4/13/16.
 */
public interface ServiceStatusResponse {
    String getServiceName();

    List<ServiceComponent> getComponents();

    List<ServiceComponent> getHealthyComponents();

    List<ServiceComponent> getUnhealthyComponents();

    Date getCheckDate();

    List<ServiceAlert> getAlerts();

    List<ServiceAlert> getAlertsWithoutComponent();

    STATE getState();

    Date getLatestAlertTimestamp();

    Date getEarliestAlertTimestamp();

    public enum STATE {
        UP, DOWN, WARNING;
    }
}
