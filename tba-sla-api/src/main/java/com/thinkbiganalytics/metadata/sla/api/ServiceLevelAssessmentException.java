/**
 * 
 */
package com.thinkbiganalytics.metadata.sla.api;

/**
 *
 * @author Sean Felten
 */
public class ServiceLevelAssessmentException extends RuntimeException {
    private static final long serialVersionUID = 7236695892099702703L;

    public ServiceLevelAssessmentException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServiceLevelAssessmentException(String message) {
        super(message);
    }
}
