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
    
    ObligationAssessor<Obligation> registerObligationAssessor(ObligationAssessor<Obligation> assessor);
    
    MetricAssessor<Metric> registerMetricAssessor(MetricAssessor<Metric> assessor);
}
