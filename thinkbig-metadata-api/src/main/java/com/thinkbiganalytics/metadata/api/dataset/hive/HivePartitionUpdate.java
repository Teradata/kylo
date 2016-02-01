/**
 * 
 */
package com.thinkbiganalytics.metadata.api.dataset.hive;

import java.util.List;

/**
 *
 * @author Sean Felten
 */
public interface HivePartitionUpdate {

    String getColumnName();
    
    List<Object> getValuses();
}
