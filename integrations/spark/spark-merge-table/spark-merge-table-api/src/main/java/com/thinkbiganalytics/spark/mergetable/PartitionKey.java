/**
 * 
 */
package com.thinkbiganalytics.spark.mergetable;

/**
 *
 */
public interface PartitionKey {

    String getKey();
    
    String getKeyWithAlias();
    
    String getKeyForSql();
    
    String getType();
    
    String getFormula();
    
    String getFormulaWithAlias();
    
    String getAlias();

    String toTargetSQLWhere(String value);
    
    String toSourceSQLWhere(String value);
    
    String toPartitionNameValue(String value);
    
}
