/**
 * 
 */
package com.thinkbiganalytics.alerts.api;

/**
 *
 * @author Sean Felten
 */
public class AlertNotfoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final Alert.ID alertId;

    public AlertNotfoundException(Alert.ID id) {
        this(id, "An alert with the given ID does not exists: " + id);
    }

    public AlertNotfoundException(Alert.ID id, String message) {
        super(message);
        this.alertId = id;
    }
    
    /**
     * @return the alertId
     */
    public Alert.ID getAlertId() {
        return alertId;
    }
}
