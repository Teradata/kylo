/**
 * 
 */
package com.thinkbiganalytics.spark.mergetable;

import org.apache.spark.sql.hive.HiveContext;

/**
 *
 */
public interface TableMerger {
    
    void merge(HiveContext context, TableMergeConfig mergeConfig, String feedPartitionValue, boolean shouldDedupe);
    
    void mergeOnPrimaryKey(HiveContext context, TableMergeConfig mergeConfig, String feedPartitionValue);
    
    void synchronize(HiveContext context, TableMergeConfig mergeConfig, String feedPartitionValue, boolean partitionsOnly);
}
