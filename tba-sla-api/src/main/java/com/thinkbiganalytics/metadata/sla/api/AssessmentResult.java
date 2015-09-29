/**
 * 
 */
package com.thinkbiganalytics.metadata.sla.api;

/**
 *
 * @author Sean Felten
 */
public enum AssessmentResult {
    SUCCESS, WARNING, FAILURE;
    
    public AssessmentResult max(AssessmentResult ar) {
        return ar.ordinal() > this.ordinal() ? ar : this;
    }
}
