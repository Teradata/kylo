/**
 * 
 */
package com.thinkbiganalytics.metadata.api.op;

import java.io.Serializable;
import java.util.Set;

import org.joda.time.DateTime;

import com.thinkbiganalytics.metadata.api.datasource.Datasource;

/**
 *
 * @author Sean Felten
 */
public interface Dataset<D extends Datasource, C extends ChangeSet> extends Serializable {
    
    enum ChangeType { UPDATE, DELETE }

    DateTime getTime();
    
    ChangeType getType();
    
    DataOperation getDataOperation();
    
    D getDataset();
    
    Set<C> getChanges();
}
