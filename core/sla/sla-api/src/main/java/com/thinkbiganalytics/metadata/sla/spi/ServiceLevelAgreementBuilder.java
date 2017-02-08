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

import com.thinkbiganalytics.metadata.sla.api.Obligation;
import com.thinkbiganalytics.metadata.sla.api.ObligationGroup;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreementActionConfiguration;

import java.util.List;

/**
 * A builder for creating new SLAs.
 */
public interface ServiceLevelAgreementBuilder {

    /**
     * @param name sets the SLA name
     * @return this builder
     */
    ServiceLevelAgreementBuilder name(String name);

    /**
     * @param description sets the description
     * @return this builder
     */
    ServiceLevelAgreementBuilder description(String description);

    /**
     * Adds an obligation to the default group of this SLA.
     *
     * @param obligation the obligation to add
     * @return this builder
     */
    ServiceLevelAgreementBuilder obligation(Obligation obligation);

    /**
     * Produces a builder for adding a new obligation to the default group of this SLA.
     *
     * @return the obligation builder
     */
    ObligationBuilder<ServiceLevelAgreementBuilder> obligationBuilder();

    /**
     * Convenience method that produces a builder for adding a new obligation within its own
     * group governed by the specified condition.
     *
     * @return the obligation builder
     */
    ObligationBuilder<ServiceLevelAgreementBuilder> obligationBuilder(ObligationGroup.Condition condition);


    /**
     * Produces a builder for an obligation group to be added to this SLA under the given condition.
     *
     * @param condition the condition controlling how this group will contribute to this SLA's assessment.
     * @return the obligation builder
     */
    ObligationGroupBuilder obligationGroupBuilder(ObligationGroup.Condition condition);

    /**
     * Assign any Action Config options to be passed to the AlertResponder to execute specific responses when the SLA is violated
     */
    ServiceLevelAgreementBuilder actionConfigurations(List<? extends ServiceLevelAgreementActionConfiguration> actionConfigurations);

    /**
     * Generates the SLA and adds it to the provider that produce this builder
     *
     * @return the SLA that was created and added to the provider
     */
    ServiceLevelAgreement build();
}
