/**
 *
 */
package com.thinkbiganalytics.metadata.api;

/**
 * A command to execute in the context of a transaction when it gets rolledback
 */
public interface MetadataRollbackAction {

    void execute(Exception e) throws Exception;
}
