/**
 * 
 */
package com.thinkbiganalytics.spark.mergetable;

/**
 *
 */
public interface ColumnSpec {
    
    String getName();

    String getComment();

    String getDataType();

    boolean isPrimaryKey();

    boolean isCreateDate();

    boolean isModifiedDate();

    String getOtherColumnName();

}
