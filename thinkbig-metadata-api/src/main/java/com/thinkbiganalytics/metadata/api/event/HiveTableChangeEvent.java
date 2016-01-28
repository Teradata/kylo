/**
 * 
 */
package com.thinkbiganalytics.metadata.api.event;

import com.thinkbiganalytics.metadata.api.dataset.hive.HiveTableDataset;
import com.thinkbiganalytics.metadata.api.dataset.hive.HiveTableUpdate;

/**
 *
 * @author Sean Felten
 */
public interface HiveTableChangeEvent extends ChangeEvent<HiveTableDataset, HiveTableUpdate> {

}
