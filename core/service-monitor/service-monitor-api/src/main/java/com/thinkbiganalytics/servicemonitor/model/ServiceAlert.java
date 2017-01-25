package com.thinkbiganalytics.servicemonitor.model;

import java.util.Date;

/**
 * Health information for a service
 * A Service  can be configured to have a number of Components which it should check for health.
 * Each component can provide a number of ServiceAlert indicating the overall healh of the given component
 *
 * Refer to the service-monitor-core for builder objects to create these alerts
 */
public interface ServiceAlert {

    /**
     * The Service Name
     */
    String getServiceName();

  void setServiceName(String serviceName);

    /**
     * The name of the component for this alert
     */
    String getComponentName();

  void setComponentName(String componentName);

  /**
   * The label shown on the service component -> alert details page
   * @return
   */
  String getLabel();

  /**
   * Set the label for this alert
   * @param label
   */
  void setLabel(String label);

  String getMessage();

  void setMessage(String message);

  Date getFirstTimestamp();

  void setFirstTimestamp(Date firstTimestamp);

  Date getLatestTimestamp();

  void setLatestTimestamp(Date latestTimestamp);

  STATE getState();

  void setState(STATE state);

  public enum STATE {
    OK(1), UNKNOWN(2), WARNING(3), CRITICAL(4);
    private int severity;

    STATE(int severity) {
      this.severity = severity;
    }

    public int getSeverity() {
      return this.severity;
    }

    public boolean isError() {
      return this.severity > 1;
    }

    public boolean isHealthy() {
      return this.severity == 1;
    }
  }
}
