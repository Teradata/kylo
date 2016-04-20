/**
 * 
 */
package com.thinkbiganalytics.metadata.api.datasource.hive;

import java.util.List;

import com.thinkbiganalytics.metadata.api.op.ChangeSet;

/**
 *
 * @author Sean Felten
 */
public interface HiveTableUpdate extends ChangeSet {

    int getRecordCount();
    
    List<HivePartitionUpdate> getPartitions();
}
