/**
 * 
 */
package com.thinkbiganalytics.metadata.sla.api;

/**
 * The possible result of an assessment of an SLA, obligation, or metic.
 * 
 * @author Sean Felten
 */
public enum AssessmentResult {
    SUCCESS, WARNING, FAILURE;
    
    /**
     * Returns whether this result or the argument is of more severe result (i.e. which is closer to failure)
     * 
     * @param result the result to be compared
     * @return this result or the argument depending upon which is more severe of a result
     */
    public AssessmentResult max(AssessmentResult result) {
        return result.ordinal() > this.ordinal() ? result : this;
    }
}
