/**
 * 
 */
package com.thinkbiganalytics.metadata.api.security;

import java.security.AccessControlException;

/**
 *
 * @author Sean Felten
 */
public class MetadataAccessControlException extends AccessControlException {

    private static final long serialVersionUID = 1L;

    public MetadataAccessControlException(String message) {
        super(message);
    }
}
