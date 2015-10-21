/**
 * 
 */
package com.thinkbiganalytics.metadata.sla.spi.core;

import com.thinkbiganalytics.metadata.sla.api.AssessmentResult;
import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.spi.MetricAssessmentBuilder;
import com.thinkbiganalytics.metadata.sla.spi.MetricAssessor;

/**
 *
 * @author Sean Felten
 */
public class TestMetricAssessor implements MetricAssessor<TestMetric> {

    private int expectedIntValue;
    private String expectedStringValue;
    private AssessmentResult failResult;

    public TestMetricAssessor(int expectedIntValue, String expectedStringValue) {
        this(expectedIntValue, expectedStringValue, AssessmentResult.FAILURE);
    }
    
    public TestMetricAssessor(int expectedIntValue, String expectedStringValue, AssessmentResult failResult) {
        super();
        this.expectedIntValue = expectedIntValue;
        this.expectedStringValue = expectedStringValue;
        this.failResult = failResult;
    }

    @Override
    public <M extends Metric> boolean accepts(M metric) {
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
                .result(AssessmentResult.SUCCESS);
        } else {
            builder
                .message("Expected values not found: " + this.expectedIntValue + " \"" + this.expectedStringValue + "\"")
                .result(this.failResult);
        }
    }

}
