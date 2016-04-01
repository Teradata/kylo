/**
 * 
 */
package com.thinkbiganalytics.metadata.core.dataset.files;

import java.nio.file.Path;

import com.thinkbiganalytics.metadata.api.datasource.filesys.DirectoryDatasource;
import com.thinkbiganalytics.metadata.core.dataset.BaseDatasource;

/**
 *
 * @author Sean Felten
 */
public class BaseDirectoryDatasource extends BaseDatasource implements DirectoryDatasource {
    
    private Path directory;

    public BaseDirectoryDatasource(String name, String descr, Path dir) {
        super(name, descr);
        
        this.directory = dir;
    }

    public BaseDirectoryDatasource(BaseDatasource ds, Path dir) {
        this(ds.getName(), ds.getDescription(), dir);
    }

    public Path getDirectory() {
        return this.directory;
    }

}
