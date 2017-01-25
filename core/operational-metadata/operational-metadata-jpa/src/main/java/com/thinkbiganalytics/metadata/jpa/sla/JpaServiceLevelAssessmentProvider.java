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

    public ServiceLevelAssessment save(ServiceLevelAssessment assessment) {
        return this.serviceLevelAssessmentRepository.save((JpaServiceLevelAssessment) assessment);
    }

    @Override
    public List<? extends ServiceLevelAssessment> getAssessments() {
        return serviceLevelAssessmentRepository.findAll();
    }


    //find last assessment
    public ServiceLevelAssessment findLatestAssessment(ServiceLevelAgreement.ID slaId) {
        List<? extends ServiceLevelAssessment> latestAssessments = serviceLevelAssessmentRepository.findLatestAssessments(slaId.toString());
        if (latestAssessments != null) {
            JpaServiceLevelAssessment jpaServiceLevelAssessment = (JpaServiceLevelAssessment) latestAssessments.get(0);
            ensureServiceLevelAgreementOnAssessment(jpaServiceLevelAssessment);
            return jpaServiceLevelAssessment;
        }
        return null;
    }


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

    @Override
    public ServiceLevelAssessment findServiceLevelAssessment(ServiceLevelAssessment.ID id) {
        ServiceLevelAssessment assessment = serviceLevelAssessmentRepository.findOne(id);
        return assessment;
    }


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
