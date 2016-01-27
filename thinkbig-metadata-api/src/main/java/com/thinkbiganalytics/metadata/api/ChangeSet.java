/**
 * 
 */
package com.thinkbiganalytics.metadata.api;

import java.util.Set;

import org.joda.time.DateTime;

/**
 *
 * @author Sean Felten
 */
public interface ChangeSet<C extends ChangeContent> {
    
    enum Type { UPDATE, DELETE }

    DateTime getTime();
    
    Type getType();
    
    Dataset getDataset();
    
    DataOperation getDataOperation();
    
    Set<C> getChanges();
}
