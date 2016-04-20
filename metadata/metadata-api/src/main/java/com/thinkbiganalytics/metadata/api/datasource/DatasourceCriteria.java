/**
 * 
 */
package com.thinkbiganalytics.metadata.api.datasource;

import org.joda.time.DateTime;

import com.thinkbiganalytics.metadata.api.MetadataCriteria;

/**
 *
 * @author Sean Felten
 */
public interface DatasourceCriteria extends MetadataCriteria<DatasourceCriteria> {

    DatasourceCriteria name(String name);
    DatasourceCriteria createdOn(DateTime time);
    DatasourceCriteria createdAfter(DateTime time);
    DatasourceCriteria createdBefore(DateTime time);
    DatasourceCriteria type(Class<? extends Datasource> type);
}
