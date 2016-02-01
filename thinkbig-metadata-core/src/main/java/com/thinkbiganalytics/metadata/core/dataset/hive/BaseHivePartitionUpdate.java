/**
 * 
 */
package com.thinkbiganalytics.metadata.core.dataset.hive;

import java.util.List;

import com.thinkbiganalytics.metadata.api.dataset.hive.HivePartitionUpdate;

/**
 *
 * @author Sean Felten
 */
public class BaseHivePartitionUpdate implements HivePartitionUpdate {

    private String columnName;
    private List<Object> values;
    
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.dataset.hive.HivePartitionUpdate#getColumnName()
     */
    public String getColumnName() {
        return this.columnName;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.dataset.hive.HivePartitionUpdate#getValuses()
     */
    public List<Object> getValuses() {
        return this.values;
    }

}
