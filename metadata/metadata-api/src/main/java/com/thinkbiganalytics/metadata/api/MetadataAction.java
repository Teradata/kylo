/**
 * 
 */
package com.thinkbiganalytics.metadata.api;

/**
 * A command to execute in the context of a transaction.
 * @author Sean Felten
 */
public interface MetadataAction {
    
    void execute() throws Exception;
}
