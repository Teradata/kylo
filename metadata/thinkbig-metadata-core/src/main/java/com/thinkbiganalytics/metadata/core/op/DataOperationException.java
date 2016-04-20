/**
 * 
 */
package com.thinkbiganalytics.metadata.core.op;

/**
 *
 * @author Sean Felten
 */
public class DataOperationException extends RuntimeException {

    private static final long serialVersionUID = 4164695875440412101L;

    public DataOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataOperationException(String message) {
        super(message);
    }

}
