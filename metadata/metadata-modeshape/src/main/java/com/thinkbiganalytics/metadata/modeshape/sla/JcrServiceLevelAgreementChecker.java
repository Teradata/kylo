/**
 *
 */
package com.thinkbiganalytics.metadata.modeshape.sla;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thinkbiganalytics.alerts.api.Alert;
import com.thinkbiganalytics.alerts.api.Alert.Level;
import com.thinkbiganalytics.alerts.sla.AssessmentAlerts;
import com.thinkbiganalytics.alerts.spi.AlertManager;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.sla.api.AssessmentResult;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAssessment;
import com.thinkbiganalytics.metadata.sla.spi.AssessorNotFoundException;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementChecker;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementProvider;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAssessmentProvider;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAssessor;

/**
 * Reads all existing (currently) SLAs and assesses them.  If any assessments fail then it will generate an SLA violation alert for each failure.
 *
 * @author Sean Felten
 */
public class JcrServiceLevelAgreementChecker implements ServiceLevelAgreementChecker {

    private static final Logger LOG = LoggerFactory.getLogger(JcrServiceLevelAgreementChecker.class);

    @Inject
    private ServiceLevelAgreementProvider slaProvider;

    @Inject
    private ServiceLevelAssessmentProvider assessmentProvider;

    @Inject
    private ServiceLevelAssessor assessor;

    @Inject
    private AlertManager alertManager;

    @Inject
    private MetadataAccess metadataAccess;

    //store a ref to the last Assessment ID that was alerted
    private Map<ServiceLevelAgreement.ID, ServiceLevelAssessment.ID> alertedAssessments;


    public JcrServiceLevelAgreementChecker() {
        this.alertedAssessments = Collections
            .synchronizedMap(new LinkedHashMap<ServiceLevelAgreement.ID, ServiceLevelAssessment.ID>());
    }

    public void checkAgreements() {
        metadataAccess.read(() -> {
            List<? extends ServiceLevelAgreement> list = slaProvider.getAgreements();

            LOG.info("Checking {} service level agreements", list.size());

            for (ServiceLevelAgreement agreement : list) {
                checkAgreement(agreement);
            }

            LOG.info("Completed checking SLAs");
        });

    }

    public void checkAgreement(ServiceLevelAgreement agreement) {
        if(agreement != null) {
            Alert alert = metadataAccess.commit(() -> {
                Alert newAlert = null;
                if (isAssessable(agreement)) {
                    LOG.info("Assessing SLA: " + agreement.getName());

                    try {
                        ServiceLevelAssessment assessment = assessor.assess(agreement);

                        if (assessment.getResult() != AssessmentResult.SUCCESS && shouldAlert(agreement, assessment)) {
                            newAlert = alertManager.create(AssessmentAlerts.VIOLATION_ALERT_TYPE,
                                                        Level.FATAL,
                                                        "Violation of SLA: " + agreement.getName(), assessment.getId());


                        }
                    } catch (AssessorNotFoundException e) {
                        LOG.info("SLA assessment failed.  Assessor Not found: {} - Exception: {}", agreement.getName(), e);
                    }
                }
                return newAlert;
            });
            if (alert != null) {
                // Record this assessment as the latest for this SLA.
                alertedAssessments.put(agreement.getId(), (ServiceLevelAssessment.ID) alert.getContent());
                LOG.info("SLA assessment failed: {} - generated alert: {}", agreement.getName(), alert.getId());
            }
        }


    }


    /**
     * Determine whether an alert should be generated for this assessment by comparing is to the last one for the same SLA.
     *
     * @return true if the alert should be generated
     */
    private boolean shouldAlert(ServiceLevelAgreement agreement, ServiceLevelAssessment assessment) {
        // Get the last assessment that was created for this SLA (if any).
        ServiceLevelAssessment previous = null;
        ServiceLevelAssessment.ID previousId = this.alertedAssessments.get(agreement.getId());
        if (previousId != null) {
            previous = this.assessmentProvider.findServiceLevelAssessment(previousId);
        } else {
            previous = this.assessmentProvider.findLatestAssessment(agreement.getId());
        }

        if (previous != null) {
            return assessment.compareTo(previous) != 0;
        } else {
            return true;
        }
    }

    private boolean isAssessable(ServiceLevelAgreement agreement) {
        // TODO: validate that this is a kind of agreement that we assess.  Assume we assess all SLAs for now.
        return true;
    }

}
