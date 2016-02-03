/**
 * 
 */
package com.thinkbiganalytics.metadata.core.dataset;

/**
 *
 * @author Sean Felten
 */
public class DatasetException extends RuntimeException {

    private static final long serialVersionUID = 787147228293321761L;

    /**
     * @param message
     */
    public DatasetException(String message) {
        super(message);
    }

    /**
     * @param message
     * @param cause
     */
    public DatasetException(String message, Throwable cause) {
        super(message, cause);
    }

}
