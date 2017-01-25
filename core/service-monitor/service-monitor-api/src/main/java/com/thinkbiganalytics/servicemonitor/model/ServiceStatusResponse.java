package com.thinkbiganalytics.servicemonitor.model;

import java.util.Date;
import java.util.List;

/**
 * When a Service Status check is made it expects this object to be returned.
 * This includes the Service and respective {@link ServiceComponent} and {@link ServiceAlert} objects as well as information to quickly assess if the service is healthy or not
 *
 * Refer to the service-monitor-core for common builders in creating this object
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
