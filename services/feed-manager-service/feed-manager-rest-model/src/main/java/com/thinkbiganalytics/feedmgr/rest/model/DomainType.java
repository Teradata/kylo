package com.thinkbiganalytics.feedmgr.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.thinkbiganalytics.policy.rest.model.FieldPolicy;

/**
 * Defines the domain type (zip, phone, credit card) of a column.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DomainType {

    private String description;
    private FieldPolicy fieldPolicy;
    private String icon;
    private String iconColor;
    private String id;
    private String regexFlags;
    private String regexPattern;
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

    public String getRegexFlags() {
        return regexFlags;
    }

    public void setRegexFlags(String regexFlags) {
        this.regexFlags = regexFlags;
    }

    public String getRegexPattern() {
        return regexPattern;
    }

    public void setRegexPattern(String regexPattern) {
        this.regexPattern = regexPattern;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
