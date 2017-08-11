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

import com.thinkbiganalytics.metadata.sla.api.AssessmentResult;
import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.api.MetricAssessment;

import java.io.Serializable;
import java.util.Comparator;

/**
 * An assessor responsible for generating assessments of the types of metrics that it accepts.
 */
public interface MetricAssessmentBuilder<D extends Serializable> {

    /**
     * @param metric the metric being assessed
     * @return this builder
     */
    MetricAssessmentBuilder<D> metric(Metric metric);

    /**
     * @param descr the message describing the result of the assessment
     * @return this builder
     */
    MetricAssessmentBuilder<D> message(String descr);

    /**
     * @param comp the comparator for this assessment
     * @return this builder
     */
    MetricAssessmentBuilder<D> comparitor(Comparator<MetricAssessment<D>> comp);

    /**
     * Generates a comparator for this assessment that uses each comparable in its comparison.
     *
     * @param value       a comparable value to use in comparisons
     * @param otherValues any additional comparables
     * @return this builder
     */
    @SuppressWarnings("unchecked")
    MetricAssessmentBuilder<D> compareWith(Comparable<? extends Serializable> value, Comparable<? extends Serializable>... otherValues);

    /**
     * Allows attaching arbitrary, undefined data to this assessment.  The consumer of the assessment must
     * know what kind of data is expected.
     *
     * @param data arbitrary data associated with this assessment.
     * @return D
     */
    MetricAssessmentBuilder<D> data(D data);

    /**
     * @param result the result status of this assessment
     * @return this builder
     */
    MetricAssessmentBuilder<D> result(AssessmentResult result);

}
