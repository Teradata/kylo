/**
 * 
 */
package com.thinkbiganalytics.metadata.rest.model.op;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 *
 * @author Sean Felten
 */
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class FileList extends ChangeSet {

    private List<String> paths = new ArrayList<>();
    
    public FileList() {
    }
    
    public FileList(ArrayList<Path> paths) {
        for (Path path : paths) {
            this.paths.add(path.toString());
        }
    }

    public List<String> getPaths() {
        return paths;
    }

    public void setPaths(List<String> paths) {
        this.paths = paths;
    }

    public void addPath(String path) {
        this.paths.add(path);
    }
    
    
}
