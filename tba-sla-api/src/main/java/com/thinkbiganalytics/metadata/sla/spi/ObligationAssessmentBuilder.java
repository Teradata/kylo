/**
 * 
 */
package com.thinkbiganalytics.metadata.sla.spi;

import com.thinkbiganalytics.metadata.sla.api.AssessmentResult;
import com.thinkbiganalytics.metadata.sla.api.AssessmentSeverity;
import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.api.Obligation;

/**
 *
 * @author Sean Felten
 */
public interface ObligationAssessmentBuilder {

        ObligationAssessmentBuilder obligation(Obligation ob);

        ObligationAssessmentBuilder result(AssessmentResult result);

        ObligationAssessmentBuilder severity(AssessmentSeverity severity);
        
        ObligationAssessmentBuilder assess(Metric metric);

}
