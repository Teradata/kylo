/**
 * 
 */
package com.thinkbiganalytics.metadata.jpa.datasource.files;

import java.nio.file.Path;

import javax.persistence.DiscriminatorValue;

import com.thinkbiganalytics.metadata.api.datasource.filesys.DirectoryDatasource;
import com.thinkbiganalytics.metadata.jpa.datasource.JpaDatasource;

/**
 *
 * @author Sean Felten
 */
@DiscriminatorValue("directory")
public class JpaDirectoryDatasource extends JpaDatasource implements DirectoryDatasource {

    private static final long serialVersionUID = 6142399996937408985L;
    
    private Path directory;

    public JpaDirectoryDatasource(String name, String descr, Path dir) {
        super(name, descr);
        
        this.directory = dir;
    }

    public JpaDirectoryDatasource(JpaDatasource ds, Path dir) {
        this(ds.getName(), ds.getDescription(), dir);
    }

    public Path getDirectory() {
        return this.directory;
    }

}
