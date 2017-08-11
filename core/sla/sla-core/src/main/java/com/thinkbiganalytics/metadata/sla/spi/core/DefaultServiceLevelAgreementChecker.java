package com.thinkbiganalytics.metadata.sla.spi.core;

/*-
 * #%L
 * thinkbig-sla-core
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

import com.thinkbiganalytics.alerts.api.Alert;
import com.thinkbiganalytics.alerts.sla.AssessmentAlerts;
import com.thinkbiganalytics.alerts.spi.AlertManager;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAssessment;
import com.thinkbiganalytics.metadata.sla.spi.AssessorNotFoundException;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementChecker;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementProvider;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAssessmentProvider;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAssessor;
import com.thinkbiganalytics.security.role.SecurityRole;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

/**
 */
public class DefaultServiceLevelAgreementChecker implements ServiceLevelAgreementChecker {


    private static final Logger LOG = LoggerFactory.getLogger(DefaultServiceLevelAgreementChecker.class);

    @Inject
    protected ServiceLevelAgreementProvider slaProvider;

    @Inject
    protected ServiceLevelAssessmentProvider assessmentProvider;

    @Inject
    protected ServiceLevelAssessor assessor;

    @Inject
    @Named("kyloAlertManager")
    protected AlertManager alertManager;


    //store a ref to the last Assessment ID that was alerted
    protected Map<ServiceLevelAgreement.ID, ServiceLevelAssessment.ID> alertedAssessments;


    public DefaultServiceLevelAgreementChecker() {
        this.alertedAssessments = Collections
            .synchronizedMap(new LinkedHashMap<ServiceLevelAgreement.ID, ServiceLevelAssessment.ID>());
    }

    /**
     * Caller should wrap this in commit
     */
    public void checkAgreements() {
        List<? extends ServiceLevelAgreement> list = slaProvider.getAgreements();

        LOG.info("Checking {} service level agreements", list.size());

        for (ServiceLevelAgreement agreement : list) {
            checkAgreement(agreement);
        }

        LOG.info("Completed checking SLAs");


    }

    /**
     * Check the Agreement. Caller needs to wrap this in MetadataAccesss transcation
     */
    public void checkAgreement(ServiceLevelAgreement agreement) {
        if (agreement != null) {
            Alert newAlert = null;
            if (isAssessable(agreement)) {
                LOG.info("Assessing SLA  : " + agreement.getName());

                try {
                    ServiceLevelAssessment assessment = assessor.assess(agreement);

                    if (shouldAlert(agreement, assessment)) {
                        newAlert = alertManager.createEntityAlert(AssessmentAlerts.VIOLATION_ALERT_TYPE,
                                                       Alert.Level.FATAL,
                                                       "Violation of SLA: " + agreement.getName(), alertManager.createEntityIdentificationAlertContent(agreement.getId().toString(),
                                                                                                                                                       SecurityRole.ENTITY_TYPE.SLA,assessment.getId()));


                    }
                } catch (AssessorNotFoundException e) {
                    LOG.info("SLA assessment failed.  Assessor Not found: {} - Exception: {}", agreement.getName(), e);
                }
            }
            if (newAlert != null) {
                // Record this assessment as the latest for this SLA.
                alertedAssessments.put(agreement.getId(), (ServiceLevelAssessment.ID) newAlert.getContent());
                LOG.info("SLA assessment failed: {} - generated alert: {}", agreement.getName(), newAlert.getId());
            }
        }


    }


    /**
     * Determine whether an alert should be generated for this assessment by comparing is to the last one for the same SLA.
     *
     * @return true if the alert should be generated
     */
    protected boolean shouldAlert(ServiceLevelAgreement agreement, ServiceLevelAssessment assessment) {
        boolean shouldAlert = false;
        try {
            // Get the last assessment that was created for this SLA (if any).
            ServiceLevelAssessment previous = null;
            ServiceLevelAssessment.ID previousId = this.alertedAssessments.get(agreement.getId());
            if (previousId != null) {
                previous = this.assessmentProvider.findServiceLevelAssessment(previousId);
            } else {
                previous = this.assessmentProvider.findLatestAssessment(agreement.getId());
            }

            if (previous != null) {

                if (previous.getAgreement() == null && assessment.getServiceLevelAgreementId() != null ) {
                    ServiceLevelAgreement previousAgreement = slaProvider.getAgreement(slaProvider.resolve(assessment.getServiceLevelAgreementId().toString()));

                }

                shouldAlert = assessment.compareTo(previous) != 0;
            } else {
                shouldAlert = true;
            }
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
