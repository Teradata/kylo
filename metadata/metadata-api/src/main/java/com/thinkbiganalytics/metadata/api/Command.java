/**
 * 
 */
package com.thinkbiganalytics.metadata.api;

/**
 * A command to execute (in lieu of a java 8 closure) in the context of a transaction.
 * @author Sean Felten
 */
public interface Command<R> {
    
    R execute();
}
