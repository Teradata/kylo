/**
 * 
 */
package com.thinkbiganalytics.metadata.sla.spi.core;

import com.thinkbiganalytics.metadata.sla.api.AssessmentResult;
import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.api.MetricAssessment;
import com.thinkbiganalytics.metadata.sla.api.Obligation;
import com.thinkbiganalytics.metadata.sla.spi.ObligationAssessmentBuilder;
import com.thinkbiganalytics.metadata.sla.spi.ObligationAssessor;

/**
 *
 * @author Sean Felten
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
        
        if (this.expectedDescription != null && ! obligation.getDescription().equals(this.expectedDescription)) {
            builder
                .message("The expected description does not match: " + this.expectedDescription)
                .compareWith(this.expectedDescription);
            result = AssessmentResult.FAILURE;
        }
        
        builder.result(result);
    }

}
