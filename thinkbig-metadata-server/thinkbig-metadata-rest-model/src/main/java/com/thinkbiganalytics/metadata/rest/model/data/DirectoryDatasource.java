/**
 * 
 */
package com.thinkbiganalytics.metadata.rest.model.data;

import java.util.Set;

/**
 *
 * @author Sean Felten
 */
public class DirectoryDatasource {
    
    private String path;
    private Set<FilePattern> patterns;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Set<FilePattern> getPatterns() {
        return patterns;
    }

    public void setPatterns(Set<FilePattern> patterns) {
        this.patterns = patterns;
    }
}
