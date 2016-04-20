/**
 * 
 */
package com.thinkbiganalytics.metadata.jpa.datasource.hive;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.thinkbiganalytics.metadata.api.datasource.hive.HivePartitionUpdate;

/**
 *
 * @author Sean Felten
 */
public class HivePartitionUpdateImpl implements HivePartitionUpdate {

    private static final long serialVersionUID = 8505876587865483987L;

    private String columnName;
    private List<String> partitionValues = new ArrayList<>();
    
    
    public HivePartitionUpdateImpl(String columnName) {
        super();
        this.columnName = columnName;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.datasource.hive.HivePartitionUpdate#getColumnName()
     */
    public String getColumnName() {
        return this.columnName;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.datasource.hive.HivePartitionUpdate#getValuses()
     */
    public List<Serializable> getValues() {
        List<Serializable> list = new ArrayList<>();
        for (String value : getPartitionValues()) {
            list.add(value);
        }
        return list;
    }

    public List<String> getPartitionValues() {
        return partitionValues;
    }

    public void setPartitionValues(List<String> partitionValues) {
        this.partitionValues = partitionValues;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

}
