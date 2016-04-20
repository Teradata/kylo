/**
 * 
 */
package com.thinkbiganalytics.metadata.api.event;

import com.thinkbiganalytics.metadata.api.datasource.Datasource;
import com.thinkbiganalytics.metadata.api.op.Dataset;
import com.thinkbiganalytics.metadata.api.op.ChangeSet;

/**
 *
 * @author Sean Felten
 */
public interface DataChangeEvent<D extends Datasource, C extends ChangeSet> {

    Dataset<D, C> getDataset();
}
