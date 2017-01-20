package com.thinkbiganalytics.metadata.sla;


import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.sla.api.AssessmentResult;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAssessment;
import com.thinkbiganalytics.metadata.sla.spi.core.DefaultServiceLevelAgreementChecker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * SLA Checker that will lookup the SLA data from JCR and use it with the JPA SLA Assessments Created by sr186054 on 9/15/16.
 */
public class JpaJcrServiceLevelAgreementChecker extends DefaultServiceLevelAgreementChecker {


    @Inject
    JcrMetadataAccess jcrMetadataAccess;

    private static final Logger LOG = LoggerFactory.getLogger(JpaJcrServiceLevelAgreementChecker.class);


    public JpaJcrServiceLevelAgreementChecker() {
        super();
    }

    @Override
    protected boolean shouldAlert(ServiceLevelAgreement agreement, ServiceLevelAssessment assessment) {
        boolean shouldAlert = false;
        try {
            shouldAlert = jcrMetadataAccess.read(() -> {
            // Get the last assessment that was created for this SLA (if any).
            ServiceLevelAssessment previous = this.assessmentProvider.findLatestAssessmentNotEqualTo(agreement.getId(), assessment.getId());

            if (previous != null) {
                assessmentProvider.ensureServiceLevelAgreementOnAssessment(previous);
                LOG.debug("found previous assessment {} ", previous.getClass());

                return (!assessment.getResult().equals(AssessmentResult.SUCCESS) && assessment.compareTo(previous) != 0);
            } else {
                return true;
            }
        });
        } catch (Exception e) {
            LOG.error("Error checking shouldAlert for {}. {} ", agreement.getName(), e.getMessage(), e);
        }
        return shouldAlert;
    }

    private boolean isAssessable(ServiceLevelAgreement agreement) {
        // TODO: validate that this is a kind of agreement that we assess.  Assume we assess all SLAs for now.
        return true;
    }


}
