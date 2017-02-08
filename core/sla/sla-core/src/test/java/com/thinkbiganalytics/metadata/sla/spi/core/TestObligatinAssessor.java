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
import com.thinkbiganalytics.metadata.sla.api.MetricAssessment;
import com.thinkbiganalytics.metadata.sla.api.Obligation;
import com.thinkbiganalytics.metadata.sla.spi.ObligationAssessmentBuilder;
import com.thinkbiganalytics.metadata.sla.spi.ObligationAssessor;

/**
 *
 */
public class TestObligatinAssessor implements ObligationAssessor<Obligation> {

    private String expectedDescription;

    public TestObligatinAssessor() {
    }

    public TestObligatinAssessor(String expectedDescription) {
        super();
        this.expectedDescription = expectedDescription;
    }

    @Override
    public boolean accepts(Obligation obligation) {
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void assess(Obligation obligation, ObligationAssessmentBuilder builder) {
        AssessmentResult result = AssessmentResult.SUCCESS;

        for (Metric metric : obligation.getMetrics()) {
            MetricAssessment assessment = builder.assess(metric);
            result = result.max(assessment.getResult());
        }

        if (this.expectedDescription != null) {
            builder.compareWith(this.expectedDescription);

            if (!obligation.getDescription().equals(this.expectedDescription)) {
                builder.message("The expected description does not match: " + this.expectedDescription);
                result = AssessmentResult.FAILURE;
            }
        }

        builder.result(result);
    }

    protected void setExpectedDescription(String expectedDescription) {
        this.expectedDescription = expectedDescription;
    }
}
