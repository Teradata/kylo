/**
 * 
 */
package com.thinkbiganalytics.metadata.upgrade;

/**
 *
 */
public class UpgradeException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * @param message
     */
    public UpgradeException(String message) {
        super(message);
    }

    /**
     * @param message
     * @param cause
     */
    public UpgradeException(String message, Throwable cause) {
        super(message, cause);
    }
}
