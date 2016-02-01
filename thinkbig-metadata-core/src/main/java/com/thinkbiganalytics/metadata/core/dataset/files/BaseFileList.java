/**
 * 
 */
package com.thinkbiganalytics.metadata.core.dataset.files;

import java.nio.file.Path;
import java.util.List;

import com.thinkbiganalytics.metadata.api.dataset.filesys.FileList;

/**
 *
 * @author Sean Felten
 */
public class BaseFileList implements FileList {

    private List<Path> filePaths;
    
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.dataset.filesys.FileList#getFilePaths()
     */
    public List<Path> getFilePaths() {
        return this.filePaths;
    }

}
