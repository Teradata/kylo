/**
 * 
 */
package com.thinkbiganalytics.metadata.jpa.datasource.hive;

import com.thinkbiganalytics.metadata.api.datasource.hive.HiveTableDatasource;
import com.thinkbiganalytics.metadata.api.datasource.hive.TableColumn;
import com.thinkbiganalytics.metadata.jpa.datasource.JpaDatasource;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 *
 * @author Sean Felten
 */
@Entity
@DiscriminatorValue("hivetable")
public class JpaHiveTableDatasource extends JpaDatasource implements HiveTableDatasource {
    
    private static final long serialVersionUID = -9033261327846205036L;
    
    @Column(name="database_name")
    private String database;
    
    @Column(name="table_name")
    private String tableName;
    
    public JpaHiveTableDatasource() {
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

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

    @Override
    public String getModifiers() {
        return null;
    }

    @Override
    public List<TableColumn> getColumns() {
        return null;
    }

    @Override
    public void setModifiers(String modifiers) {

    }

    @Override
    public TableColumn addColumn(String name, String type) {
        return null;
    }

    @Override
    public boolean removeColumn(String name) {
        return false;
    }
}
