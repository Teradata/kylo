/**
 * 
 */
package com.thinkbiganalytics.metadata.sla.spi;

import java.io.Serializable;
import java.util.Comparator;

import com.thinkbiganalytics.metadata.sla.api.AssessmentResult;
import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.api.MetricAssessment;

/**
 * An assessor responsible for generating assessments of the types of metrics that it accepts.
 * @author Sean Felten
 */
public interface MetricAssessmentBuilder {

    /**
     * @param metric the metric being assessed
     * @return this builder
     */
    MetricAssessmentBuilder metric(Metric metric);

    /**
     * @param descr the message describing the result of the assessment
     * @return this builder
     */
    MetricAssessmentBuilder message(String descr);
    
    /**
     * @param comp the comparator for this assessment
     * @return this builder
     */
    MetricAssessmentBuilder comparitor(Comparator<MetricAssessment> comp);
    
    /**
     * Generates a comparator for this assessment that uses each comparable in its comparison.
     * @param value a comparable value to use in comparisons
     * @param otherValeus any additional comparables
     * @return this builder
     */
    @SuppressWarnings("unchecked")
    MetricAssessmentBuilder compartWith(Comparable<? extends Serializable> value, Comparable<? extends Serializable>... otherValues);

    /**
     * @param result the result status of this assessment
     * @return this builder
     */
    MetricAssessmentBuilder result(AssessmentResult result);

}
