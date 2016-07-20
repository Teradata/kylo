/**
 * 
 */
package com.thinkbiganalytics.metadata.api.datasource.hive;

import java.util.List;

import com.thinkbiganalytics.metadata.api.datasource.Datasource;

/**
 *
 * @author Sean Felten
 */
public interface HiveTableDatasource extends Datasource {

    String getDatabaseName();
    
    String getTableName();
    
    String getModifiers();
    
    List<TableColumn> getColumns();
    
    void setModifiers(String modifiers);
    
    void setDatabase(String name);
    
    void setTableName(String name);
    
    TableColumn addColumn(String name, String type);
    
    boolean removeColumn(String name);
}
