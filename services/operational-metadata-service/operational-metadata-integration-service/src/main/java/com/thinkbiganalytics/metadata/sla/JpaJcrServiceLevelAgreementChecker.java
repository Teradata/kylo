package com.thinkbiganalytics.metadata.sla;

/*-
 * #%L
 * thinkbig-operational-metadata-integration-service
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.sla.api.AssessmentResult;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAssessment;
import com.thinkbiganalytics.metadata.sla.spi.core.DefaultServiceLevelAgreementChecker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * SLA Checker that will lookup the SLA data from JCR and use it with the JPA SLA Assessments.
 */
public class JpaJcrServiceLevelAgreementChecker extends DefaultServiceLevelAgreementChecker {


    private static final Logger LOG = LoggerFactory.getLogger(JpaJcrServiceLevelAgreementChecker.class);

    @Inject
    MetadataAccess metadataAccess;

    /**
     * Runs the assessment provider on the provided agreement and acts accordingly.
     *
     * @param agreement  The agreement to assess
     * @param assessment The strategy of assessment
     * @return true if the assessment succeeds or is not found
     */
    @Override
    protected boolean shouldAlert(ServiceLevelAgreement agreement, ServiceLevelAssessment assessment) {
        boolean shouldAlert = false;
        try {
            shouldAlert = metadataAccess.read(() -> {
                // Get the last assessment that was created for this SLA (if any).
                ServiceLevelAssessment previous = this.assessmentProvider.findLatestAssessmentNotEqualTo(agreement.getId(), assessment.getId());
                boolean alert = false;
                if (previous != null) {
                    assessmentProvider.ensureServiceLevelAgreementOnAssessment(previous);
                    boolean matchesPreviousAssessment = assessment.compareTo(previous) == 0;
                    alert = (!assessment.getResult().equals(AssessmentResult.SUCCESS) && !matchesPreviousAssessment);
                    LOG.debug("{}. {} ",alert ? "Generating an alert ":" Not generating an alert " , matchesPreviousAssessment ? "This assessment is the same as the previous assessment": "This assessment is different than the previous assessment");
                } else {
                    LOG.debug("Unable to find a previous assessment for agreement {}.  {}", agreement.getId(),alert ? "Generating Alert ":" Not generating an alert " );
                    alert = !assessment.getResult().equals(AssessmentResult.SUCCESS);
                }
                return alert;
            },MetadataAccess.SERVICE);
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
