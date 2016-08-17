/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.common;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author Sean Felten
 */
public interface SecurityPaths {

    public static final Path METADATA = Paths.get("/metadata");
    public static final Path SECURITY = METADATA.resolve("security");
    public static final Path PROTOTYPES = SECURITY.resolve("prototypes");
    
    default Path prototypeActionsPath(String name) {
        return PROTOTYPES.resolve(name);
    }
    
    default Path actionGroupPath(String name) {
        return PROTOTYPES.resolve(name);
    }
}
