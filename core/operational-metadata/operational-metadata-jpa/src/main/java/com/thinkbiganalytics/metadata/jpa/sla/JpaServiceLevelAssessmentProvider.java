/**
 *
 */
package com.thinkbiganalytics.metadata.jpa.sla;

/*-
 * #%L
 * thinkbig-operational-metadata-jpa
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

import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAssessment;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementProvider;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAssessmentProvider;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;

/**
 * Provider accessing the {@link JpaServiceLevelAssessment}
 */
@Service
public class JpaServiceLevelAssessmentProvider implements ServiceLevelAssessmentProvider {

    private static final Logger log = LoggerFactory.getLogger(JpaServiceLevelAssessmentProvider.class);

    private JpaServiceLevelAssessmentRepository serviceLevelAssessmentRepository;

    @Inject
    private ServiceLevelAgreementProvider slaProvider;

    /**
     * metadataAccess.commit(() -> { ServiceLevelAgreement sla = slaProvider.getAgreement(slaId);
     */


    @Autowired
    public JpaServiceLevelAssessmentProvider(JpaServiceLevelAssessmentRepository serviceLevelAssessmentRepository) {

        this.serviceLevelAssessmentRepository = serviceLevelAssessmentRepository;
    }


    public ServiceLevelAssessment.ID resolveId(Serializable ser) {
        if (ser instanceof JpaServiceLevelAssessment.ID) {
            return (JpaServiceLevelAssessment.ID) ser;
        } else {
            return new JpaServiceLevelAssessment.SlaAssessmentId(ser);
        }
    }

    /**
     * save an sla assessment to the database
     *
     * @param assessment the assessment to save
     * @return the saved assessment
     */
    public ServiceLevelAssessment save(ServiceLevelAssessment assessment) {
        return this.serviceLevelAssessmentRepository.save((JpaServiceLevelAssessment) assessment);
    }

    /**
     * Find all SLA assessments
     * @return find all the SLA assessments
     */
    @Override
    public List<? extends ServiceLevelAssessment> getAssessments() {
        return serviceLevelAssessmentRepository.findAll();
    }


    /**
     * find the latest assessment
     * @param slaId the service level agreement id
     * @return the latest assessment for the sla
     */
    public ServiceLevelAssessment findLatestAssessment(ServiceLevelAgreement.ID slaId) {
        List<? extends ServiceLevelAssessment> latestAssessments = serviceLevelAssessmentRepository.findLatestAssessments(slaId.toString());
        if (latestAssessments != null) {
            JpaServiceLevelAssessment jpaServiceLevelAssessment = (JpaServiceLevelAssessment) latestAssessments.get(0);
            ensureServiceLevelAgreementOnAssessment(jpaServiceLevelAssessment);
            return jpaServiceLevelAssessment;
        }
        return null;
    }


    /**
     * Find the latest SLA that doesn't match the incoming {@code assessmentId}
     * @param slaId the id to reference
     * @param assessmentId the assessment id to cehck
     * @return the latest SLA that doesn't match the incoming {@code assessmentId}
     */
    @Override
    public ServiceLevelAssessment findLatestAssessmentNotEqualTo(ServiceLevelAgreement.ID slaId, ServiceLevelAssessment.ID assessmentId) {
        if (assessmentId != null) {
            List<? extends ServiceLevelAssessment> latestAssessments = serviceLevelAssessmentRepository.findLatestAssessmentsNotEqualTo(slaId.toString(), assessmentId);
            if (latestAssessments != null && !latestAssessments.isEmpty()) {
                return latestAssessments.get(0);
            } else {
                return null;
            }
        } else {
            return findLatestAssessment(slaId);
        }
    }

    /**
     * Find a ServiceLevelAssessment by its id
     * @param id the id of the sla assessment
     * @return the matching ServiceLevelAssement
     */
    @Override
    public ServiceLevelAssessment findServiceLevelAssessment(ServiceLevelAssessment.ID id) {
        ServiceLevelAssessment assessment = serviceLevelAssessmentRepository.findOne(id);
        return assessment;
    }


    /**
     * Makes sure the Service Level Assessment object has its respective SLA attached to it for reference lookups
     * @param assessment the assessment to check and ensure it has its SLA attached
     * @return {@code true} if it was able to attach the SLA to the assessment, {@code false} if it was not about to attach and find the SLA
     */
    public boolean ensureServiceLevelAgreementOnAssessment(ServiceLevelAssessment assessment) {
        if (assessment != null && assessment.getAgreement() != null) {
            return true;
        }
        if (assessment.getAgreement() == null && StringUtils.isNotBlank(assessment.getServiceLevelAgreementId())) {
            ServiceLevelAgreement agreement = slaProvider.getAgreement(slaProvider.resolve(assessment.getServiceLevelAgreementId()));
            ((JpaServiceLevelAssessment) assessment).setAgreement(agreement);
        }
        return assessment != null && assessment.getAgreement() != null;
    }


}
