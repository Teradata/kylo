/**
 * 
 */
package com.thinkbiganalytics.metadata.api.event;

import com.thinkbiganalytics.metadata.api.datasource.Datasource;
import com.thinkbiganalytics.metadata.api.op.ChangeSet;

/**
 *
 * @author Sean Felten
 */
public interface DataChangeEventListener<D extends Datasource, C extends ChangeSet> {

    void handleEvent(DataChangeEvent<D, C> event);
}
