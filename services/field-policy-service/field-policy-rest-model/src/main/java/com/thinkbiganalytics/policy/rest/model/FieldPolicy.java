package com.thinkbiganalytics.policy.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Created by sr186054 on 1/25/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FieldPolicy {

    private boolean partition;
    private boolean profile;
    private boolean index;
    private String fieldName;

    private List<FieldStandardizationRule> standardization;

    private List<FieldValidationRule> validation;

    public FieldPolicy(){

    }

    public boolean isPartition() {
        return partition;
    }

    public void setPartition(boolean partition) {
        this.partition = partition;
    }

    public boolean isProfile() {
        return profile;
    }

    public void setProfile(boolean profile) {
        this.profile = profile;
    }

    public List<FieldStandardizationRule> getStandardization() {
        return standardization;
    }

    public void setStandardization(List<FieldStandardizationRule> standardization) {
        this.standardization = standardization;
    }

    public List<FieldValidationRule> getValidation() {
        return validation;
    }

    public void setValidation(List<FieldValidationRule> validation) {
        this.validation = validation;
    }

    public boolean isIndex() {
        return index;
    }

    public void setIndex(boolean index) {
        this.index = index;
    }


    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }
}
