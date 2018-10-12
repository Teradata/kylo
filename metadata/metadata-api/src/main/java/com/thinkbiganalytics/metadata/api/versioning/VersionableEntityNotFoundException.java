/**
 * 
 */
package com.thinkbiganalytics.metadata.api.versioning;

import com.thinkbiganalytics.metadata.api.MetadataException;

import java.io.Serializable;

/**
 *
 */
public class VersionableEntityNotFoundException extends MetadataException {

    private static final long serialVersionUID = 1L;

    private final Serializable id;
    
    public VersionableEntityNotFoundException(Serializable id) {
        this("No versionable entity exists with the ID: " + id, id);
    }

    public VersionableEntityNotFoundException(String message, Serializable id) {
        super(message);
        this.id = id;
    }
    
    /**
     * @return the id
     */
    public Serializable getId() {
        return id;
    }
}
