/**
 * 
 */
package com.thinkbiganalytics.metadata.sla.spi;

import com.thinkbiganalytics.metadata.sla.api.AssessmentResult;
import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.api.MetricAssessment;
import com.thinkbiganalytics.metadata.sla.api.Obligation;

/**
 *
 * @author Sean Felten
 */
public interface ObligationAssessmentBuilder {

        ObligationAssessmentBuilder obligation(Obligation ob);

        ObligationAssessmentBuilder result(AssessmentResult result);
        
        ObligationAssessmentBuilder message(String descr);
        
        MetricAssessment assess(Metric metric);

//        ObligationAssessment build();
}
