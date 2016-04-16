package com.thinkbiganalytics.servicemonitor.model;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by sr186054 on 4/13/16.
 */
public interface ServiceComponent {
    String DEFAULT_CLUSTER = "Default";

    ServiceAlert.STATE getHighestAlertState();

    Map<String, Object> getProperties();

    String getServiceName();

    String getName();

    boolean isHealthy();

    Date getCheckDate();

    String getMessage();

    STATE getState();

    List<ServiceAlert> getAlerts();

    boolean isContainsErrorAlerts();

    String getClusterName();

    List<ServiceAlert> getErrorAlerts();

    Date getLatestAlertTimestamp();

    Date getEarliestAlertTimestamp();

    void setServiceName(String serviceName);

    ServiceAlert.STATE getMaxAlertState();

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
