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

import java.io.Serializable;

/**
 * An assessor responsible for generating assessments of the types of metrics that it accepts.
 */
public interface MetricAssessor<M extends Metric, D extends Serializable> {

    /**
     * Indicates whether this assessor accepts a particular kind of metric
     *
     * @param metric the metric being checked
     * @return true if this assessor should be used to assess the given metric, otherwise false
     */
    boolean accepts(Metric metric);

    /**
     * Generates a new assessment of the given metric.
     *
     * @param metric  the metric to assess
     * @param builder the builder that this assessor should use to generate the assessment
     */
    void assess(M metric, MetricAssessmentBuilder<D> builder);
}
