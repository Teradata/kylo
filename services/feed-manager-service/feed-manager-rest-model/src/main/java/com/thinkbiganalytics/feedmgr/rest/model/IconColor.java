package com.thinkbiganalytics.feedmgr.rest.model;

/**
 * Created by sr186054 on 7/14/16.
 */
public class IconColor {

    private String name;
    private String color;

    public IconColor() {
    }

    public IconColor(String name, String color) {
        this.name = name;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
