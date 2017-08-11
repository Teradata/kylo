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

import java.util.Collection;

/**
 * A builder for producing obligations for an SLA.
 */
public interface ObligationBuilder<B> {

    /**
     * @param description sets the description
     * @return this builder
     */
    ObligationBuilder<B> description(String description);

    /**
     * @param metric a metric to add to this obligation
     * @param more   optional additional metrics to add
     * @return this builder
     */
    ObligationBuilder<B> metric(Metric metric, Metric... more);

    /**
     * @param metrics metrics to add to this obligation
     * @return this builder
     */
    ObligationBuilder<B> metric(Collection<Metric> metrics);

    /**
     * Builds the obligation and adds it to the SLA that is being built
     *
     * @return the SLA builder that produced this builder
     */
    B build();

}
