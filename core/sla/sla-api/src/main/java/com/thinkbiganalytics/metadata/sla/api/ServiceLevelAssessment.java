/**
 *
 */
package com.thinkbiganalytics.metadata.sla.api;

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

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.Set;

/**
 * The overall assessment of this SLA.
 */
public interface ServiceLevelAssessment extends Comparable<ServiceLevelAssessment>, Serializable {

    /**
     * @return the unique ID of this assessment
     */
    ID getId();

    /**
     * @return the time when this assessment was generated
     */
    DateTime getTime();

    /**
     * @return the SLA that was assessed
     */
    ServiceLevelAgreement getAgreement();

    ServiceLevelAgreementDescription getServiceLevelAgreementDescription();

    ServiceLevelAgreement.ID getServiceLevelAgreementId();

    /**
     * @return a message describing the result of this assessment
     */
    String getMessage();

    /**
     * @return the result status of this assessment
     */
    AssessmentResult getResult();

    /**
     * @return the assessments of each of the obligations of the SLA
     */
    Set<ObligationAssessment> getObligationAssessments();

    interface ID extends Serializable {

    }

}
