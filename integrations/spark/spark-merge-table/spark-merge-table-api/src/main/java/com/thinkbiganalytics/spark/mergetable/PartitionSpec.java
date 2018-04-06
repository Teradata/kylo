/**
 * 
 */
package com.thinkbiganalytics.spark.mergetable;

import java.util.List;

/**
 *
 */
public interface PartitionSpec {

    boolean isNonPartitioned();
    
    List<PartitionKey> getKeys();
    
    List<String> getKeyNames();
    
    PartitionSpec withAlias(String alias);
    
    String toTargetSQLWhere(List<String> values);
    
    String toSourceSQLWhere(List<String> values);
    
    String toPartitionSpec(List<String> values);
    
    String toDynamicPartitionSpec();
    
    String toPartitionSelectSQL();
    
    String toDynamicSelectSQLSpec();
    
    String toDistinctSelectSQL(String sourceSchema, String sourceTable, String feedPartitionValue);
}
