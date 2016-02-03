/**
 * 
 */
package com.thinkbiganalytics.metadata.core.dataset.files;

import java.nio.file.Path;

import com.thinkbiganalytics.metadata.api.dataset.filesys.DirectoryDataset;
import com.thinkbiganalytics.metadata.core.dataset.BaseDataset;

/**
 *
 * @author Sean Felten
 */
public class BaseDirectoryDataset extends BaseDataset implements DirectoryDataset {
    
    private Path directory;

    public BaseDirectoryDataset(String name, String descr, Path dir) {
        super(name, descr);
        
        this.directory = dir;
    }

    public Path getDirectory() {
        return this.directory;
    }

}
