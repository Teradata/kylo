/**
 * 
 */
package com.thinkbiganalytics.metadata.api.dataset;

import org.joda.time.DateTime;

import com.thinkbiganalytics.metadata.api.MetadataCriteria;

/**
 *
 * @author Sean Felten
 */
public interface DatasetCriteria extends MetadataCriteria<DatasetCriteria> {

    DatasetCriteria name(String name);
    DatasetCriteria createdOn(DateTime time);
    DatasetCriteria createdAfter(DateTime time);
    DatasetCriteria createdBefore(DateTime time);
    DatasetCriteria type(Class<? extends Dataset> type);
}
