/**
 * 
 */
package com.thinkbiganalytics.metadata.jpa.datasource.hive;

import com.thinkbiganalytics.metadata.api.datasource.hive.HiveTableDatasource;
import com.thinkbiganalytics.metadata.jpa.datasource.JpaDatasource;

/**
 *
 * @author Sean Felten
 */
public class JpaHiveTableDatasource extends JpaDatasource implements HiveTableDatasource {
    
    private String database;
    private String tableName;

    public JpaHiveTableDatasource(String name, String descr, String db, String table) {
        super(name, descr);
        
        this.database = db;
        this.tableName = table;
    }

    public JpaHiveTableDatasource(JpaDatasource ds, String db, String table) {
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
