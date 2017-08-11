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
import com.thinkbiganalytics.metadata.sla.api.Obligation;
import com.thinkbiganalytics.metadata.sla.api.ObligationAssessment;

import java.io.Serializable;
import java.util.Comparator;

/**
 * The builder that is used to generate assessment of obligations.
 */
public interface ObligationAssessmentBuilder {

    /**
     * @param ob the obligation being assessed
     * @return this builder
     */
    ObligationAssessmentBuilder obligation(Obligation ob);

    /**
     * @param result the result status of the assessment
     * @return this builder
     */
    ObligationAssessmentBuilder result(AssessmentResult result);

    /**
     * @param descr a message describing the assessment result
     * @return this builder
     */
    ObligationAssessmentBuilder message(String descr);

    /**
     * @param comp the comparator for this assessment
     * @return this builder
     */
    ObligationAssessmentBuilder comparator(Comparator<ObligationAssessment> comp);

    /**
     * Generates a comparator for this assessment that uses each comparable in its comparison.
     *
     * @param value       a comparable value to use in comparisons
     * @param otherValeus any additional comparables
     * @return this builder
     */
    @SuppressWarnings("unchecked")
    ObligationAssessmentBuilder compareWith(Comparable<? extends Serializable> value, Comparable<? extends Serializable>... otherValeus);

    /**
     * Used to add a new metric assessment to this obligation assessment.  The appropriate metric assessor will
     * be looked up to perform the assessment.
     *
     * @param metric the metric to assess
     * @return M the metric's assessment
     */
    <M extends Metric> MetricAssessment assess(M metric);
}
