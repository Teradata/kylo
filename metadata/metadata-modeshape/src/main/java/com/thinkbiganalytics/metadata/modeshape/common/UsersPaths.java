/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.common;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Defines and resolves paths of users and user groups within the JCR repository.
 * 
 * @author Sean Felten
 */
public interface UsersPaths {

    public static final Path USERS = Paths.get("users");
    public static final Path GROUPS = Paths.get("groups");
    
    static Path userPath(String name) {
        return USERS.resolve(name);
    }
    
    static Path groupPath(String name) {
        return GROUPS.resolve(name);
    }
}
