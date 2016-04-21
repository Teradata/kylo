/**
 * 
 */
package com.thinkbiganalytics.metadata.rest.model.data;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Sean Felten
 */
public class HiveTableDatasource extends Datasource {

    private String database;
    private String tableName;
    private String modifiers;
    private List<HiveTableField> fields = new ArrayList<>();
    private List<HiveTablePartition> partitions = new ArrayList<>();

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getModifiers() {
        return modifiers;
    }

    public void setModifiers(String modifiers) {
        this.modifiers = modifiers;
    }

    public List<HiveTableField> getFields() {
        return fields;
    }

    public void setFields(List<HiveTableField> fields) {
        this.fields = fields;
    }

    public List<HiveTablePartition> getPartitions() {
        return partitions;
    }

    public void setPartitions(List<HiveTablePartition> partitions) {
        this.partitions = partitions;
    }
}
