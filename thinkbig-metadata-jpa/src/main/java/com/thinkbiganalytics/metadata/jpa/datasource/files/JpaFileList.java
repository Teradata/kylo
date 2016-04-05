/**
 * 
 */
package com.thinkbiganalytics.metadata.jpa.datasource.files;

import java.nio.file.Path;
import java.util.List;

import com.thinkbiganalytics.metadata.api.datasource.filesys.FileList;
import com.thinkbiganalytics.metadata.jpa.op.JpaChangedContent;

/**
 *
 * @author Sean Felten
 */
public class JpaFileList extends JpaChangedContent implements FileList {

    private List<Path> filePaths;
    
    public JpaFileList(List<Path> paths) {
        this.filePaths = paths;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.datasource.filesys.FileList#getFilePaths()
     */
    public List<Path> getFilePaths() {
        return this.filePaths;
    }

}
