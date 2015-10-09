/**
 * 
 */
package com.thinkbiganalytics.metadata.sla.alerts;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.thinkbiganalytics.alerts.api.Alert;
import com.thinkbiganalytics.alerts.spi.AlertManager;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAssessment;

/**
 *
 * @author Sean Felten
 */
public class AssessmentAlertGenerator {
    
    @Inject
    private AlertManager alertManager;

    /**
     * 
     */
    public AssessmentAlertGenerator() {
    }
    
    @PostConstruct
    public void initAlertTypes() {
        this.alertManager.addDescriptor(AssessmentAlerts.VIOLATION_ALERT);
    }
    
    public Alert generateViolationAlert(Alert.Level level, ServiceLevelAssessment assessment) {
        String description = "";
        
        return this.alertManager.create(AssessmentAlerts.VIOLATION_ALERT.getAlertType(), level, description, assessment);
    }

}
