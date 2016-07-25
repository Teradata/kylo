package com.thinkbiganalytics.metadata.modeshape.sla;

import com.thinkbiganalytics.alerts.api.Alert;
import com.thinkbiganalytics.alerts.api.AlertProvider;
import com.thinkbiganalytics.alerts.api.AlertResponder;
import com.thinkbiganalytics.alerts.api.AlertResponse;
import com.thinkbiganalytics.alerts.sla.AssessmentAlerts;
import com.thinkbiganalytics.metadata.api.Command;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreementAction;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreementActionConfiguration;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAssessment;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementCheck;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAssessmentProvider;
import com.thinkbiganalytics.spring.SpringApplicationContext;

import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by sr186054 on 7/20/16.
 */
public class JcrServiceLevelAgreementActionAlertResponderFactory implements AlertResponder {

    @Inject
    MetadataAccess metadataAccess;

    @Inject
    private ServiceLevelAssessmentProvider assessmentProvider;

    @Inject
    private AlertProvider provider;


    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.api.AlertResponder#alertChange(com.thinkbiganalytics.alerts.api.Alert, com.thinkbiganalytics.alerts.api.AlertResponse)
     */
    @Override
    public void alertChange(Alert alert, AlertResponse response) {
        if (alert.getEvents().get(0).getState() == Alert.State.UNHANDLED) {
            if (alert.getType().equals(AssessmentAlerts.VIOLATION_ALERT.getAlertType())) {
                try {
                    response.inProgress("Handling SLA Alert");
                    handleViolation(alert);
                    response.handle("Handled SLA Alert");
                } catch (Exception e) {
                    response.unHandle("Failed to handle violation");
                }
            }
        }
    }

    private void handleViolation(Alert alert) {
        metadataAccess.read(new Command<Object>() {
            @Override
            public Object execute() {
                ServiceLevelAssessment.ID assessmentId = alert.getContent();
                ServiceLevelAssessment assessment = assessmentProvider.findServiceLevelAssessment(assessmentId);
                ServiceLevelAgreement agreement = assessment.getAgreement();
                if (agreement.getSlaChecks() != null) {
                    for (ServiceLevelAgreementCheck check : agreement.getSlaChecks()) {
                        for (ServiceLevelAgreementActionConfiguration configuration : check.getActionConfigurations()) {
                            List<Class<? extends ServiceLevelAgreementAction>> responders = configuration.getActionClasses();
                            if (responders != null) {
                                //first check to see if there is a Spring Bean configured for this class type... if so call that
                                for (Class<? extends ServiceLevelAgreementAction> responderClass : responders) {
                                    ServiceLevelAgreementAction action = null;
                                    try {
                                        action = SpringApplicationContext.getBean(responderClass);
                                    } catch (NoSuchBeanDefinitionException e) {
                                        //this is ok
                                    }

                                    //if not spring bound then construct the Responder
                                    if (action == null) {
                                        //construct and invoke
                                        try {
                                            action = ConstructorUtils.invokeConstructor(responderClass, null);
                                        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
                                            //TODO LOG error
                                            e.printStackTrace();
                                        }
                                    }
                                    if (action != null) {
                                        //reassign the content of the alert to the ServiceLevelAssessment
                                        action.respond(configuration, assessment, alert);
                                    }
                                }
                            }
                        }
                    }
                }
                return null;
            }


        });
    }


}
