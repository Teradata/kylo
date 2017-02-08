/**
 *
 */
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

import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.api.Obligation;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAssessment;

import java.io.Serializable;

/**
 * A service for producing assessments SLAs.  It is also used to register obligation and metric assessors
 * that may be delegated the responsibility of producing assessments of their associated obligation/metric types.
 */
public interface ServiceLevelAssessor {

    /**
     * Produces an assessment instance of an SLA, delegating to any registered assessors as necessary.
     *
     * @param sla the SLA to be assessed
     * @return an assessment of the SLA
     */
    ServiceLevelAssessment assess(ServiceLevelAgreement sla);

    ServiceLevelAssessment findLatestAssessment(ServiceLevelAgreement sla);

    /**
     * Registers an assessor of obligations that match its expected obligation type.
     *
     * @param assessor the assessor
     * @return the same assessor (aids registration of new assessor beans in spring by providing this method as a factory method)
     */
    ObligationAssessor<? extends Obligation> registerObligationAssessor(ObligationAssessor<? extends Obligation> assessor);

    /**
     * Registers an assessor of metrics that match its expected obligation type.
     *
     * @param assessor the assessor
     * @return the same assessor (aids registration of new assessor beans in spring by providing this method as a factory method)
     */
    MetricAssessor<? extends Metric, ? extends Serializable> registerMetricAssessor(MetricAssessor<? extends Metric, ? extends Serializable> assessor);
}
