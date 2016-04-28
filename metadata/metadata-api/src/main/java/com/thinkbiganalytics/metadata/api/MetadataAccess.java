/**
 * 
 */
package com.thinkbiganalytics.metadata.api;

/**
 * Simple facade over around whatever database/transaction mechanism is deployed in the environment where 
 * the metadata API is used.  Applications may inject/obtain and use instances of this type to pass commands
 * that read and manipulate metadata entities via the providers.
 * 
 * @author Sean Felten
 */
public interface MetadataAccess {
    
    /**
     * Executes the command and commits any changes.
     * @param cmd the command to execute
     * @return the result returned from the command
     */
    <R> R commit(Command<R> cmd);
    
    /**
     * Executes the command in a read-only manner.  Does not affect the underlying metadata store.
     * @param cmd the command to execute
     * @return the result returned from the command
     */
    <R> R read(Command<R> cmd);

}
