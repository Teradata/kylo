/**
 * 
 */
package com.thinkbiganalytics.metadata.sla.spi;

import com.thinkbiganalytics.metadata.sla.api.AssessmentResult;
import com.thinkbiganalytics.metadata.sla.api.Metric;

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
     * @param result the result status of this assessment
     * @return this builder
     */
    MetricAssessmentBuilder result(AssessmentResult result);

}
