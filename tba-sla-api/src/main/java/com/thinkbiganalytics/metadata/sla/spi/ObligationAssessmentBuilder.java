/**
 * 
 */
package com.thinkbiganalytics.metadata.sla.spi;

import com.thinkbiganalytics.metadata.sla.api.AssessmentResult;
import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.api.MetricAssessment;
import com.thinkbiganalytics.metadata.sla.api.Obligation;

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
         * Used to add a new metric assessment to this obligation assessment.  The appropriate metric assessor will
         * be looked up to perform the assessment.
         * @param metric the metric to assess
         * @return the metric's assessment
         */
        <M extends Metric> MetricAssessment assess(M metric);
}
