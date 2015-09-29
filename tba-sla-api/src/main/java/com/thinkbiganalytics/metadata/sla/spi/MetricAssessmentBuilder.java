/**
 * 
 */
package com.thinkbiganalytics.metadata.sla.spi;

import com.thinkbiganalytics.metadata.sla.api.AssessmentResult;
import com.thinkbiganalytics.metadata.sla.api.Metric;

/**
 *
 * @author Sean Felten
 */
public interface MetricAssessmentBuilder {

    MetricAssessmentBuilder metric(Metric metric);

    MetricAssessmentBuilder message(String descr);

    MetricAssessmentBuilder result(AssessmentResult result);

}
