/**
 * 
 */
package com.thinkbiganalytics.metadata.api;

/**
 * Thrown when there was a framework problem accessing metadata. 
 * 
 * @author Sean Felten
 */
public class MetadataAccessException extends MetadataException {

    private static final long serialVersionUID = 2232711559874661176L;

    /**
     * @param message
     * @param cause
     */
    public MetadataAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     */
    public MetadataAccessException(String message) {
        super(message);
    }

}
