/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.common;

import java.nio.file.Path;

import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;

/**
 * Defines and resolves security-related paths within the JCR repository.
 *
 * @author Sean Felten
 */
public interface SecurityPaths {

    public static final Path METADATA = JcrUtil.path("metadata");
    public static final Path SECURITY = METADATA.resolve("security");
    public static final Path PROTOTYPES = SECURITY.resolve("prototypes");
    
    static Path prototypeActionsPath(String name) {
        return PROTOTYPES.resolve(name);
    }
    
    static Path moduleActionPath(String name) {
        return SECURITY.resolve(name);
    }
}
