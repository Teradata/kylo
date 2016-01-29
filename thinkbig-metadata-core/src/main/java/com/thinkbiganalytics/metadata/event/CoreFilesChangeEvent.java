/**
 * 
 */
package com.thinkbiganalytics.metadata.event;

import com.thinkbiganalytics.metadata.api.dataset.filesys.DirectoryDataset;
import com.thinkbiganalytics.metadata.api.dataset.filesys.FileList;
import com.thinkbiganalytics.metadata.api.event.FilesChangeEvent;

/**
 *
 * @author Sean Felten
 */
public class CoreFilesChangeEvent extends BaseChangeEvent<DirectoryDataset, FileList> implements FilesChangeEvent {

}
