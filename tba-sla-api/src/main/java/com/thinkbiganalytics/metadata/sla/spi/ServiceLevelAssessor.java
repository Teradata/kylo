/**
 * 
 */
package com.thinkbiganalytics.metadata.sla.spi;

import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.api.Obligation;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAssessment;

/**
 *
 * @author Sean Felten
 */
public interface ServiceLevelAssessor {

    ServiceLevelAssessment assess(ServiceLevelAgreement sla);
    
    void registerObligationAssessor(ObligationAssessor<? extends Obligation> assessor);
    
    void registerMetricAssessor(MetricAssessor<? extends Metric> assessor);
}
