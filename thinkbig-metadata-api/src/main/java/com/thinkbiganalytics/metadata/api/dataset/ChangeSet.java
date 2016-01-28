/**
 * 
 */
package com.thinkbiganalytics.metadata.api.dataset;

import java.util.Set;

import org.joda.time.DateTime;

/**
 *
 * @author Sean Felten
 */
public interface ChangeSet<D extends Dataset, C extends ChangedContent> {
    
    enum Type { UPDATE, DELETE }

    DateTime getTime();
    
    Type getType();
    
    DataOperation getDataOperation();
    
    D getDataset();
    
    Set<C> getChanges();
}
