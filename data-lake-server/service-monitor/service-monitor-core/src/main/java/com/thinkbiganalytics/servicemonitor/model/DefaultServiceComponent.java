/*
 * Copyright (c) 2015.
 */

package com.thinkbiganalytics.servicemonitor.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sr186054 on 9/30/15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DefaultServiceComponent implements ServiceComponent {

  private String clusterName;
  private String serviceName;
  private String name;
  private boolean healthy;
  private String message;
  private Date checkDate;
  private List<ServiceAlert> alerts;
  private List<ServiceAlert> errorAlerts;
  private boolean containsErrorAlerts;
  private Map<String, Object> properties;
  private STATE state;
  private ServiceAlert.STATE maxAlertState;

  public DefaultServiceComponent(Builder builder) {
    this.clusterName = builder.clusterName;
    this.serviceName = builder.serviceName;
    this.name = builder.name;
    this.healthy = builder.healthy;
    this.message = builder.message;
    this.checkDate = new Date();
    this.properties = builder.properties;
    this.alerts = builder.alerts;
    this.state = builder.state;
    this.errorAlerts = this.getErrorAlerts();
    this.containsErrorAlerts = (errorAlerts != null && !errorAlerts.isEmpty());
    this.maxAlertState = getHighestAlertState();

  }

  public DefaultServiceAlert.STATE getHighestAlertState() {
    DefaultServiceAlert.STATE maxState = null;
    if (this.alerts != null) {
      for (ServiceAlert alert : alerts) {
        if (alert.getState().isError()) {
          if (maxState == null) {
            maxState = alert.getState();
          } else {
            if (alert.getState().getSeverity() > maxState.getSeverity()) {
              maxState = alert.getState();
            }
          }
        }
      }
    }
    return maxState;
  }

  public static class Builder {

    private String clusterName = ServiceComponent.DEFAULT_CLUSTER;
    private String serviceName;
    private String name;
    private boolean healthy = true;
    private String message;
    private List<ServiceAlert> alerts;
    private Map<String, Object> properties;
    private STATE state;

    public Builder(String clusterName, String serviceName, String componentName, STATE state) {
      this.clusterName = clusterName;
      this.serviceName = serviceName;
      this.name = componentName;
      this.state = state;
    }

    public Builder(String componentName, STATE state) {
      this.name = componentName;
      this.state = state;
    }

    public Builder serviceName(String serviceName) {
      this.serviceName = serviceName;
      return this;
    }

    public Builder clusterName(String clusterName) {
      this.clusterName = clusterName;
      return this;
    }

    public Builder message(String message) {
      this.message = message;
      return this;
    }

    public Builder properties(Map<String, Object> properties) {
      if (this.properties == null) {
        this.properties = new HashMap<>();
      }
      this.properties.putAll(properties);
      return this;
    }

    public Builder alerts(List<ServiceAlert> alerts) {
      this.alerts = alerts;
      if (this.alerts != null) {
        for (ServiceAlert alert : alerts) {
          if (alert.getState().isError()) {
            this.healthy = false;
          }
        }
      }
      return this;
    }

    public Builder property(String key, Object value) {
      if (this.properties == null) {
        this.properties = new HashMap<>();
      }
      this.properties.put(key, value);
      return this;
    }

    public Builder exception(Throwable e) {
      this.message = e.getMessage();
      this.healthy = false;
      return this;
    }

    public ServiceComponent build() {
      if (this.healthy) {
        this.healthy = state.isHealthy();
      }
      ServiceComponent health = new DefaultServiceComponent(this);
      return health;
    }
  }

  public Map<String, Object> getProperties() {
    return properties;
  }

  public String getServiceName() {
    return serviceName;
  }

  public String getName() {
    return name;
  }

  public boolean isHealthy() {
    return healthy;
  }

  public Date getCheckDate() {
    return checkDate;
  }

  public String getMessage() {
    return message;
  }

  public STATE getState() {
    return state;
  }

  public List<ServiceAlert> getAlerts() {
    return alerts;
  }

  public boolean isContainsErrorAlerts() {
    return containsErrorAlerts;
  }

  public String getClusterName() {
    return clusterName;
  }

  public List<ServiceAlert> getErrorAlerts() {
    boolean hasErrors = false;
    if (alerts != null && !alerts.isEmpty()) {
      Predicate<ServiceAlert> predicate = new Predicate<ServiceAlert>() {
        @Override
        public boolean apply(ServiceAlert alert) {
          return alert.getState().isError();
        }
      };
      Collection<ServiceAlert> matchingAlerts = Collections2.filter(alerts, predicate);
      if (matchingAlerts != null && !matchingAlerts.isEmpty()) {
        return new ArrayList<ServiceAlert>(matchingAlerts);
      }
    }
    return null;
  }

  private Date getEarliestOrLatestAlertTimestamp(TIMESTAMP_TYPE timestampType, boolean onlyErrors) {
    ServiceAlert latestAlert = null;
    Date latestTime = null;
    List<ServiceAlert> alerts = getAlerts();
    if (alerts != null) {
      for (ServiceAlert alert : alerts) {
        Date time = alert.getLatestTimestamp();
        if (time != null && ((onlyErrors && alert.getState().isError()) || !onlyErrors) && (latestTime == null || (
            timestampType.equals(TIMESTAMP_TYPE.LATEST) && latestTime != null && time.after(latestTime)) ||
                                                                                            (timestampType
                                                                                                 .equals(TIMESTAMP_TYPE.EARLIEST)
                                                                                             && latestTime != null && time
                                                                                                 .before(latestTime)))) {
          latestAlert = alert;
          latestTime = time;
        }
      }
    }
    return latestTime;
  }

  public Date getLatestAlertTimestamp() {
    Date date = null;
    if (this.containsErrorAlerts) {
      date = getEarliestOrLatestAlertTimestamp(TIMESTAMP_TYPE.LATEST, true);
    } else {
      date = getEarliestOrLatestAlertTimestamp(TIMESTAMP_TYPE.LATEST, false);
    }
    return date;
  }

  public Date getEarliestAlertTimestamp() {
    Date date = null;
    if (this.containsErrorAlerts) {
      date = getEarliestOrLatestAlertTimestamp(TIMESTAMP_TYPE.EARLIEST, true);
    } else {
      date = getEarliestOrLatestAlertTimestamp(TIMESTAMP_TYPE.EARLIEST, false);
    }
    return date;
  }

  public void setServiceName(String serviceName) {
    this.serviceName = serviceName;
  }

  public DefaultServiceAlert.STATE getMaxAlertState() {
    return maxAlertState;
  }

}
