/**
 * 
 */
package com.thinkbiganalytics.metadata.api.dataset.hive;

import com.thinkbiganalytics.metadata.api.dataset.Dataset;

/**
 *
 * @author Sean Felten
 */
public interface HiveTableDataset extends Dataset {

    String getDatabase();
    
    String getTableName();
}
