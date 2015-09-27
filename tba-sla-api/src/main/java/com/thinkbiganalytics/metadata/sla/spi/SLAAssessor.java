/**
 * 
 */
package com.thinkbiganalytics.metadata.sla.spi;

import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.api.Obligation;
import com.thinkbiganalytics.metadata.sla.api.SLA;
import com.thinkbiganalytics.metadata.sla.api.SLAAssessment;

/**
 *
 * @author Sean Felten
 */
public interface SLAAssessor {

    SLAAssessment assess(SLA sla);
    
    void registerObligationAssessor(ObligationAssessor<? extends Obligation> assessor);
    
    void registerMetricAssessor(MetricAssessor<? extends Metric> assessor);
}
