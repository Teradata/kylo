package com.thinkbiganalytics.spark.datavalidator;

import java.io.Serializable;

/**
 * Represents configuration parameter from command line.
 */
public class Param implements Serializable {

    private String name;
    private String value;

    public Param(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }
}
