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

import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementCheck;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * Describes an SLA, which is a collection of obligations that must be met when assessed.
 */
public interface ServiceLevelAgreement {

    /**
     * @return the unique ID of this SLA
     */
    ID getId();

    ;

    /**
     * @return the name of this SLA
     */
    String getName();

    /**
     * @return the time when this SLA was created
     */
    DateTime getCreatedTime();

    /**
     * @return a description of this SLA
     */
    String getDescription();

    /**
     * Gets all of the obligation groups of this SLA in assessment order.  The list
     * returned will always have at least 1 group (the default group) of none of the
     * obligations of this SLA have been explicitly grouped.
     *
     * @return all the obligation groups
     */
    List<ObligationGroup> getObligationGroups();

    /**
     * Gets all obligations from that exist in this SLA, in assessment order, regardless
     * of how they are grouped.
     *
     * @return all obligations that make up this SLA
     */
    List<Obligation> getObligations();
    
    /**
     * Convenience method to get all metrics of all obligations found in this SLA regardless of 
     * which obligations they are organized under.
     * @return the set of all metrics
     */
    Set<Metric> getAllMetrics();

    List<ServiceLevelAgreementCheck> getSlaChecks();

    boolean isEnabled();

    interface ID extends Serializable {

    }

}
