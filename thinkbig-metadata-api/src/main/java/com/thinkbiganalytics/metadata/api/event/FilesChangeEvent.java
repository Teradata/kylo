/**
 * 
 */
package com.thinkbiganalytics.metadata.api.event;

import com.thinkbiganalytics.metadata.api.dataset.filesys.DirectoryDataset;
import com.thinkbiganalytics.metadata.api.dataset.filesys.FileList;

/**
 *
 * @author Sean Felten
 */
public interface FilesChangeEvent extends ChangeEvent<DirectoryDataset, FileList> {

}
