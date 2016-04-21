/**
 * 
 */
package com.thinkbiganalytics.metadata.api.datasource.hive;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author Sean Felten
 */
public interface HivePartitionUpdate extends Serializable {

    String getColumnName();
    
    List<Serializable> getValues();
}
