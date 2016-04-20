/**
 * 
 */
package com.thinkbiganalytics.metadata.api;

/**
 *
 * @author Sean Felten
 */
public class MetadataException extends RuntimeException {

    private static final long serialVersionUID = 353707089141114163L;

    public MetadataException() {
        super();
    }

    public MetadataException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public MetadataException(String message, Throwable cause) {
        super(message, cause);
    }

    public MetadataException(String message) {
        super(message);
    }

    public MetadataException(Throwable cause) {
        super(cause);
    }

    
}
