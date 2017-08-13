package com.thinkbiganalytics.metadata.sla.spi;

/*-
 * #%L
 * thinkbig-sla-api
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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.Serializable;
import java.util.List;

/**
 */
public interface ServiceLevelAssessmentProvider {


    List<? extends ServiceLevelAssessment> getAssessments();

    ServiceLevelAssessment findServiceLevelAssessment(ServiceLevelAssessment.ID id);

    ServiceLevelAssessment findLatestAssessment(ServiceLevelAgreement.ID slaId);

    ServiceLevelAssessment findLatestAssessmentNotEqualTo(ServiceLevelAgreement.ID slaId, ServiceLevelAssessment.ID assessmentId);


    ServiceLevelAssessment.ID resolveId(Serializable id);
    /**
     * Ensure the assessment.getAgreement() is not null and if it is it will query and get the correct agreement according to the slaId on the assessment
     */
    boolean ensureServiceLevelAgreementOnAssessment(ServiceLevelAssessment assessment);


    /**
     * Find all Assessments  with the provided filter.
     *
     * @return a paged result set of all the SLA assessments matching the incoming filter
     */
     Page<? extends ServiceLevelAssessment> findAll(String filter, Pageable pageable);



    }
