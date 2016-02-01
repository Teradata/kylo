/**
 * 
 */
package com.thinkbiganalytics.metadata.api.dataset.hive;

import java.util.List;

import com.thinkbiganalytics.metadata.api.op.ChangedContent;

/**
 *
 * @author Sean Felten
 */
public interface HiveTableUpdate extends ChangedContent {

    int getRecordCount();
    
    List<HivePartitionUpdate> getPartitions();
}
