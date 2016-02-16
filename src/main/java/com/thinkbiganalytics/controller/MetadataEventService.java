/**
 * 
 */
package com.thinkbiganalytics.controller;

import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.controller.ControllerService;

import com.thinkbiganalytics.metadata.api.dataset.filesys.DirectoryDataset;
import com.thinkbiganalytics.metadata.api.dataset.filesys.FileList;
import com.thinkbiganalytics.metadata.api.dataset.hive.HiveTableDataset;
import com.thinkbiganalytics.metadata.api.dataset.hive.HiveTableUpdate;
import com.thinkbiganalytics.metadata.api.event.DataChangeEventListener;

/**
 *
 * @author Sean Felten
 */
@Tags({"thinkbig", "metadata", "operation", "change", "events"})
@CapabilityDescription("A service used to register interest in metadata change events")
public interface MetadataEventService extends ControllerService {
    
    void addListener(DirectoryDataset ds, DataChangeEventListener<DirectoryDataset, FileList> listener);
    
    void addListener(HiveTableDataset ds, DataChangeEventListener<HiveTableDataset, HiveTableUpdate> listener);
}
