/**
 * 
 */
package com.thinkbiganalytics.metadata.event;

import com.thinkbiganalytics.metadata.api.datasource.Datasource;
import com.thinkbiganalytics.metadata.api.event.DataChangeEvent;
import com.thinkbiganalytics.metadata.api.op.Dataset;
import com.thinkbiganalytics.metadata.api.op.ChangeSet;

/**
 *
 * @author Sean Felten
 */
public class BaseDataChangeEvent<D extends Datasource, C extends ChangeSet> implements DataChangeEvent<D, C> {
    
    private Dataset<D, C> changeSet;
    
    public BaseDataChangeEvent(Dataset<D, C> changeSet) {
        this.changeSet = changeSet;
    }

    public Dataset<D, C> getChangeSet() {
        return this.changeSet;
    }

}
