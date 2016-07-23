package com.thinkbiganalytics.metadata.sla.alerts;

import com.thinkbiganalytics.alerts.api.Alert;
import com.thinkbiganalytics.alerts.api.AlertProvider;
import com.thinkbiganalytics.alerts.api.AlertResponder;
import com.thinkbiganalytics.alerts.api.AlertResponse;
import com.thinkbiganalytics.alerts.sla.AssessmentAlerts;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreementAction;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreementActionConfiguration;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAssessment;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementCheck;
import com.thinkbiganalytics.spring.SpringApplicationContext;

import org.apache.commons.lang3.reflect.ConstructorUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by sr186054 on 7/20/16.
 */
public class ServiceLevelAgreementActionAlertResponderFactory implements AlertResponder {


    @Inject
    private AlertProvider provider;


    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.api.AlertResponder#alertChange(com.thinkbiganalytics.alerts.api.Alert, com.thinkbiganalytics.alerts.api.AlertResponse)
     */
    @Override
    public void alertChange(Alert alert, AlertResponse response) {
        if (alert.getType().equals(AssessmentAlerts.VIOLATION_ALERT.getAlertType())) {
            try {
                response.inProgress("Handling volation");
                handleViolation(alert);
                response.handle("Handled violation");
                response.handle();
            } catch (Exception e) {
                response.unHandle("Failed to handle violation");
            }
        }
    }

    private void handleViolation(Alert alert) {
        ServiceLevelAssessment assessment = alert.getContent();
        ServiceLevelAgreement agreement = assessment.getAgreement();
        if (agreement.getSlaChecks() != null) {
            for (ServiceLevelAgreementCheck check : agreement.getSlaChecks()) {
                for (ServiceLevelAgreementActionConfiguration configuration : check.getActionConfigurations()) {
                List<Class<? extends ServiceLevelAgreementAction>> responders = configuration.getActionClasses();
                if (responders != null) {
                    //first check to see if there is a Spring Bean configured for this class type... if so call that
                    for (Class<? extends ServiceLevelAgreementAction> responderClass : responders) {
                        ServiceLevelAgreementAction responder = SpringApplicationContext.getBean(responderClass);

                        //if not spring bound then construct the Responder
                        if (responder == null) {
                            //construct and invoke
                            try {
                                responder = ConstructorUtils.invokeConstructor(responderClass, null);
                            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
                                //TODO LOG error
                                e.printStackTrace();
                            }
                        }
                        if (responder != null) {
                            responder.respond(configuration, alert);
                        }
                    }
                }
                }
            }
        }


    }


}
