/**
 * 
 */
package com.thinkbiganalytics.metadata.core.dataset.files;

import java.nio.file.Path;
import java.util.List;

import com.thinkbiganalytics.metadata.api.datasource.filesys.FileList;
import com.thinkbiganalytics.metadata.core.op.BaseChangedContent;

/**
 *
 * @author Sean Felten
 */
public class BaseFileList extends BaseChangedContent implements FileList {

    private List<Path> filePaths;
    
    public BaseFileList(List<Path> paths) {
        this.filePaths = paths;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.datasource.filesys.FileList#getFilePaths()
     */
    public List<Path> getFilePaths() {
        return this.filePaths;
    }

}
