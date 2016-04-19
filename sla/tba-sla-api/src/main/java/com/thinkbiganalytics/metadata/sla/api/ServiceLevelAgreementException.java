/**
 * 
 */
package com.thinkbiganalytics.metadata.sla.api;

/**
 *
 * @author Sean Felten
 */
public class ServiceLevelAgreementException extends RuntimeException {

    private static final long serialVersionUID = -6350855489286578608L;

    /**
     * @param message
     */
    public ServiceLevelAgreementException(String message) {
        super(message);
    }

    /**
     * @param message
     * @param cause
     */
    public ServiceLevelAgreementException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     * @param cause
     * @param enableSuppression
     * @param writableStackTrace
     */
    public ServiceLevelAgreementException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
