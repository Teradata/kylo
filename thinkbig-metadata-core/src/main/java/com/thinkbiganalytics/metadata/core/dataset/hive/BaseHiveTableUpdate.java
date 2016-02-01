/**
 * 
 */
package com.thinkbiganalytics.metadata.core.dataset.hive;

import java.util.List;

import com.thinkbiganalytics.metadata.api.dataset.hive.HivePartitionUpdate;
import com.thinkbiganalytics.metadata.api.dataset.hive.HiveTableUpdate;

/**
 *
 * @author Sean Felten
 */
public class BaseHiveTableUpdate implements HiveTableUpdate {

    private int recourdCount;
    private List<HivePartitionUpdate> partitions;
    
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.dataset.hive.HiveTableUpdate#getRecordCount()
     */
    public int getRecordCount() {
        return this.recourdCount;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.dataset.hive.HiveTableUpdate#getPartitions()
     */
    public List<HivePartitionUpdate> getPartitions() {
        return this.partitions;
    }

}
