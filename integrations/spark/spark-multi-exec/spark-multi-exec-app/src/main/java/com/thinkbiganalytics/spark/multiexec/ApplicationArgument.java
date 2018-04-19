/**
 * 
 */
package com.thinkbiganalytics.spark.multiexec;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 */
public class ApplicationArgument {
    
    private String name;
    private String value;
    
    public ApplicationArgument() {
    }
    
    public ApplicationArgument(String value) {
        this(null, value);
    }

    public ApplicationArgument(String name, String value) {
        super();
        this.name = name;
        this.value = value;
    }

    @JsonProperty("a")  // Reduces command line size
    public String getName() {
        return this.name;
    }

    @JsonProperty("v")  // Reduces command line size
    public String getValue() {
        return this.value;
    }
}
