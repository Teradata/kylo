/**
 * 
 */
package com.thinkbiganalytics.metadata.core.dataset.files;

import java.nio.file.Path;

import com.thinkbiganalytics.metadata.api.datasource.filesys.DirectoryDataset;
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

    public BaseDirectoryDataset(BaseDataset ds, Path dir) {
        this(ds.getName(), ds.getDescription(), dir);
    }

    public Path getDirectory() {
        return this.directory;
    }

}
