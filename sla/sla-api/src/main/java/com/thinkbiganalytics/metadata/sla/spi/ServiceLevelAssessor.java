/**
 * 
 */
package com.thinkbiganalytics.metadata.sla.spi;

import java.io.Serializable;

import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.api.Obligation;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAssessment;

/**
 * A service for producing assessments SLAs.  It is also used to register obligation and metric assessors
 * that may be delegated the responsibility of producing assessments of their associated obligation/metric types. 
 * 
 * @author Sean Felten
 */
public interface ServiceLevelAssessor {

    /**
     * Produces an assessment instance of an SLA, delegating to any registered assessors as necessary.
     * @param sla the SLA to be assessed
     * @return an assessment of the SLA
     */
    ServiceLevelAssessment assess(ServiceLevelAgreement sla);
    
    /**
     * Registers an assessor of obligations that match its expected obligation type.
     * @param assessor the assessor
     * @return the same assessor (aids registration of new assessor beans in spring by providing this method as a factory method)
     */
    ObligationAssessor<? extends Obligation> registerObligationAssessor(ObligationAssessor<? extends Obligation> assessor);
    
    /**
     * Registers an assessor of metrics that match its expected obligation type.
     * @param assessor the assessor
     * @return the same assessor (aids registration of new assessor beans in spring by providing this method as a factory method)
     */
    MetricAssessor<? extends Metric, ? extends Serializable> registerMetricAssessor(MetricAssessor<? extends Metric, ? extends Serializable> assessor);
}
