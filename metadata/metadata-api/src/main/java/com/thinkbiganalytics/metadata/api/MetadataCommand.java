/**
 * 
 */
package com.thinkbiganalytics.metadata.api;

/**
 * A command to execute in the context of a transaction.
 * @author Sean Felten
 */
public interface MetadataCommand<R> {
    
    R execute() throws Exception;
}
