/**
 * 
 */
package com.thinkbiganalytics.metadata.api;

import java.security.Principal;

import com.thinkbiganalytics.security.GroupPrincipal;
import com.thinkbiganalytics.security.UsernamePrincipal;

/**
 * Simple facade over whatever database/transaction mechanism is deployed in the environment where 
 * the metadata API is used.  Applications may inject/obtain and use instances of this type to pass commands
 * that read and manipulate metadata entities via the providers.
 * 
 * @author Sean Felten
 */
public interface MetadataAccess {

    /** A principal representing an anonymous user */
    public static final Principal ANONYMOUS = new UsernamePrincipal("anonymous");
    /** A principal representing a service account */
    public static final Principal SERVICE = new UsernamePrincipal("service");
    /** A principal representing an the admin group */
    public static final Principal ADMIN = new GroupPrincipal("admin");
    
    /**
     * Executes the command and commits any changes using credentials derived by the provided principals.
     * If no principals are provided then the commmand will execute using credentials derived from the current 
     * security context.
     * @param cmd the command to execute
     * @param princials one or more principals, or none to use the current security context
     * @return the result returned from the command
     */
    <R> R commit(Command<R> cmd, Principal... principals);
    
    /**
     * Executes the command in a read-only context using credentials derived by the provided principals.
     * If no principals are provided then the commmand will execute using credentials derived from the current 
     * security context.
     * @param cmd the command to execute
     * @param princials one or more principals, or none to use the current security context
     * @return the result returned from the command
     */
    <R> R read(Command<R> cmd, Principal... principals);

}
