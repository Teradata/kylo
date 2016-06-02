/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape;

/**
 *
 * @author Sean Felten
 */
public class MetadataRepositoryException extends RuntimeException {

    private static final long serialVersionUID = -3693942950368082938L;

    /**
     * @param message
     */
    public MetadataRepositoryException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public MetadataRepositoryException(String message, Throwable cause) {
        super(cause);
    }

}
