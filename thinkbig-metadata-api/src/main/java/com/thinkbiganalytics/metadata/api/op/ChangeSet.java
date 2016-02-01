/**
 * 
 */
package com.thinkbiganalytics.metadata.api.op;

import java.util.Set;

import org.joda.time.DateTime;

import com.thinkbiganalytics.metadata.api.dataset.Dataset;

/**
 *
 * @author Sean Felten
 */
public interface ChangeSet<D extends Dataset, C extends ChangedContent> {
    
    enum ChangeType { UPDATE, DELETE }

    DateTime getTime();
    
    ChangeType getType();
    
    DataOperation getDataOperation();
    
    D getDataset();
    
    Set<C> getChanges();
}
