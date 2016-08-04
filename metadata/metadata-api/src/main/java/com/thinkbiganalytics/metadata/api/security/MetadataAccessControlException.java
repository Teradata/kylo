/**
 * 
 */
package com.thinkbiganalytics.metadata.api.security;

import com.thinkbiganalytics.metadata.api.MetadataException;

/**
 *
 * @author Sean Felten
 */
public class MetadataAccessControlException extends MetadataException {

    private static final long serialVersionUID = 1L;

    public MetadataAccessControlException(String message, Throwable cause) {
        super(message, cause);
    }

    public MetadataAccessControlException(String message) {
        super(message);
    }
}
