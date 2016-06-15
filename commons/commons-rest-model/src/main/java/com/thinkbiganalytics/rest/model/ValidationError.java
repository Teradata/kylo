package com.thinkbiganalytics.rest.model;

/**
 * Created by Jeremy Merrifield on 6/13/16.
 */
public class ValidationError {
    private String fieldValue;
    private String errorMessage;

    public ValidationError(String fieldValue, String errorMessage) {
        this.fieldValue = fieldValue;
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getFieldValue() {
        return fieldValue;
    }
}
