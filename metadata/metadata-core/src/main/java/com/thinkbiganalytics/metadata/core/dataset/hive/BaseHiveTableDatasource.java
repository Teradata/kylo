/**
 * 
 */
package com.thinkbiganalytics.metadata.core.dataset.hive;

import java.util.List;

import com.thinkbiganalytics.metadata.api.datasource.hive.HiveTableDatasource;
import com.thinkbiganalytics.metadata.api.datasource.hive.TableColumn;
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

    @Override
    public String getModifiers() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<TableColumn> getColumns() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setModifiers(String modifiers) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public TableColumn addColumn(String name, String type) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean removeColumn(String name) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setDatabase(String name) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setTableName(String name) {
        // TODO Auto-generated method stub
        
    }

}
