package com.thinkbiganalytics.metadata.rest.model.feed;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by sr186054 on 11/16/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FeedLineageStyle {

    private String icon;
    private String color;
    private Integer size;

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }
}
