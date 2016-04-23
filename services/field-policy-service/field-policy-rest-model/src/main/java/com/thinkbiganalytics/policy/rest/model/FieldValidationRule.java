package com.thinkbiganalytics.policy.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by sr186054 on 2/9/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FieldValidationRule  extends BaseUiPolicyRule {
    private String regex;
    private String type;


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

}
