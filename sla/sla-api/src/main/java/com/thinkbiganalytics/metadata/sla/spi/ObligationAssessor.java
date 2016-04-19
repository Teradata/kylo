/**
 * 
 */
package com.thinkbiganalytics.metadata.sla.spi;

import com.thinkbiganalytics.metadata.sla.api.Obligation;

/**
 * An assessor responsible for generating assessments of the types of obligations that it accepts.
 * @author Sean Felten
 */
public interface ObligationAssessor<O extends Obligation> {
    
    /**
     * Indicates whether this assessor accepts a particular kind of obligation
     * @param obligation the obligation being checked
     * @return true if this assessor should be used to assess the given obligation, otherwise false
     */
    boolean accepts(Obligation obligation);
    
    /**
     * Generates a new assessment of the given obligation.
     * @param obligation the obligation to assess
     * @param builder the builder that this assessor should use to generate the assessment
     */
    void assess(O obligation, ObligationAssessmentBuilder builder);
}
