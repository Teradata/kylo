/**
 * 
 */
package com.thinkbiganalytics.metadata.jpa.datasource.hive;

import java.io.Serializable;

import javax.persistence.Embeddable;

/**
 *
 * @author Sean Felten
 */
@Embeddable
public class HivePartitionValue implements Serializable {

    private String name;
    private String value;
    
    public HivePartitionValue() {
    }

    public HivePartitionValue(String name, String value) {
        super();
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
    
    
}
