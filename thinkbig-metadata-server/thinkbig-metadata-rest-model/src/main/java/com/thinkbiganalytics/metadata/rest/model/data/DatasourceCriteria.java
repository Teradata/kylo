/**
 * 
 */
package com.thinkbiganalytics.metadata.rest.model.data;

import org.joda.time.DateTime;

/**
 *
 * @author Sean Felten
 */
public interface DatasourceCriteria {

    static final String NAME = "name";
    static final String OWNER = "owner";
    static final String ON = "on";
    static final String AFTER = "after";
    static final String BEFORE = "before";
    static final String TYPE = "type";

    DatasourceCriteria name(String name);
    DatasourceCriteria createdOn(DateTime time);
    DatasourceCriteria createdAfter(DateTime time);
    DatasourceCriteria createdBefore(DateTime time);
    DatasourceCriteria owner(String owner);
    DatasourceCriteria type(Class<? extends Datasource> type, Class<? extends Datasource>... others);
}
