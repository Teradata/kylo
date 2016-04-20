/**
 * 
 */
package com.thinkbiganalytics.metadata.rest.model.data;

/**
 *
 * @author Sean Felten
 */
public class FilePattern {

    private String regex;
    private String glob;

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
        this.glob = null;
    }

    public String getGlob() {
        return glob;
    }

    public void setGlob(String glob) {
        this.glob = glob;
        this.regex = null;
    }
}
