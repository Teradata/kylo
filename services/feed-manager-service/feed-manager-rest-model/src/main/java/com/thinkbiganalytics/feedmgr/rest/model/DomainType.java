package com.thinkbiganalytics.feedmgr.rest.model;

import com.thinkbiganalytics.policy.rest.model.FieldPolicy;

/**
 * Defines the domain type (zip, phone, credit card) of a column.
 */
public class DomainType {

    private String description;
    private FieldPolicy fieldPolicy;
    private String icon;
    private String iconColor;
    private String id;
    private String regex;
    private String title;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public FieldPolicy getFieldPolicy() {
        return fieldPolicy;
    }

    public void setFieldPolicy(FieldPolicy fieldPolicy) {
        this.fieldPolicy = fieldPolicy;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getIconColor() {
        return iconColor;
    }

    public void setIconColor(String iconColor) {
        this.iconColor = iconColor;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
