/**
 * 
 */
package com.thinkbiganalytics.metadata.rest.client;

import com.thinkbiganalytics.metadata.rest.model.data.HiveTableDatasource;

/**
 *
 * @author Sean Felten
 */
public interface HiveTableDatasourceBuilder extends DatasourceBuilder<HiveTableDatasourceBuilder, HiveTableDatasource> {

    HiveTableDatasourceBuilder database(String name);
    HiveTableDatasourceBuilder tableName(String name);
    HiveTableDatasourceBuilder modifiers(String mods);
    HiveTableDatasourceBuilder field(String name, String type);
    HiveTableDatasourceBuilder partition(String name, String formula, String value, String... more);
}
