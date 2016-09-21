package com.thinkbiganalytics.metadata.sla;


import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
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
        return jcrMetadataAccess.read(() -> {
            // Get the last assessment that was created for this SLA (if any).
            ServiceLevelAssessment previous = null;
            ServiceLevelAssessment.ID previousId = this.alertedAssessments.get(agreement.getId());
            if (previousId != null) {
                previous = this.assessmentProvider.findServiceLevelAssessment(previousId);
            } else {
                previous = this.assessmentProvider.findLatestAssessment(agreement.getId());
            }

            if (previous != null) {
                assessmentProvider.ensureServiceLevelAgreementOnAssessment(previous);

                return assessment.compareTo(previous) != 0;
            } else {
                return true;
            }
        });
    }

    private boolean isAssessable(ServiceLevelAgreement agreement) {
        // TODO: validate that this is a kind of agreement that we assess.  Assume we assess all SLAs for now.
        return true;
    }


}
