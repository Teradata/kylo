/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.common;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Defines and resolves security-related paths within the JCR repository.
 *
 * @author Sean Felten
 */
public interface SecurityPaths {

    public static final Path METADATA = Paths.get("metadata");
    public static final Path SECURITY = METADATA.resolve("security");
    public static final Path PROTOTYPES = SECURITY.resolve("prototypes");
    
    static Path prototypeActionsPath(String name) {
        return PROTOTYPES.resolve(name);
    }
    
    static Path moduleActionPath(String name) {
        return SECURITY.resolve(name);
    }
}
