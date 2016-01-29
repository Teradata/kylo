/**
 * 
 */
package com.thinkbiganalytics.metadata.core.dataset.files;

import java.nio.file.Path;

import com.thinkbiganalytics.metadata.api.dataset.filesys.DirectoryDataset;
import com.thinkbiganalytics.metadata.core.dataset.CoreDataset;

/**
 *
 * @author Sean Felten
 */
public class CoreDirectoryDataset extends CoreDataset implements DirectoryDataset {
    
    private Path directory;

    public Path getDirectory() {
        return this.directory;
    }

}
