/**
 * 
 */
package com.thinkbiganalytics.metadata.rest.client;

import org.joda.time.DateTime;

import com.thinkbiganalytics.metadata.rest.model.data.Datasource;

/**
 *
 * @author Sean Felten
 */
public interface DatasourceCriteria {

    DatasourceCriteria name(String name);
    DatasourceCriteria createdOn(DateTime time);
    DatasourceCriteria createdAfter(DateTime time);
    DatasourceCriteria createdBefore(DateTime time);
    DatasourceCriteria owner(String owner);
    DatasourceCriteria type(Class<? extends Datasource> type, Class<? extends Datasource>... others);
}
