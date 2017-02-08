/**
 *
 */
package com.thinkbiganalytics.metadata.core.sla;

/*-
 * #%L
 * thinkbig-metadata-core
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

import com.thinkbiganalytics.metadata.api.sla.TestMetric;
import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.spi.MetricAssessmentBuilder;
import com.thinkbiganalytics.metadata.sla.spi.MetricAssessor;

import java.io.Serializable;

/**
 *
 */
public class TestMetricAssessor implements MetricAssessor<TestMetric, Serializable> {

    @Override
    public boolean accepts(Metric metric) {
        return metric instanceof TestMetric;
    }

    @Override
    public void assess(TestMetric metric, MetricAssessmentBuilder<Serializable> builder) {
        builder
            .metric(metric)
            .result(metric.getResult())
            .message(metric.getMessage());
    }

}
