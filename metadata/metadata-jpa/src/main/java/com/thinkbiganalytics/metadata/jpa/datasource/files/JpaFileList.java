/**
 * 
 */
package com.thinkbiganalytics.metadata.jpa.datasource.files;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.thinkbiganalytics.metadata.api.datasource.filesys.FileList;
import com.thinkbiganalytics.metadata.jpa.op.JpaChangeSet;

/**
 *
 * @author Sean Felten
 */
@Entity
@Table(name="CHANGE_SET_FILES")
public class JpaFileList extends JpaChangeSet implements FileList {

    private static final long serialVersionUID = -1129519814650674547L;
    
    @ElementCollection
    @CollectionTable(name="CHANGE_SET_FILES_PATH", joinColumns=@JoinColumn(name="change_set_files_id"))
    @Column(name="path")
    private List<String> paths = new ArrayList<>();
    
    public JpaFileList() {
    }
    
    public JpaFileList(List<Path> paths) {
        for (Path path : paths) {
            this.paths.add(path.toString());
        }
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.datasource.filesys.FileList#getFilePaths()
     */
    public List<Path> getFilePaths() {
        return Lists.transform(getPaths(), new Function<String, Path>() {
            @Override
            public Path apply(String filepath) {
                return Paths.get(filepath);
            }
        }); 
    }

    public List<String> getPaths() {
        return paths;
    }

    public void setFilenames(List<String> paths) {
        this.paths = paths;
    }
    
}
