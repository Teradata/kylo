package com.thinkbiganalytics.feedmgr.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by sr186054 on 1/22/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Tag {

    private String name;

    public Tag(){

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
