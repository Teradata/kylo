/**
 * 
 */
package com.thinkbiganalytics.metadata.core.dataset.hive;

import com.thinkbiganalytics.metadata.api.datasource.hive.HiveTableDatasource;
import com.thinkbiganalytics.metadata.core.dataset.BaseDatasource;

/**
 *
 * @author Sean Felten
 */
public class BaseHiveTableDatasource extends BaseDatasource implements HiveTableDatasource {
    
    private String database;
    private String tableName;

    public BaseHiveTableDatasource(String name, String descr, String db, String table) {
        super(name, descr);
        
        this.database = db;
        this.tableName = table;
    }

    public BaseHiveTableDatasource(BaseDatasource ds, String db, String table) {
        this(ds.getName(), ds.getDescription(), db, table);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.datasource.hive.HiveTableDatasource#getDatabase()
     */
    public String getDatabaseName() {
        return database;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.datasource.hive.HiveTableDatasource#getTableName()
     */
    public String getTableName() {
        return tableName;
    }

}
