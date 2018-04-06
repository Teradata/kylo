/**
 * 
 */
package com.thinkbiganalytics.spark.mergetable;

import java.util.List;

/**
 *
 */
public interface TableMergeConfig {
    
    enum MergeStrategy {MERGE, DEDUPE_MERGE, PK_MERGE, SYNC, ROLLING_SYNC}
    
    String getTargetTable();
    
    String getTargetSchema();
    
    String getSourceTable();
    
    String getSourceSchema();
    
    MergeStrategy getStrategy();
    
    List<ColumnSpec> getColumnSpecs();
    
    PartitionSpec getPartionSpec();
}
