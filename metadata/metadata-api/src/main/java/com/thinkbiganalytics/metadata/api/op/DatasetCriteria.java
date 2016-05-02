/**
 * 
 */
package com.thinkbiganalytics.metadata.api.op;

import org.joda.time.DateTime;

import com.thinkbiganalytics.metadata.api.MetadataCriteria;
import com.thinkbiganalytics.metadata.api.datasource.Datasource;

/**
 *
 * @author Sean Felten
 */
public interface DatasetCriteria extends MetadataCriteria<DatasetCriteria> {

    DatasetCriteria datasource(Datasource.ID... dsIds);
    DatasetCriteria type(Dataset.ChangeType... types);
    DatasetCriteria changedOn(DateTime time);
    DatasetCriteria changedAfter(DateTime time);
    DatasetCriteria changedBefore(DateTime time);
}
