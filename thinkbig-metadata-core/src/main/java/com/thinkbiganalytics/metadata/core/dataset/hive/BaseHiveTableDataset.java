/**
 * 
 */
package com.thinkbiganalytics.metadata.core.dataset.hive;

import com.thinkbiganalytics.metadata.api.dataset.hive.HiveTableDataset;
import com.thinkbiganalytics.metadata.core.dataset.BaseDataset;

/**
 *
 * @author Sean Felten
 */
public class BaseHiveTableDataset extends BaseDataset implements HiveTableDataset {
    
    private String database;
    private String tableName;

    public BaseHiveTableDataset(String name, String descr, String db, String table) {
        super(name, descr);
        
        this.database = db;
        this.tableName = table;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.dataset.hive.HiveTableDataset#getDatabase()
     */
    public String getDatabase() {
        return database;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.dataset.hive.HiveTableDataset#getTableName()
     */
    public String getTableName() {
        return tableName;
    }

}
