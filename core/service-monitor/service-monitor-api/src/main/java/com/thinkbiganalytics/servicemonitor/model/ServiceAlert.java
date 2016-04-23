package com.thinkbiganalytics.servicemonitor.model;

import java.util.Date;

/**
 * Created by sr186054 on 4/13/16.
 */
public interface ServiceAlert {

  String getServiceName();

  void setServiceName(String serviceName);

  String getComponentName();

  void setComponentName(String componentName);

  String getLabel();

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
