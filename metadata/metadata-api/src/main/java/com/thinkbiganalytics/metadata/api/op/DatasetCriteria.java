/**
 * 
 */
package com.thinkbiganalytics.metadata.api.op;

import org.joda.time.DateTime;

import com.thinkbiganalytics.metadata.api.MetadataCriteria;

/**
 *
 * @author Sean Felten
 */
public interface DatasetCriteria extends MetadataCriteria<DatasetCriteria> {

    DatasetCriteria type(Dataset.ChangeType... types);
    DatasetCriteria changedOn(DateTime time);
    DatasetCriteria changedAfter(DateTime time);
    DatasetCriteria changedBefore(DateTime time);
}
