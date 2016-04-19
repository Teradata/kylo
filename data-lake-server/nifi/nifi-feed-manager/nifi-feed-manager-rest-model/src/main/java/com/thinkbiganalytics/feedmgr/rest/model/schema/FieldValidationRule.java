package com.thinkbiganalytics.feedmgr.rest.model.schema;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Created by sr186054 on 2/9/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FieldValidationRule {
    private String name;
    private String regex;
    private String type;
    private String description;
    private List<FieldRuleProperty> properties;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public List<FieldRuleProperty> getProperties() {
        return properties;
    }

    public void setProperties(List<FieldRuleProperty> properties) {
        this.properties = properties;
    }
}
