/**
 * 
 */
package com.thinkbiganalytics.metadata.sla.spi;

import java.io.Serializable;
import java.util.Comparator;

import com.thinkbiganalytics.metadata.sla.api.AssessmentResult;
import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.api.MetricAssessment;
import com.thinkbiganalytics.metadata.sla.api.Obligation;
import com.thinkbiganalytics.metadata.sla.api.ObligationAssessment;

/**
 * The builder that is used to generate assessment of obligations.
 * @author Sean Felten
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
         * @param value a comparable value to use in comparisons
         * @param otherValeus any additional comparables
         * @return this builder
         */
        @SuppressWarnings("unchecked")
        ObligationAssessmentBuilder compareWith(Comparable<? extends Serializable> value, Comparable<? extends Serializable>... otherValeus);
        
        /**
         * Used to add a new metric assessment to this obligation assessment.  The appropriate metric assessor will
         * be looked up to perform the assessment.
         * @param metric the metric to assess
         * @return the metric's assessment
         */
        <M extends Metric> MetricAssessment assess(M metric);
}
