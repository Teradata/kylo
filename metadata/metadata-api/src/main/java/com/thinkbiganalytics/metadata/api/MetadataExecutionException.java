/**
 * 
 */
package com.thinkbiganalytics.metadata.api;

/**
 * Wraps any unhandled exception that is thrown within a MetadataCommand or MetadataAction.
 * 
 * @author Sean Felten
 */
public class MetadataExecutionException extends MetadataException {

    private static final long serialVersionUID = -8584258657040637162L;

    /**
     * @param message
     * @param cause
     */
    public MetadataExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     */
    public MetadataExecutionException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public MetadataExecutionException(Throwable cause) {
        super(cause);
    }

}
