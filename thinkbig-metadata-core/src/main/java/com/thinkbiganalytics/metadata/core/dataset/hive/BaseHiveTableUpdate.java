/**
 * 
 */
package com.thinkbiganalytics.metadata.core.dataset.hive;

import java.util.ArrayList;
import java.util.List;

import com.thinkbiganalytics.metadata.api.datasource.hive.HivePartitionUpdate;
import com.thinkbiganalytics.metadata.api.datasource.hive.HiveTableUpdate;
import com.thinkbiganalytics.metadata.core.op.BaseChangedContent;

/**
 *
 * @author Sean Felten
 */
public class BaseHiveTableUpdate extends BaseChangedContent implements HiveTableUpdate {

    private int recourdCount;
    private List<HivePartitionUpdate> partitions = new ArrayList<>();
    
    public BaseHiveTableUpdate(int count) {
        this.recourdCount = count;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.datasource.hive.HiveTableUpdate#getRecordCount()
     */
    public int getRecordCount() {
        return this.recourdCount;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.datasource.hive.HiveTableUpdate#getPartitions()
     */
    public List<HivePartitionUpdate> getPartitions() {
        return this.partitions;
    }

}
