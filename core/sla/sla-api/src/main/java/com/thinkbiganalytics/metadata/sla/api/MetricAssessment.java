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

import java.io.Serializable;

/**
 * Reports an assessment of a metric.
 */
public interface MetricAssessment<D extends Serializable> extends Comparable<MetricAssessment<D>>, Serializable {

    /**
     * @return the metric that was assessed
     */
    Metric getMetric();

    /**
     * @return the description about the metric
     */
    String getMetricDescription();

    /**
     * @return a message describing the assessment result
     */
    String getMessage();

    /**
     * @return the result status of the assessment
     */
    AssessmentResult getResult();

    /**
     * Gets some arbitrary data that was attached to this assessment.  The type of data is
     * undefined and must be known by the consumer of this assessment.
     *
     * @return the data attached to this assessment
     */
    D getData();

}
