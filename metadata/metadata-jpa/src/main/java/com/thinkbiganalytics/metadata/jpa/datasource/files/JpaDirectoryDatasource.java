/**
 * 
 */
package com.thinkbiganalytics.metadata.jpa.datasource.files;

import com.thinkbiganalytics.jpa.PathAttributeConverter;
import com.thinkbiganalytics.metadata.api.datasource.filesys.DirectoryDatasource;
import com.thinkbiganalytics.metadata.jpa.datasource.JpaDatasource;

import java.nio.file.Path;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 *
 * @author Sean Felten
 */
@Entity
@DiscriminatorValue("directory")
public class JpaDirectoryDatasource extends JpaDatasource implements DirectoryDatasource {

    private static final long serialVersionUID = 6142399996937408985L;
    
    @Convert(converter = PathAttributeConverter.class)
    @Column(name="path", length=255)
    private Path directory;
    
    public JpaDirectoryDatasource() {
    }

    public void setDirectory(Path directory) {
        this.directory = directory;
    }

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
