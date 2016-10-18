/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.common;

import java.nio.file.Path;

import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;

/**
 * Defines and resolves paths of users and user groups within the JCR repository.
 * 
 * @author Sean Felten
 */
public interface UsersPaths {

    public static final Path USERS = JcrUtil.path("users");
    public static final Path GROUPS = JcrUtil.path("groups");
    
    static Path userPath(String name) {
        return USERS.resolve(name);
    }
    
    static Path groupPath(String name) {
        return GROUPS.resolve(name);
    }
}
