/**
 * 
 */
package com.thinkbiganalytics.metadata.core.dataset;

/**
 *
 * @author Sean Felten
 */
public class DatasourceException extends RuntimeException {

    private static final long serialVersionUID = 787147228293321761L;

    /**
     * @param message
     */
    public DatasourceException(String message) {
        super(message);
    }

    /**
     * @param message
     * @param cause
     */
    public DatasourceException(String message, Throwable cause) {
        super(message, cause);
    }

}
