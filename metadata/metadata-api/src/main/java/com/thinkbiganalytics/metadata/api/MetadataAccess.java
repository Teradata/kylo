/**
 * 
 */
package com.thinkbiganalytics.metadata.api;

import com.thinkbiganalytics.security.GroupPrincipal;
import com.thinkbiganalytics.security.UsernamePrincipal;

import java.security.Principal;

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
     * Executes the {@link MetadataCommand} and commits any changes using credentials derived by the provided principals.
     * If no principals are provided then the commmand will execute using credentials derived from the current 
     * security context.
     * @param cmd the command to execute
     * @param principals one or more principals, or none to use the current security context
     * @return the result returned from the command
     */
    <R> R commit(MetadataCommand<R> cmd, Principal... principals);

    /**
     * Executes the {@link MetadataCommand} and commits any changes using credentials derived by the provided principals. If no principals are provided then the commmand will execute using credentials
     * derived from the current security context.
     *
     * @param cmd         the command to execute
     * @param rollbackCmd the command to execute if an exception occurs and the transaction is rolled back
     * @param principals  one or more principals, or none to use the current security context
     * @return the result returned from the command
     */
    <R> R commit(MetadataCommand<R> cmd, MetadataRollbackCommand rollbackCmd, Principal... principals);
    
    /**
     * Executes the {@link Runnable} and commits any changes using credentials derived by the provided principals.
     * If no principals are provided then the runnable will execute using credentials derived from the current 
     * security context.
     * @param action the command to execute
     * @param principals one or more principals, or none to use the current security context
     */
    void commit(MetadataAction action, Principal... principals);


    /**
     * Executes the {@link Runnable} and commits any changes using credentials derived by the provided principals. If no principals are provided then the runnable will execute using credentials
     * derived from the current security context. the rollbackAction will be called if an execption is thrown and the transaction is rolledback.  This is intended for users that need to do some
     * additiona work when rolling back
     *
     * @param action     the command to execute
     * @param principals one or more principals, or none to use the current security context
     */
    void commit(MetadataAction action, MetadataRollbackAction rollbackAction, Principal... principals);


    /**
     * Executes the {@link MetadataCommand} in a read-only context using credentials derived by the provided principals.
     * If no principals are provided then the commmand will execute using credentials derived from the current 
     * security context.
     * @param cmd the command to execute
     * @param principals one or more principals, or none to use the current security context
     * @return the result returned from the command
     */
    <R> R read(MetadataCommand<R> cmd, Principal... principals);

    /**
     * Executes the {@link Runnable} in a read-only context using credentials derived by the provided principals.
     * If no principals are provided then the runnable will execute using credentials derived from the current 
     * security context.
     * @param cmd the command to execute
     * @param principals one or more principals, or none to use the current security context
     */
    void read(MetadataAction cmd, Principal... principals);
}
