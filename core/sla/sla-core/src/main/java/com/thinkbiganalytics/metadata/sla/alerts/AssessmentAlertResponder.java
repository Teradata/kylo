/**
 * 
 */
package com.thinkbiganalytics.metadata.sla.alerts;

import com.thinkbiganalytics.alerts.api.Alert;
import com.thinkbiganalytics.alerts.api.AlertResponder;
import com.thinkbiganalytics.alerts.api.AlertResponse;
import com.thinkbiganalytics.alerts.sla.AssessmentAlerts;

/**
 *
 * @author Sean Felten
 */
public class AssessmentAlertResponder implements AlertResponder {

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.api.AlertResponder#alertChange(com.thinkbiganalytics.alerts.api.Alert, com.thinkbiganalytics.alerts.api.AlertResponse)
     */
    @Override
    public void alertChange(Alert alert, AlertResponse response) {
        if (alert.getType().equals(AssessmentAlerts.VIOLATION_ALERT)) {
            try {
                response.inProgress("Handling volation");
                handleViolation(alert);
                response.handle("Handled violation");
            } catch (Exception e) {
                response.unhandle("Failed to handle violation");
            }
        }
    }

    private void handleViolation(Alert alert) {

    }

}
