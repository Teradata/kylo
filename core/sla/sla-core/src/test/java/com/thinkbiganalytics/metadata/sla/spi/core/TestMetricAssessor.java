/**
 *
 */
package com.thinkbiganalytics.metadata.sla.spi.core;

/*-
 * #%L
 * thinkbig-sla-core
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
import com.thinkbiganalytics.metadata.sla.spi.MetricAssessmentBuilder;
import com.thinkbiganalytics.metadata.sla.spi.MetricAssessor;

import java.io.Serializable;

/**
 *
 */
public class TestMetricAssessor implements MetricAssessor<TestMetric, Serializable> {

    private int expectedIntValue;
    private String expectedStringValue;
    private AssessmentResult failResult;
    private AssessmentResult successResult;

    public TestMetricAssessor(int expectedIntValue, String expectedStringValue) {
        this(expectedIntValue, expectedStringValue, AssessmentResult.FAILURE, AssessmentResult.SUCCESS);
    }

    public TestMetricAssessor(int expectedIntValue, String expectedStringValue, AssessmentResult failResult) {
        this(expectedIntValue, expectedStringValue, failResult, AssessmentResult.SUCCESS);
    }

    public TestMetricAssessor(int expectedIntValue,
                              String expectedStringValue,
                              AssessmentResult failResult,
                              AssessmentResult successResult) {
        super();
        this.expectedIntValue = expectedIntValue;
        this.expectedStringValue = expectedStringValue;
        this.failResult = failResult;
        this.successResult = successResult;
    }

    @Override
    public boolean accepts(Metric metric) {
        return metric instanceof TestMetric;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void assess(TestMetric metric, MetricAssessmentBuilder builder) {
        builder
            .compareWith(this.expectedIntValue, this.expectedStringValue)
            .metric(metric);

        if (metric.getIntValue() == this.expectedIntValue && metric.getStringValue().equals(this.expectedStringValue)) {
            builder
                .message("Found expected values: " + this.expectedIntValue + " \"" + this.expectedStringValue + "\"")
                .result(this.successResult);
        } else {
            builder
                .message("Expected values not found: " + this.expectedIntValue + " \"" + this.expectedStringValue + "\"")
                .result(this.failResult);
        }
    }

}
