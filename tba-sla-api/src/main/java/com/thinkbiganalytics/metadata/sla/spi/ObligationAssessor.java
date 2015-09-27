/**
 * 
 */
package com.thinkbiganalytics.metadata.sla.spi;

import com.thinkbiganalytics.metadata.sla.api.Obligation;

/**
 *
 * @author Sean Felten
 */
public interface ObligationAssessor<O extends Obligation> {
    
    boolean accepts(Obligation obligation);
    
    void assess(O obligation, ObligationAssessmentBuilder builder);
}
