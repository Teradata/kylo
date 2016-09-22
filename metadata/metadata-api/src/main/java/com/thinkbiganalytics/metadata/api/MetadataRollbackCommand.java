/**
 *
 */
package com.thinkbiganalytics.metadata.api;

/**
 * A command to execute in the context of a transaction when it gets rolled back.
 */
public interface MetadataRollbackCommand {

    void execute(Exception e) throws Exception;
}
