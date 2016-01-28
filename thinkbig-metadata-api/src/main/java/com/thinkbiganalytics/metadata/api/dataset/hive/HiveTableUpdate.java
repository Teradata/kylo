/**
 * 
 */
package com.thinkbiganalytics.metadata.api.dataset.hive;

import com.thinkbiganalytics.metadata.api.dataset.ChangedContent;

/**
 *
 * @author Sean Felten
 */
public interface HiveTableUpdate extends ChangedContent {

    int getRecordCount();
}
