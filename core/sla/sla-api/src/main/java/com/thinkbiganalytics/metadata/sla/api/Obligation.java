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

import java.io.Serializable;
import java.util.Set;

/**
 * Describes an obligation that must be met as part of an SLA.
 */
public interface Obligation {

    ID getId();

    ;

    /**
     * @return a description of the obligation
     */
    String getDescription();

    /**
     * @return the SLA of which this obligation is a part
     */
    ServiceLevelAgreement getAgreement();

    /**
     * @return the group of which this obligation is a part
     */
    ObligationGroup getGroup();

    /**
     * @return the metrics of this obligation that are measured when this obligation is assessed
     */
    Set<Metric> getMetrics();

    interface ID extends Serializable {

    }

}
