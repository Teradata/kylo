/**
 * 
 */
package com.thinkbiganalytics.metadata.rest.model.data;

/**
 *
 * @author Sean Felten
 */
public class HiveTableColumn {

    private String name;
    private String type;
    
    public HiveTableColumn() {
    }

    public HiveTableColumn(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
