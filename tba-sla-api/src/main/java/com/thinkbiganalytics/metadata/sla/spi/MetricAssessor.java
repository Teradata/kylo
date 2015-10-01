/**
 * 
 */
package com.thinkbiganalytics.metadata.sla.spi;

import com.thinkbiganalytics.metadata.sla.api.Metric;

/**
 * An assessor responsible for generating assessments of the types of metrics that it accepts.
 * @author Sean Felten
 * @param <M>
 */
public interface MetricAssessor<M extends Metric> {

    /**
     * Indicates whether this assessor accepts a particular kind of metric
     * @param metric the metric being checked
     * @return true if this assessor should be used to assess the given metric, otherwise false
     */
    <M extends Metric> boolean accepts(M metric);
    
    /**
     * Generates a new assessment of the given metric.
     * @param metric the metric to assess
     * @param builder the builder that this assessor should use to generate the assessment
     */
    void assess(M metric, MetricAssessmentBuilder builder);
}
