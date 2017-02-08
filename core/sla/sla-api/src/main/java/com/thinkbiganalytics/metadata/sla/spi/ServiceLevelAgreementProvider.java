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

import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;

import java.io.Serializable;
import java.util.List;

/**
 * A provider for creating and managing SLAs.
 */
public interface ServiceLevelAgreementProvider {

    // TODO Add criteria-based SLA search methods

    /**
     * Resolves an ID from a serialized form, for instance the string result from the toSting() method of an ID.
     *
     * @param ser some serializable form
     * @return a reconstituted SLA ID
     */
    ServiceLevelAgreement.ID resolve(Serializable ser);

    /**
     * @return a set of all SLAs
     */
    List<ServiceLevelAgreement> getAgreements();

    /**
     * @param id the ID of an SLA
     * @return the SLA corresponding to the given ID, or null if the SLA no longer exists
     */
    ServiceLevelAgreement getAgreement(ServiceLevelAgreement.ID id);

    /**
     * Search for an SLA by name.
     *
     * @param slaName to name to match
     * @return the SLA or null if not found
     */
    // TODO: remove/deprecate this method when criteria-based search methods are added
    ServiceLevelAgreement findAgreementByName(String slaName);

    /**
     * Removes an SLA with the given ID.
     *
     * @param id the ID of an SLA
     * @return true of the SLA existed and was SLA, otherwise false
     */
    boolean removeAgreement(ServiceLevelAgreement.ID id);

    /**
     * Produces a new builder for creating a new SLA.
     *
     * @return the builder
     */
    ServiceLevelAgreementBuilder builder();

    /**
     * Produces a new builder for creating a new SLA that replaces another one having the given ID.
     *
     * @param id the ID of the SLA that will be replaced
     * @return the builder
     */
    ServiceLevelAgreementBuilder builder(ServiceLevelAgreement.ID id);

    ServiceLevelAgreementCheckBuilder slaCheckBuilder(ServiceLevelAgreement.ID slaId);

}
