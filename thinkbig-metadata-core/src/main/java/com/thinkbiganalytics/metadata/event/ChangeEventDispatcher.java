/**
 * 
 */
package com.thinkbiganalytics.metadata.event;

import com.thinkbiganalytics.metadata.api.datasource.Datasource;
import com.thinkbiganalytics.metadata.api.event.DataChangeEventListener;
import com.thinkbiganalytics.metadata.api.op.Dataset;
import com.thinkbiganalytics.metadata.api.op.ChangeSet;

/**
 *
 * @author Sean Felten
 */
public interface ChangeEventDispatcher {

    <D extends Datasource, C extends ChangeSet> void addListener(D dataset, DataChangeEventListener<D, C> listener);
    
    <D extends Datasource, C extends ChangeSet> void addListener(DataChangeEventListener<D, C> listener);
    
    <D extends Datasource, C extends ChangeSet> void nofifyChange(Dataset<D, C> change);
}
