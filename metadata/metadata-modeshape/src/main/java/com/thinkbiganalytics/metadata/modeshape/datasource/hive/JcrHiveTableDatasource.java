/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.datasource.hive;

import java.util.List;
import java.util.stream.Collectors;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import com.thinkbiganalytics.metadata.api.MetadataException;
import com.thinkbiganalytics.metadata.api.datasource.hive.HiveTableDatasource;
import com.thinkbiganalytics.metadata.api.datasource.hive.TableColumn;
import com.thinkbiganalytics.metadata.modeshape.datasource.JcrDatasource;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;

/**
 *
 * @author Sean Felten
 */
public class JcrHiveTableDatasource extends JcrDatasource implements HiveTableDatasource {
    
    private static final String TABLE_COLUMN = "tba:hiveTableColumn";

    private static final String PATH_NAME = "hive";
    
    public static final String NODE_TYPE = "tba:hiveTableDatasource";
    
    private static final String DATABASE_NAME = "tba:database";
    private static final String TABLE_NAME = "tba:tableName";
    private static final String MODIFIERS = "tba:modifiers";
    private static final String COLUMNS = "tba:columns";
    private static final String PARTITIONS = "tba:partitions";

    /**
     * 
     */
    public JcrHiveTableDatasource(Node node) {
        super(node);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.datasource.hive.HiveTableDatasource#getDatabaseName()
     */
    @Override
    public String getDatabaseName() {
        return super.getProperty(DATABASE_NAME, String.class);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.datasource.hive.HiveTableDatasource#getTableName()
     */
    @Override
    public String getTableName() {
        return super.getProperty(TABLE_NAME, String.class);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.datasource.hive.HiveTableDatasource#getModifiers()
     */
    @Override
    public String getModifiers() {
        return super.getProperty(MODIFIERS, String.class);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.datasource.hive.HiveTableDatasource#getColumns()
     */
    @Override
    public List<TableColumn> getColumns() {
        try {
            return JcrUtil.getNodes(this.node.getNode(COLUMNS), null, JcrTableColumn.class).stream().collect(Collectors.toList());
        } catch (RepositoryException e) {
            throw new MetadataException("Failed to retrieve table columns", e);
        }
    }
    
    @Override
    public void setDatabase(String name) {
        super.setProperty(DATABASE_NAME, name);
    }

    @Override
    public void setTableName(String name) {
        super.setProperty(TABLE_NAME, name);
    }

    @Override
    public void setModifiers(String modifiers) {
    }

    @Override
    public TableColumn addColumn(String name, String type) {
        try {
            Node colNode = this.node.getNode(COLUMNS);
            
            if (colNode.hasNode(name)) {
                colNode.getNode(name).remove();
            }
            
            colNode = this.node.addNode(COLUMNS, TABLE_COLUMN);
            return new JcrTableColumn(colNode);
        } catch (RepositoryException e) {
            throw new MetadataException("Failed to add a table column", e);
        }
    }

    @Override
    public boolean removeColumn(String name) {
        try {
            Node colNode = this.node.getNode(COLUMNS);
            
            if (colNode.hasNode(name)) {
                colNode.getNode(name).remove();
                return true;
            } else {
                return false;
            }
        } catch (RepositoryException e) {
            throw new MetadataException("Failed to add a table column", e);
        }
    }

}
