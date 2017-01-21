/**
 * 
 */
package com.thinkbiganalytics.metadata.rest.model.data;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Sean Felten
 */
public class DirectoryDatasource extends Datasource {
    
    private String path;
    private Set<FilePattern> patterns = new HashSet<>();

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
    
    public void addGlobPattern(String pattern) {
        FilePattern fp = new FilePattern();
        fp.setGlob(pattern);
        this.patterns.add(fp);
    }
    
    public void addRegexPattern(String pattern) {
        FilePattern fp = new FilePattern();
        fp.setRegex(pattern);
        this.patterns.add(fp);
    }
}
