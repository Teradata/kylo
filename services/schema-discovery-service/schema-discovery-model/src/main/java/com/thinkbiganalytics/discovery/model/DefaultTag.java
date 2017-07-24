package com.thinkbiganalytics.discovery.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.thinkbiganalytics.discovery.schema.Tag;

/**
 * A basic {@link Tag}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DefaultTag implements Tag {

    private String name;

    public DefaultTag() {
    }

    public DefaultTag(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
