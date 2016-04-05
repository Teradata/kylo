/**
 * 
 */
package com.thinkbiganalytics.metadata.jpa.datasource.hive;

import java.util.ArrayList;
import java.util.List;

import com.thinkbiganalytics.metadata.api.datasource.hive.HivePartitionUpdate;
import com.thinkbiganalytics.metadata.api.datasource.hive.HiveTableUpdate;
import com.thinkbiganalytics.metadata.jpa.op.JpaChangedContent;

/**
 *
 * @author Sean Felten
 */
public class JpaHiveTableUpdate extends JpaChangedContent implements HiveTableUpdate {

    private int recourdCount;
    private List<HivePartitionUpdate> partitions = new ArrayList<>();
    
    public JpaHiveTableUpdate(int count) {
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
