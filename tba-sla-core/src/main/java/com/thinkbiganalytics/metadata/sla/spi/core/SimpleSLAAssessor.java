/**
 * 
 */
package com.thinkbiganalytics.metadata.sla.spi.core;

import com.thinkbiganalytics.metadata.sla.api.AssessmentResult;
import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.api.MetricAssessment;
import com.thinkbiganalytics.metadata.sla.api.Obligation;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAssessment;
import com.thinkbiganalytics.metadata.sla.spi.MetricAssessor;
import com.thinkbiganalytics.metadata.sla.spi.ObligationAssessmentBuilder;
import com.thinkbiganalytics.metadata.sla.spi.ObligationAssessor;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAssessor;

/**
 *
 * @author Sean Felten
 */
public class SimpleSLAAssessor implements ServiceLevelAssessor {

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAssessor#assess(com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement)
     */
    @Override
    public ServiceLevelAssessment assess(ServiceLevelAgreement sla) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAssessor#registerAssessor(com.thinkbiganalytics.metadata.sla.spi.ObligationAssessor)
     */
    @Override
    public void registerObligationAssessor(ObligationAssessor<? extends Obligation> assessor) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAssessor#registerAssessor(com.thinkbiganalytics.metadata.sla.spi.MetricAssessor)
     */
    @Override
    public void registerMetricAssessor(MetricAssessor<? extends Metric> assessor) {
        // TODO Auto-generated method stub

    }

    
    
    private class DefaultObligationAssessor implements ObligationAssessor<Obligation> {

        @Override
        public boolean accepts(Obligation obligation) {
            // Accepts any obligations
            return true;
        }

        @Override
        public void assess(Obligation obligation, ObligationAssessmentBuilder builder) {
            boolean success = true;
            
            // Iterate through and assess each metric.
            // Obligation is considered successful if all metrics are successful
//            for (Metric metric : obligation.getMetrics()) {
//                MetricAssessment assessment = assessMetric(metric);
//                success |= assessment.getResult() == AssessmentResult.SUCCESS;
//            }
//            
//            if (success) {
//                builder.
//            }
        }
        
    }
}
