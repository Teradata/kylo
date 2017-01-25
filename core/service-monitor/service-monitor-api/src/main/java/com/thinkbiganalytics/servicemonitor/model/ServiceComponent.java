package com.thinkbiganalytics.servicemonitor.model;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * A service can monitor 1 or more components.
 * Each component has its own health status along with the overal Service health status
 */
public interface ServiceComponent {

  String DEFAULT_CLUSTER = "Default";

    /**
     * Returns the Highest/Most severe alert state for alerts found on this component
     */
    ServiceAlert.STATE getHighestAlertState();

    /**
     * Get any additional properties to display on the Kylo UI for the service component
     */
    Map<String, Object> getProperties();

  /**
   *
   * @return the associated service name for this component
   */
  String getServiceName();

  /**
   *
   * @return the name for this component
   */
  String getName();

  /**
   *
   * @return true if healthy, false if unhealthy
   */
  boolean isHealthy();

  /**
   *
   * @return the date last checked
   */
  Date getCheckDate();

  /**
   *
   * @return a message indicating some description of this component and alert status
   */
  String getMessage();

  /**
   * Get the state of this component
   * @return the state of the component
   */
  STATE getState();

  /**
   * Get associated alerts for this component
   * @return a list of alert objects
   */
  List<ServiceAlert> getAlerts();

  /**
   * if there are any error alerts
   * @return
   */
  boolean isContainsErrorAlerts();

  /**
   * if associated with a cluster, provide the cluster name the component is on
   * @return the name of the cluster
   */
  String getClusterName();

  /**
   * find any error alerts
   * @return a list of alerts marked as errors
   */
  List<ServiceAlert> getErrorAlerts();

  /**
   * find the latest alert time for this component
   * @return the latest timestamp for this alert
   */
  Date getLatestAlertTimestamp();

  /**
   * find the earliest alert time for the list of alerts on this component
   * @return the earliest date for the alerts on this component
   */
  Date getEarliestAlertTimestamp();

  void setServiceName(String serviceName);

  /**
   * Return the most severe alert state
   * @return
   */
  ServiceAlert.STATE getMaxAlertState();


  /**
   * The state of a given Component
   */
  public enum STATE {
    UP(1), STARTING(2), DOWN(3), UNKNOWN(4);
    private int severity;

    STATE(int severity) {
      this.severity = severity;
    }

    public int getSeverity() {
      return this.severity;
    }

    public boolean isError() {
      return this.severity > 2;
    }

    public boolean isHealthy() {
      return this.severity <= 2;
    }
  }

  public enum TIMESTAMP_TYPE {
    EARLIEST, LATEST
  }
}
