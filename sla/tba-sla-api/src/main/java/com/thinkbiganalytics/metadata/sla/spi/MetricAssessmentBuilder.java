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
     * @param value a comparable value to use in comparisons
     * @param otherValeus any additional comparables
     * @return this builder
     */
    @SuppressWarnings("unchecked")
    MetricAssessmentBuilder<D> compareWith(Comparable<? extends Serializable> value, Comparable<? extends Serializable>... otherValues);
    
    /**
     * Allows attaching arbitrary, undefined data to this assessment.  The consumer of the assessment must
     * know what kind of data is expected.
     * @param data arbitrary data associated with this assessment.
     * @return
     */
    MetricAssessmentBuilder<D> data(D data);

    /**
     * @param result the result status of this assessment
     * @return this builder
     */
    MetricAssessmentBuilder<D> result(AssessmentResult result);

}
